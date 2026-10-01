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
                preferencesManager.lastErrorMessage = result.diagnosticMessage
            }
        }
        result
    }

    suspend fun sendMessage(
        topicId: Long,
        userText: String,
        currentMode: String,
        curriculumContext: String? = null
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

        // 4. Determine system prompt according to mode and curriculum state
        val systemPrompt = buildSystemPromptForMode(currentMode, topic, curriculumContext)

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
                preferencesManager.lastErrorMessage = result.diagnosticMessage
                Result.failure(Exception(result.message))
            }
        }
    }

    suspend fun editAndResendMessage(
        topicId: Long,
        userMessageId: Long,
        newText: String,
        currentMode: String,
        curriculumContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanText = newText.trim()
        if (cleanText.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Message cannot be empty"))
        }

        val topic = dao.getTopicById(topicId) ?: return@withContext Result.failure(
            IllegalStateException("Topic not found")
        )

        val targetMessage = dao.getMessageById(userMessageId) ?: return@withContext Result.failure(
            IllegalStateException("Target message not found")
        )

        // 1. Update the user message content and timestamp
        dao.updateMessage(
            targetMessage.copy(
                content = cleanText,
                timestamp = System.currentTimeMillis(),
                status = "SENT"
            )
        )

        // 2. Delete all subsequent messages in this topic (branch from this point forward)
        dao.deleteMessagesAfterId(topicId, userMessageId)

        // 3. Insert placeholder model message
        val modelMsgId = dao.insertMessage(
            MessageEntity(
                topicId = topicId,
                role = "model",
                content = "",
                status = "SENDING"
            )
        )

        // 4. Build history up to and including the updated user message
        val allMessages = dao.getMessagesForTopicOnce(topicId)
        val conversationHistory = mutableListOf<GeminiContent>()

        for (msg in allMessages) {
            if (msg.id == modelMsgId) continue // Skip placeholder
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

        // 5. Determine system prompt according to mode and curriculum state
        val systemPrompt = buildSystemPromptForMode(currentMode, topic, curriculumContext)

        // 6. Call Gemini API via Backend Proxy
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
                preferencesManager.lastErrorMessage = result.diagnosticMessage
                Result.failure(Exception(result.message))
            }
        }
    }

    suspend fun retryAiResponse(
        topicId: Long,
        assistantMessageId: Long,
        currentMode: String,
        curriculumContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val topic = dao.getTopicById(topicId) ?: return@withContext Result.failure(
            IllegalStateException("Topic not found")
        )

        val targetMessage = dao.getMessageById(assistantMessageId) ?: return@withContext Result.failure(
            IllegalStateException("Assistant message not found")
        )

        val previousSuccessfulContent = targetMessage.content
        val previousStatus = targetMessage.status

        // 1. Delete messages that came after this assistant message (if any)
        dao.deleteMessagesAfterId(topicId, assistantMessageId)

        // 2. Set this message to SENDING state (with empty content so UI shows typing indicator)
        dao.updateMessage(
            targetMessage.copy(
                content = "",
                status = "SENDING",
                timestamp = System.currentTimeMillis()
            )
        )

        // 3. Build history from messages strictly BEFORE this assistant message
        val allMessages = dao.getMessagesForTopicOnce(topicId)
        val conversationHistory = mutableListOf<GeminiContent>()

        for (msg in allMessages) {
            if (msg.id >= assistantMessageId) continue // skip this message and any later ones
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

        // 4. Determine system prompt
        val systemPrompt = buildSystemPromptForMode(currentMode, topic, curriculumContext)

        // 5. Call Gemini API
        val startTime = System.currentTimeMillis()
        val result = geminiClient.generateContent(conversationHistory, systemPrompt, mode = currentMode)
        val latency = System.currentTimeMillis() - startTime
        preferencesManager.lastLatencyMs = latency

        when (result) {
            is GeminiResult.Success -> {
                val aiResponse = result.data
                dao.updateMessage(
                    targetMessage.copy(
                        content = aiResponse,
                        timestamp = System.currentTimeMillis(),
                        status = "SENT"
                    )
                )
                dao.updateTopic(topic.copy(updatedAt = System.currentTimeMillis()))
                preferencesManager.lastRequestSuccess = true
                preferencesManager.lastErrorMessage = "None"
                Result.success(aiResponse)
            }
            is GeminiResult.Error -> {
                // Restore previous successful response if one existed, or mark as FAILED
                if (previousSuccessfulContent.isNotBlank() && previousStatus == "SENT") {
                    dao.updateMessage(
                        targetMessage.copy(
                            content = previousSuccessfulContent,
                            status = "SENT"
                        )
                    )
                } else {
                    dao.updateMessage(
                        targetMessage.copy(
                            content = result.message,
                            status = "FAILED"
                        )
                    )
                }
                preferencesManager.lastRequestSuccess = false
                preferencesManager.lastErrorMessage = result.diagnosticMessage
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
                    preferencesManager.lastErrorMessage = result.diagnosticMessage
                    Result.failure(Exception(result.message))
                }
            }
        }

    private fun buildSystemPromptForMode(mode: String, topic: TopicEntity, extraContext: String? = null): String {
        val basePrompt = GeminiConfig.SYSTEM_PROMPT
        val roadmapContextClause = buildString {
            if (topic.roadmapJson.isNotBlank()) {
                append("\n\nCURRICULUM CONTEXT:\n").append(topic.roadmapJson)
            }
            if (!extraContext.isNullOrBlank()) {
                append("\n\nUSER ROADMAP LEARNING PROGRESS & CONTEXT:\n").append(extraContext)
            }
        }

        val modeDirective = when (mode.uppercase()) {
            "LEARNING" -> """
Active Mode: STRUCTURED AI LEARNING MODE.
Curriculum Topic: ${topic.title}$roadmapContextClause
Pedagogical Tutor Flow:
You are an expert, encouraging tutor leading a structured learning session for the topic "${topic.title}".
Methodology:
1. Explain the core concept with clarity and precision.
2. Give a simple, relatable example.
3. Ask ONE focused comprehension question to check understanding.
4. When the user responds, evaluate their answer, praise insights, and gently correct errors.
5. Offer a practical mini-exercise or coding scenario.
6. Prepare them for the topic quiz.
Be conversational, structured, and engaging. Never give a massive static wall of text. Teach step-by-step.
            """.trimIndent()
            "SOCRATIC" -> """
Active Mode: SOCRATIC MODE.
Curriculum Topic: ${topic.title}$roadmapContextClause
Guideline: Do NOT give direct answers immediately. Challenge the user gently with insightful questions, prompts, and thought experiments so they arrive at the truth themselves.
            """.trimIndent()
            else -> """
Active Mode: NORMAL MODE.
Curriculum Topic: ${topic.title}$roadmapContextClause
Guideline: Answer questions directly, accurately, clearly, and concisely without unnecessary procedural friction, keeping context of the user's roadmap.
            """.trimIndent()
        }

        return "$basePrompt\n\n$modeDirective"
    }
}
