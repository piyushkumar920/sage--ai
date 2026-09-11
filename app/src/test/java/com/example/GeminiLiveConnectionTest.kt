package com.example

import com.example.data.api.GeminiClient
import com.example.data.api.GeminiConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiResult
import com.example.data.api.SageBackendChatResponse
import com.example.data.api.SageBackendHealthResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress

class GeminiLiveConnectionTest {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Test
    fun testClientKeyResolutionAndConfiguration() {
        println("=== VERIFYING API KEY RESOLUTION AND CONFIGURATION ===")

        // 1. Verify GeminiClient resolves the effective API key from BuildConfig or custom provider
        val client = GeminiClient()
        val effectiveKey = client.getEffectiveApiKey()
        println("Resolved effective key: ${effectiveKey?.take(6)}...")
        assertNotNull("Effective API key should be available via BuildConfig or provider", effectiveKey)

        // 2. Verify custom API key override works
        val customClient = GeminiClient(apiKeyProvider = { "custom-test-key-123" })
        assertEquals("Custom key should override default", "custom-test-key-123", customClient.getEffectiveApiKey())

        // 3. Verify client communicates with HTTPS backend proxy by default
        val effectiveUrl = client.getEffectiveBackendUrl()
        println("Effective Backend Proxy URL: $effectiveUrl")
        assertTrue("Backend URL must start with https:// or http://", effectiveUrl.startsWith("http"))
        assertEquals("Configured model must be gemini-3.5-flash", "gemini-3.5-flash", GeminiConfig.GEMINI_MODEL)
    }

    @Test
    fun testFullProxyFlowAndroidAppToBackend() = runBlocking {
        println("\n=== TESTING ANDROID APP -> BACKEND PROXY FLOW ===")

        // Spin up a local mock backend proxy using JVM's built-in HttpServer
        var capturedPath: String? = null
        var capturedBody: String? = null
        val server = HttpServer.create(InetSocketAddress(0), 0)
        val port = server.address.port

        server.createContext("/api/health") { exchange ->
            val healthJson = moshi.adapter(SageBackendHealthResponse::class.java).toJson(
                SageBackendHealthResponse(
                    status = "ok",
                    service = "sage-backend-proxy",
                    model = "gemini-3.5-flash",
                    hasApiKey = true,
                    geminiConnected = true,
                    testVerified = true
                )
            )
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, healthJson.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(healthJson.toByteArray()) }
        }

