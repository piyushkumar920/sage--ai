package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.api.GeminiConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiResult
import com.example.data.local.MessageEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDao
import com.example.data.local.TopicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SageRepository(
    private val dao: SageDao,
    private val geminiClient: GeminiClient,
    private val preferencesManager: PreferencesManager
) {
    val allTopics: Flow<List<TopicEntity>> = dao.getAllTopics()

    fun getMessagesForTopic(topicId: Long): Flow<List<MessageEntity>> =
        dao.getMessagesForTopic(topicId)

    suspend fun getTopicById(topicId: Long): TopicEntity? = withContext(Dispatchers.IO) {
        dao.getTopicById(topicId)
    }

    suspend fun getOrCreateDefaultTopic(): TopicEntity = withContext(Dispatchers.IO) {
        val savedId = preferencesManager.activeTopicId
        if (savedId > 0) {
            val existing = dao.getTopicById(savedId)
            if (existing != null) return@withContext existing
        }
        val latest = dao.getLatestTopic()
        if (latest != null) {
            preferencesManager.activeTopicId = latest.id
            return@withContext latest
        }
        val newTopic = TopicEntity(
            title = "General Learning",
            mode = "NORMAL"
        )
        val id = dao.insertTopic(newTopic)
        val created = newTopic.copy(id = id)
        preferencesManager.activeTopicId = id
        created
    }

    suspend fun createTopic(title: String, mode: String = "NORMAL"): TopicEntity = withContext(Dispatchers.IO) {
        val topic = TopicEntity(
            title = title.trim().ifEmpty { "Learning Session" },
            mode = mode
        )
        val id = dao.insertTopic(topic)
        val created = topic.copy(id = id)
        preferencesManager.activeTopicId = id
        created
    }

    suspend fun updateTopic(topic: TopicEntity) = withContext(Dispatchers.IO) {
        dao.updateTopic(topic.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTopic(topic: TopicEntity) = withContext(Dispatchers.IO) {
        dao.deleteTopic(topic)
        if (preferencesManager.activeTopicId == topic.id) {
            val remaining = dao.getLatestTopic()
            preferencesManager.activeTopicId = remaining?.id ?: -1L
        }
    }

    suspend fun testConnection(): GeminiResult<Boolean> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val result = geminiClient.testConnection()
        val duration = System.currentTimeMillis() - startTime
        preferencesManager.lastLatencyMs = duration
        when (result) {
            is GeminiResult.Success -> {
                preferencesManager.isConnectionVerified = true
                preferencesManager.lastRequestSuccess = true
                preferencesManager.lastErrorMessage = "None"
            }
            is GeminiResult.Error -> {
                preferencesManager.lastRequestSuccess = false
                preferencesManager.lastErrorMessage = result.message
            }
        }
        result
    }

    suspend fun sendMessage(
        topicId: Long,
        userText: String,
        currentMode: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanText = userText.trim()
        if (cleanText.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Message cannot be empty"))
        }

        val topic = dao.getTopicById(topicId) ?: return@withContext Result.failure(
            IllegalStateException("Topic not found")
        )

        // If topic was default titled and this is the first message, rename it nicely
        if (topic.title == "General Learning" || topic.title == "New Topic" || topic.title == "Learning Session") {
            val titleSnippet = if (cleanText.length > 30) cleanText.take(27) + "..." else cleanText
            dao.updateTopic(topic.copy(title = titleSnippet, updatedAt = System.currentTimeMillis()))
        }

        // 1. Insert user message
        val userMsgId = dao.insertMessage(
            MessageEntity(
                topicId = topicId,
                role = "user",
                content = cleanText,
                status = "SENT"
            )
        )

        // 2. Insert placeholder model message
        val modelMsgId = dao.insertMessage(
            MessageEntity(
                topicId = topicId,
                role = "model",
                content = "",
                status = "SENDING"
            )
        )

        // 3. Build history from Room (excluding the currently pending message)
        val allMessages = dao.getMessagesForTopicOnce(topicId)
        val conversationHistory = mutableListOf<GeminiContent>()

        for (msg in allMessages) {
            if (msg.id == modelMsgId) continue // Skip the pending one
            if (msg.content.isBlank() && msg.role == "model") continue
            if (msg.status == "FAILED") continue

            val geminiRole = if (msg.role == "user") "user" else "model"
            conversationHistory.add(
                GeminiContent(
                    role = geminiRole,
                    parts = listOf(GeminiPart(text = msg.content))
                )
            )
        }

        // 4. Determine system prompt according to mode
        val systemPrompt = buildSystemPromptForMode(currentMode, topic)

        // 5. Call Gemini API via Backend Proxy
        val startTime = System.currentTimeMillis()
        val result = geminiClient.generateContent(conversationHistory, systemPrompt, mode = currentMode)
        val latency = System.currentTimeMillis() - startTime
        preferencesManager.lastLatencyMs = latency

        when (result) {
            is GeminiResult.Success -> {
                val aiResponse = result.data
                dao.updateMessage(
                    MessageEntity(
                        id = modelMsgId,
                        topicId = topicId,
                        role = "model",
                        content = aiResponse,
                        timestamp = System.currentTimeMillis(),
                        status = "SENT"
                    )
                )
                dao.updateTopic(topic.copy(updatedAt = System.currentTimeMillis()))
                preferencesManager.updateStreak()
                preferencesManager.lastRequestSuccess = true
                preferencesManager.lastErrorMessage = "None"
                Result.success(aiResponse)
            }
            is GeminiResult.Error -> {
                dao.updateMessage(
                    MessageEntity(
                        id = modelMsgId,
                        topicId = topicId,
                        role = "model",
                        content = result.message,
                        timestamp = System.currentTimeMillis(),
                        status = "FAILED"
                    )
                )
                preferencesManager.lastRequestSuccess = false
                preferencesManager.lastErrorMessage = result.message
                Result.failure(Exception(result.message))
            }
        }
    }

    suspend fun retryMessage(topicId: Long, failedMessageId: Long, currentMode: String): Result<String> =
        withContext(Dispatchers.IO) {
            val topic = dao.getTopicById(topicId) ?: return@withContext Result.failure(
                IllegalStateException("Topic not found")
            )

            // Update failed message back to SENDING
            dao.updateMessage(
                MessageEntity(
                    id = failedMessageId,
                    topicId = topicId,
                    role = "model",
                    content = "",
                    status = "SENDING"
                )
            )

            // Build history up to the message before this failed one
            val allMessages = dao.getMessagesForTopicOnce(topicId)
            val conversationHistory = mutableListOf<GeminiContent>()

            for (msg in allMessages) {
                if (msg.id == failedMessageId) continue
                if (msg.content.isBlank() && msg.role == "model") continue
                if (msg.status == "FAILED") continue

                val geminiRole = if (msg.role == "user") "user" else "model"
                conversationHistory.add(
                    GeminiContent(
                        role = geminiRole,
                        parts = listOf(GeminiPart(text = msg.content))
                    )
                )
            }

            val systemPrompt = buildSystemPromptForMode(currentMode, topic)
            val startTime = System.currentTimeMillis()
            val result = geminiClient.generateContent(conversationHistory, systemPrompt, mode = currentMode)
            val latency = System.currentTimeMillis() - startTime
            preferencesManager.lastLatencyMs = latency

            when (result) {
                is GeminiResult.Success -> {
                    dao.updateMessage(
                        MessageEntity(
                            id = failedMessageId,
                            topicId = topicId,
                            role = "model",
                            content = result.data,
                            timestamp = System.currentTimeMillis(),
                            status = "SENT"
                        )
                    )
                    preferencesManager.lastRequestSuccess = true
                    preferencesManager.lastErrorMessage = "None"
                    Result.success(result.data)
                }
                is GeminiResult.Error -> {
                    dao.updateMessage(
                        MessageEntity(
                            id = failedMessageId,
                            topicId = topicId,
                            role = "model",
                            content = result.message,
                            timestamp = System.currentTimeMillis(),
                            status = "FAILED"
                        )
                    )
                    preferencesManager.lastRequestSuccess = false
                    preferencesManager.lastErrorMessage = result.message
                    Result.failure(Exception(result.message))
                }
            }
        }

    private fun buildSystemPromptForMode(mode: String, topic: TopicEntity): String {
        val basePrompt = GeminiConfig.SYSTEM_PROMPT
        val modeDirective = when (mode.uppercase()) {
            "LEARNING" -> """
Active Mode: LEARNING MODE.
Topic: ${topic.title}
Guideline: Guide the user methodically through the 7-step learning journey. Ask one diagnostic question at a time. Adapt to their level, teach one concept clearly, and ask comprehension questions before moving forward.
            """.trimIndent()
            "SOCRATIC" -> """
Active Mode: SOCRATIC MODE.
Topic: ${topic.title}
Guideline: Do NOT give direct answers immediately. Challenge the user gently with insightful questions, prompts, and thought experiments so they arrive at the truth themselves.
            """.trimIndent()
            else -> """
Active Mode: NORMAL MODE.
Topic: ${topic.title}
Guideline: Answer questions directly, accurately, clearly, and concisely without unnecessary procedural friction.
            """.trimIndent()
        }

        return "$basePrompt\n\n$modeDirective"
    }
}
