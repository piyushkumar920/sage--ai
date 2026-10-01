package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.example.data.local.MessageEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDatabase
import com.example.data.local.TopicEntity
import com.example.data.repository.SageRepository
import com.example.ui.components.MathFormatter
import com.example.ui.components.SageMarkdownParser
import com.example.ui.components.SageRichBlock
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChatEditAndRetryTest {

    private lateinit var database: SageDatabase
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: SageRepository
    private var testTopicId: Long = 0L

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, SageDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        preferencesManager = PreferencesManager(context)

        val mockClient = object : GeminiClient(backendUrlProvider = { null }) {
            override suspend fun generateContent(
                conversationHistory: List<com.example.data.api.GeminiContent>,
                systemInstruction: String?,
                mode: String?
            ): GeminiResult<String> {
                val lastUserMsg = conversationHistory.lastOrNull { it.role == "user" }
                    ?.parts?.firstOrNull()?.text ?: ""
                return if (lastUserMsg.contains("FAIL_TRIGGER")) {
                    GeminiResult.Error(message = "API temporary failure", diagnosticMessage = "Simulated 500 error")
                } else {
                    GeminiResult.Success("AI Response for: $lastUserMsg. Formula: \$\$Z = \\sqrt{R^2 + X^2}\$\$")
                }
            }
        }

        repository = SageRepository(database.sageDao(), mockClient, preferencesManager)

        val topic = TopicEntity(title = "Operating Systems Test", mode = "NORMAL")
        testTopicId = database.sageDao().insertTopic(topic)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test edit empty message returns failure and does not alter database`() = runBlocking {
        val userMsgId = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "user", content = "Explain round robin")
        )

        val resultEmpty = repository.editAndResendMessage(
            topicId = testTopicId,
            userMessageId = userMsgId,
            newText = "   ",
            currentMode = "NORMAL"
        )

        assertTrue(resultEmpty.isFailure)
        val msgInDb = database.sageDao().getMessageById(userMsgId)
        assertEquals("Explain round robin", msgInDb?.content)
    }

    @Test
    fun `test edit user message updates content and generates new response`() = runBlocking {
        // Initial turn
        val userMsg1Id = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "user", content = "Explain scheduling")
        )
        val aiMsg1Id = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "model", content = "Scheduling is...")
        )

        // Second turn
        val userMsg2Id = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "user", content = "What is FCFS?")
        )
        val aiMsg2Id = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "model", content = "FCFS is First Come First Served")
        )

        // Edit second user message
        val editResult = repository.editAndResendMessage(
            topicId = testTopicId,
            userMessageId = userMsg2Id,
            newText = "Explain Round Robin scheduling with an example.",
            currentMode = "NORMAL"
        )

        assertTrue(editResult.isSuccess)
        val messages = database.sageDao().getMessagesForTopicOnce(testTopicId)

        // Old downstream AI message (aiMsg2Id) should have been replaced/deleted and new placeholder filled
        val editedUserMsg = messages.find { it.id == userMsg2Id }
        assertNotNull(editedUserMsg)
        assertEquals("Explain Round Robin scheduling with an example.", editedUserMsg?.content)

        val latestAiMsg = messages.last()
        assertEquals("model", latestAiMsg.role)
        assertEquals("SENT", latestAiMsg.status)
        assertTrue(latestAiMsg.content.contains("Round Robin scheduling"))
    }

    @Test
    fun `test retry AI response regenerates response without duplicating user message`() = runBlocking {
        val userMsgId = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "user", content = "Explain impedance in RLC circuit")
        )
        val aiMsgId = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "model", content = "Old basic impedance explanation", status = "SENT")
        )

        val retryResult = repository.retryAiResponse(
            topicId = testTopicId,
            assistantMessageId = aiMsgId,
            currentMode = "NORMAL"
        )

        assertTrue(retryResult.isSuccess)
        val messages = database.sageDao().getMessagesForTopicOnce(testTopicId)

        // Verify user message was NOT duplicated
        val userMessages = messages.filter { it.role == "user" }
        assertEquals(1, userMessages.size)
        assertEquals("Explain impedance in RLC circuit", userMessages[0].content)

        // Verify AI message was updated with new generation
        val updatedAiMsg = database.sageDao().getMessageById(aiMsgId)
        assertNotNull(updatedAiMsg)
        assertEquals("SENT", updatedAiMsg?.status)
        assertTrue(updatedAiMsg?.content?.contains("Explain impedance in RLC circuit") == true)
    }

    @Test
    fun `test retry AI response preserves previous successful content if new generation fails`() = runBlocking {
        val userMsgId = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "user", content = "Explain FAIL_TRIGGER concept")
        )
        val aiMsgId = database.sageDao().insertMessage(
            MessageEntity(topicId = testTopicId, role = "model", content = "Original high-yield explanation", status = "SENT")
        )

        val retryResult = repository.retryAiResponse(
            topicId = testTopicId,
            assistantMessageId = aiMsgId,
            currentMode = "NORMAL"
        )

        assertTrue(retryResult.isFailure)

        // Previous successful response is preserved
        val preservedMsg = database.sageDao().getMessageById(aiMsgId)
        assertNotNull(preservedMsg)
        assertEquals("SENT", preservedMsg?.status)
        assertEquals("Original high-yield explanation", preservedMsg?.content)
    }

    @Test
    fun `test generated response from retry renders math and markdown properly`() {
        val response = "### Impedance in RLC Circuit\n\nThe total impedance is given by:\n\n\$\$Z = \\sqrt{R^2 + (X_L - X_C)^2}\$\$\n\nWhere:\n- \$X_L = 2\\pi fL\$ is inductive reactance\n- \$X_C = \\frac{1}{2\\pi fC}\$ is capacitive reactance"
        
        val blocks = SageMarkdownParser.parseBlocks(response)
        assertTrue(blocks.any { it is SageRichBlock.Heading })
        assertTrue(blocks.any { it is SageRichBlock.DisplayMath })
        assertTrue(blocks.any { it is SageRichBlock.BulletList })

        val mathFormatted = MathFormatter.latexToUnicode(response)
        assertTrue(mathFormatted.contains("√"))
        assertTrue(mathFormatted.contains("π"))
        assertFalse(mathFormatted.contains("\\sqrt"))
        assertFalse(mathFormatted.contains("\\frac"))
    }
}