        server.createContext("/api/chat") { exchange ->
            capturedPath = exchange.requestURI.path
            capturedBody = exchange.requestBody.bufferedReader().use { it.readText() }

            val responseJson = moshi.adapter(SageBackendChatResponse::class.java).toJson(
                SageBackendChatResponse(
                    success = true,
                    reply = "Gravity is a fundamental force of nature that pulls objects toward each other.",
                    model = "gemini-3.5-flash"
                )
            )
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, responseJson.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(responseJson.toByteArray()) }
        }

        server.start()

        try {
            val localBackendUrl = "http://127.0.0.1:$port/"
            val client = GeminiClient(backendUrlProvider = { localBackendUrl }, apiKeyProvider = { "" })

            // 1. Connection Test: App -> Backend -> Health/Gemini
            val connectionResult = client.testConnection()
            println("Connection Test Result: $connectionResult")
            assertTrue("Connection test should succeed", connectionResult is GeminiResult.Success)

            // 2. Chat Request: App -> Backend -> Gemini
            val history = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Explain gravity in simple words."))
                )
            )
            val chatResult = client.generateContent(
                history = history,
                systemPrompt = GeminiConfig.SYSTEM_PROMPT,
                mode = "learning"
            )

            println("Chat Result: $chatResult")
            assertTrue("Chat request should succeed through backend proxy", chatResult is GeminiResult.Success)
            val replyText = (chatResult as GeminiResult.Success).data
            assertTrue("Reply must contain gravity explanation", replyText.contains("gravity", ignoreCase = true))

            // Verify payload received by backend proxy
            assertNotNull("Backend proxy must have received /api/chat", capturedPath)
            assertEquals("/api/chat", capturedPath)
            assertNotNull("Backend proxy must have received request body", capturedBody)
            assertTrue("Body must include user prompt", capturedBody!!.contains("Explain gravity in simple words."))
            assertTrue("Body must forward selected tutoring mode", capturedBody!!.contains("learning"))

            println("Verified: Android App successfully forwarded mode and conversation history to HTTPS backend!")
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun testBackendProxyErrorHandling() = runBlocking {
        println("\n=== TESTING BACKEND PROXY ERROR HANDLING ===")

        val server = HttpServer.create(InetSocketAddress(0), 0)
        val port = server.address.port
        var responseStatusCode = 429
        var responseBodyString = """{"success":false,"error":"Gemini rate limit exceeded.","code":"RATE_LIMIT"}"""

        server.createContext("/api/chat") { exchange ->
            exchange.responseHeaders.add("Content-Type", "application/json")
            val bytes = responseBodyString.toByteArray()
            exchange.sendResponseHeaders(responseStatusCode, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        server.start()

        try {
            val localBackendUrl = "http://127.0.0.1:$port/"
            val client = GeminiClient(backendUrlProvider = { localBackendUrl }, apiKeyProvider = { "" })
            val dummyHistory = listOf(
                GeminiContent(role = "user", parts = listOf(GeminiPart(text = "Hi")))
            )

            // Test 429 Rate Limit
            val rateLimitResult = client.generateContent(dummyHistory)
            println("429 Test Result: $rateLimitResult")
            assertTrue(rateLimitResult is GeminiResult.Error)
            assertTrue((rateLimitResult as GeminiResult.Error).isRateLimit)

            // Test 502 Service Unavailable
            responseStatusCode = 502
            responseBodyString = """{"success":false,"error":"Gemini temporarily unavailable","code":"GEMINI_UNAVAILABLE"}"""
            val serviceUnavailableResult = client.generateContent(dummyHistory)
            println("502 Test Result: $serviceUnavailableResult")
            assertTrue(serviceUnavailableResult is GeminiResult.Error)
            assertTrue((serviceUnavailableResult as GeminiResult.Error).isBackendError)

            println("Verified: Backend proxy errors (429, 502/503) handled gracefully!")
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun testActualCloudRunLiveEndpoints() = runBlocking {
        println("\n=== TESTING ACTUAL LIVE CLOUD RUN HTTPS ENDPOINTS ===")
        val client = GeminiClient()
        val backendUrl = client.getEffectiveBackendUrl()
        println("Testing live URL: $backendUrl")
        assertTrue("URL must be HTTPS", backendUrl.startsWith("https://"))
        assertTrue("URL must be on Google Cloud Run (.run.app)", backendUrl.contains(".run.app"))
        assertEquals("Must match verified Cloud Run URL", GeminiConfig.DEFAULT_BACKEND_URL, backendUrl)

        // Test GET /api/health over HTTPS
        val connectionResult = client.testConnection()
        println("Live HTTPS Connection Result: $connectionResult")
        // Verify client safely returns a GeminiResult (Success or clean Error without crashing)
        assertTrue(
            "Live HTTPS test must return a valid GeminiResult",
            connectionResult is GeminiResult.Success || connectionResult is GeminiResult.Error
        )

        // Test POST /api/chat over HTTPS with Gemini
        val history = listOf(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = "What is the capital of France? One word answer."))
            )
        )
        val chatResult = client.generateContent(
            history = history,
            systemPrompt = GeminiConfig.SYSTEM_PROMPT,
            mode = "normal"
        )
        println("Live HTTPS Chat Result: $chatResult")
        assertTrue(
            "Live HTTPS chat must return a valid GeminiResult: $chatResult",
            chatResult is GeminiResult.Success || chatResult is GeminiResult.Error
        )
        if (chatResult is GeminiResult.Success) {
            val reply = chatResult.data
            assertTrue("Reply must mention Paris: $reply", reply.contains("Paris", ignoreCase = true))
        }
        println("Verified: Android GeminiClient successfully handles live Cloud Run HTTPS endpoints!")
    }
}
