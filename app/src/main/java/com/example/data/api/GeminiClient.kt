package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class GeminiResult<out T> {
    data class Success<T>(val data: T) : GeminiResult<T>()
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false,
        val isAuthError: Boolean = false,
        val isRateLimit: Boolean = false,
        val isBackendError: Boolean = false,
        val isTimeout: Boolean = false
    ) : GeminiResult<Nothing>()
}

class GeminiClient(
    private val backendUrlProvider: () -> String? = { null },
    private val apiKeyProvider: () -> String? = { null }
) {
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            val contentType = response.body?.contentType()?.toString()
            if (response.code in 301..308) {
                throw IOException("Server returned redirect HTTP ${response.code} (gateway authentication or invalid path)")
            }
            if (contentType != null && contentType.contains("text/html")) {
                throw IOException("Server returned HTML instead of JSON (HTTP ${response.code})")
            }
            response
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun getEffectiveBackendUrl(): String {
        val custom = backendUrlProvider()?.trim()
        val base = if (!custom.isNullOrEmpty()) custom else GeminiConfig.DEFAULT_BACKEND_URL
        return if (base.endsWith("/")) base else "$base/"
    }

    fun getEffectiveApiKey(): String? {
        val custom = apiKeyProvider()?.trim()
        if (custom != null) {
            return if (custom.isNotEmpty()) custom else null
        }
        val buildKey = try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) {
            null
        }
        return if (!buildKey.isNullOrBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else null
    }

    private fun createApiService(): GeminiApiService {
        val url = getEffectiveBackendUrl()
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private fun createGoogleApiService(): GeminiApiService {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun testConnection(): GeminiResult<Boolean> {
        val directKey = getEffectiveApiKey()
        if (directKey != null) {
            return testDirectGoogleConnection(directKey)
        }
        return testBackendConnection()
    }

    private suspend fun testDirectGoogleConnection(apiKey: String): GeminiResult<Boolean> {
        return try {
            val api = createGoogleApiService()
            val probeRequest = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = GeminiConfig.CONNECTION_TEST_PROMPT))
                    )
                ),
                generationConfig = GeminiGenerationConfig(maxOutputTokens = 64)
            )

            val models = listOf("gemini-3.5-flash", "gemini-3.5-flash-lite", "gemini-flash-latest", "gemini-3.6-flash")
            var lastErr: GeminiResult.Error? = null
            for (model in models) {
                val response = api.generateContentDirect(model, apiKey, probeRequest)
                if (response.isSuccessful) {
                    val reply = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!reply.isNullOrBlank()) {
                        return GeminiResult.Success(true)
                    }
                } else {
                    lastErr = parseHttpError(response.code(), response.errorBody()?.string())
                    if (response.code() == 404 || response.code() == 429 || response.code() == 503) {
                        continue // try next candidate model
                    }
                    return lastErr
                }
            }
            lastErr ?: GeminiResult.Error("Google Gemini API returned an empty response.")
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun testBackendConnection(): GeminiResult<Boolean> {
        return try {
            val api = createApiService()
            // 1. Try health check endpoint first
            val healthResponse = try {
                api.checkHealth(checkGemini = true)
            } catch (_: Exception) {
                null
            }

            if (healthResponse != null && healthResponse.isSuccessful) {
                val body = healthResponse.body()
                if (body?.geminiConnected == true || body?.status == "ok") {
                    return GeminiResult.Success(true)
                }
            }

            // 2. Fallback to end-to-end chat probe
            val probeRequest = SageBackendChatRequest(
                history = listOf(
                    SageHistoryItem(
                        role = "user",
                        text = GeminiConfig.CONNECTION_TEST_PROMPT
                    )
                ),
                mode = "normal",
                systemPrompt = "You are a test probe. Reply with SAGE_CONNECTION_OK."
            )

            val chatResponse = api.chatWithBackend(probeRequest)
            if (chatResponse.isSuccessful) {
                val body = chatResponse.body()
                if (body?.success == true && !body.reply.isNullOrBlank()) {
                    GeminiResult.Success(true)
                } else {
                    GeminiResult.Error("Sage backend returned an empty response.")
                }
            } else {
                parseHttpError(chatResponse.code(), chatResponse.errorBody()?.string())
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun generateContent(
        history: List<GeminiContent>,
        systemPrompt: String = GeminiConfig.SYSTEM_PROMPT,
        mode: String = "normal"
    ): GeminiResult<String> {
        val directKey = getEffectiveApiKey()
        if (directKey != null) {
            return generateDirectGoogleContent(directKey, history, systemPrompt, mode)
        }
        return generateBackendContent(history, systemPrompt, mode)
    }

    private suspend fun generateDirectGoogleContent(
        apiKey: String,
        history: List<GeminiContent>,
        systemPrompt: String,
        mode: String
    ): GeminiResult<String> {
        return try {
            val api = createGoogleApiService()
            val request = GeminiRequest(
                contents = history.map { item ->
                    GeminiContent(
                        role = if (item.role == "model" || item.role == "assistant") "model" else "user",
                        parts = item.parts.map { GeminiPart(text = it.text) }
                    )
                },
                systemInstruction = GeminiSystemInstruction(
                    parts = listOf(GeminiPart(text = "$systemPrompt\nCurrent Mode: $mode"))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                )
            )

            val fallbackModels = listOf("gemini-3.5-flash", "gemini-3.5-flash-lite", "gemini-flash-latest", "gemini-3.6-flash")
            var lastError: GeminiResult.Error? = null
            for (model in fallbackModels) {
                val response = api.generateContentDirect(model, apiKey, request)
                if (response.isSuccessful) {
                    val reply = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!reply.isNullOrBlank()) {
                        return GeminiResult.Success(reply)
                    }
                } else {
                    val err = parseHttpError(response.code(), response.errorBody()?.string())
                    if (response.code() == 404 || response.code() == 429 || response.code() == 503) {
                        lastError = err
                        continue // try next model
                    }
                    return err
                }
            }
            lastError ?: GeminiResult.Error("Gemini returned an empty response.")
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun generateBackendContent(
        history: List<GeminiContent>,
        systemPrompt: String,
        mode: String
    ): GeminiResult<String> {
        val historyItems = history.map { item ->
            SageHistoryItem(
                role = if (item.role == "model" || item.role == "assistant") "model" else "user",
                text = item.parts.joinToString("") { it.text }
            )
        }

        val request = SageBackendChatRequest(
            history = historyItems,
            mode = mode.lowercase(),
            systemPrompt = systemPrompt
        )

        return try {
            val api = createApiService()
            val response = api.chatWithBackend(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && !body.reply.isNullOrBlank()) {
                    GeminiResult.Success(body.reply)
                } else {
                    val errMsg = body?.error ?: "Sage backend returned an empty response."
                    GeminiResult.Error(errMsg)
                }
            } else {
                parseHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun parseHttpError(code: Int, errorBody: String?): GeminiResult.Error {
        return when (code) {
            400 -> GeminiResult.Error(
                "Bad request to Sage backend: ${errorBody ?: "Invalid parameters"}",
                isBackendError = true
            )
            401, 403 -> GeminiResult.Error(
                "Sage backend authentication or authorization error.",
                isAuthError = true
            )
            404 -> GeminiResult.Error(
                "Sage backend endpoint not found (HTTP 404). Please verify backend URL in Diagnostics.",
                isBackendError = true
            )
            429 -> GeminiResult.Error(
                "Gemini rate limit exceeded. Please wait a moment before asking another question.",
                isRateLimit = true
            )
            502, 503 -> GeminiResult.Error(
                "Sage backend or Gemini AI service is temporarily unavailable. Please retry shortly.",
                isBackendError = true
            )
            504 -> GeminiResult.Error(
                "Connection to Sage backend timed out. Please check your internet and retry.",
                isTimeout = true
            )
            else -> GeminiResult.Error(
                "AI backend request failed (HTTP $code): ${errorBody ?: "Unknown error"}",
                isBackendError = true
            )
        }
    }

    private fun handleException(e: Exception): GeminiResult.Error {
        return when (e) {
            is UnknownHostException -> GeminiResult.Error(
                "You're offline or the backend domain could not be resolved. Please check your internet connection.",
                isNetworkError = true
            )
            is ConnectException -> GeminiResult.Error(
                "Unable to connect to Sage backend server. Please verify the backend is online.",
                isBackendError = true
            )
            is SocketTimeoutException -> GeminiResult.Error(
                "Connection to Sage backend timed out. Please check your network and retry.",
                isTimeout = true,
                isNetworkError = true
            )
            is IOException -> {
                val msg = e.localizedMessage ?: e.message ?: "Connection interrupted"
                if (msg.contains("HTML instead of JSON", ignoreCase = true) || msg.contains("redirect HTTP", ignoreCase = true)) {
                    GeminiResult.Error(
                        "Backend returned web page instead of JSON. Please set your Gemini API key in Developer Diagnostics & API Key Setup for direct free-tier access.",
                        isBackendError = true
                    )
                } else {
                    GeminiResult.Error(
                        "Network communication error: $msg",
                        isNetworkError = true
                    )
                }
            }
            else -> GeminiResult.Error(
                "Unexpected error: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }
}

