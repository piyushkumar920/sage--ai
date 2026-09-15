package com.example

import com.example.data.api.GeminiClient
import com.example.data.api.GeminiConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiClientTest {
    @Test
    fun testClientConfiguration() = runBlocking {
        val client = GeminiClient()
        val url = client.getEffectiveBackendUrl()
        assertTrue("Backend URL must start with https://", url.startsWith("https://"))
        assertEquals(GeminiConfig.DEFAULT_BACKEND_URL, url)
    }
}
