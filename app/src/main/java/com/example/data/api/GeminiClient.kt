package com.example.data.api

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
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
        val diagnosticMessage: String = message,
        val isNetworkError: Boolean = false,
        val isAuthError: Boolean = false,
        val isRateLimit: Boolean = false,
        val isBackendError: Boolean = false,
        val isTimeout: Boolean = false,
        val requestId: String? = null
    ) : GeminiResult<Nothing>()
}

/**
 * Hardened Production Backend Client.
 *
 * Architecture:
 * Android APK -> HTTPS -> Render Backend Proxy -> Gemini 3.5 Flash -> JSON Response -> Android APK
 *
 * Zero Gemini credentials in Android APK.
 * Enforces HTTPS, strictly validates Content-Type JSON, and handles connection failures gracefully.
 */
class GeminiClient(
    private val backendUrlProvider: () -> String? = { null }
) {
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            val contentType = response.body?.contentType()?.toString()?.lowercase() ?: ""
            val rawCode = response.code

            // Detect HTTP redirects which often lead to HTML login / warmup pages
            if (rawCode in 301..308) {
                val redirectLocation = response.header("Location") ?: "unknown"
                throw IOException("REDIRECT_${rawCode}: HTTP $rawCode redirect to $redirectLocation (URL: ${request.url})")
            }

            // Detect HTML responses returned when JSON was expected
            if (contentType.contains("text/html")) {
                val preview = try {
                    val peek = response.peekBody(200).string().replace("\n", " ").replace("\r", " ").trim()
                    if (peek.length > 80) peek.take(80) + "..." else peek
                } catch (e: Exception) { "" }
                throw IOException("HTML_RESPONSE: [HTTP $rawCode] Content-Type: $contentType | Preview: $preview")
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

    /**
     * Resolves the active backend URL.
     * In production, the URL is provided by GeminiConfig.DEFAULT_BACKEND_URL or validated HTTPS settings.
     */
    fun getEffectiveBackendUrl(): String {
        val custom = backendUrlProvider()?.trim()
        val url = if (!custom.isNullOrEmpty()) custom else GeminiConfig.DEFAULT_BACKEND_URL
        return if (url.endsWith("/")) url else "$url/"
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

    /**
     * Verifies connectivity with the hardened production backend and Gemini AI.
     */
    suspend fun testConnection(): GeminiResult<Boolean> {
        return testBackendProxy()
    }

    /**
     * Sends prompt/chat history to the secure server-side Node.js proxy.
     */
    suspend fun generateContent(
        history: List<GeminiContent>,
        systemPrompt: String = GeminiConfig.SYSTEM_PROMPT,
        mode: String = "normal"
    ): GeminiResult<String> {
        return chatViaBackend(history, systemPrompt, mode)
    }

    /**
     * Calls the dedicated /api/study-tools endpoint on the secure Render backend.
     * Returns raw JSON string of the structured data payload or an error.
     */
    suspend fun generateStudyTool(
        operation: String,
        topic: String = "",
        subject: String = "",
        syllabusContext: String = "",
        imageBase64: String = "",
        imageMimeType: String = "image/jpeg",
        prompt: String = "",
        academicContext: com.example.data.studytools.AcademicContext? = null
    ): GeminiResult<String> {
        return try {
            val api = createApiService()
            val request = com.example.data.studytools.SageStudyToolsRequest(
                operation = operation,
                topic = topic,
                subject = subject,
                syllabusContext = syllabusContext,
                imageBase64 = imageBase64,
                imageMimeType = imageMimeType,
                prompt = prompt,
                academicContext = academicContext
            )
            val response = api.generateStudyTool(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.success || body.ok == true) && body.data != null) {
                    val jsonAdapter = moshi.adapter(Any::class.java)
                    val dataJson = jsonAdapter.toJson(body.data)
                    GeminiResult.Success(dataJson)
                } else {
                    val errMsg = body?.error ?: "Study tool generation returned empty data."
                    GeminiResult.Error(
                        message = errMsg,
                        diagnosticMessage = errMsg,
                        requestId = body?.requestId
                    )
                }
            } else {
                parseHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun testBackendProxy(): GeminiResult<Boolean> {
        return try {
            val api = createApiService()

            // 1. Primary: Server health endpoint with upstream Gemini verification
            val healthResponse = try {
                api.checkHealth(checkGemini = true)
            } catch (e: Exception) {
                if (e is IOException && e.message?.contains("HTML_RESPONSE") == true) {
                    throw e
                }
                null
            }

            if (healthResponse != null && healthResponse.isSuccessful) {
                val body = healthResponse.body()
                if (body?.geminiConnected == true || body?.status == "ok" || body?.ok == true) {
                    return GeminiResult.Success(true)
                }
            }

            // 2. Fallback: Direct lightweight chat probe
            val probeRequest = SageBackendChatRequest(
                history = listOf(
                    SageHistoryItem(
                        role = "user",
                        text = GeminiConfig.CONNECTION_TEST_PROMPT
                    )
                ),
                mode = "normal",
                systemPrompt = "You are an automated health probe. Reply with SAGE_CONNECTION_OK."
            )

            val chatResponse = api.chatWithBackend(probeRequest)
            if (chatResponse.isSuccessful) {
                val body = chatResponse.body()
                if ((body?.success == true || body?.ok == true) && !body.reply.isNullOrBlank()) {
                    GeminiResult.Success(true)
                } else {
                    GeminiResult.Error(
                        message = "AI service returned an unexpected response. Please try again.",
                        diagnosticMessage = body?.error ?: "Sage backend returned an empty response.",
                        requestId = body?.requestId
                    )
                }
            } else {
                parseHttpError(chatResponse.code(), chatResponse.errorBody()?.string())
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun chatViaBackend(
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
                if (body != null && (body.success || body.ok == true) && !body.reply.isNullOrBlank()) {
                    GeminiResult.Success(body.reply)
                } else {
                    val errMsg = body?.error ?: "Sage backend returned an empty response."
                    GeminiResult.Error(
                        message = "AI service temporarily unable to answer. Please try again.",
                        diagnosticMessage = errMsg,
                        requestId = body?.requestId
                    )
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
                message = "Invalid study tool request. Please check inputs and try again.",
                diagnosticMessage = "Bad request to backend (HTTP 400): ${errorBody ?: "Invalid parameters"}",
                isBackendError = true
            )
            401, 403 -> GeminiResult.Error(
                message = "Service access error. Please try again later.",
                diagnosticMessage = "Backend authentication or authorization error (HTTP $code).",
                isAuthError = true
            )
            404 -> GeminiResult.Error(
                message = "Study service is temporarily unavailable. Please try again.",
                diagnosticMessage = "Backend endpoint not found (HTTP 404). Route /api/study-tools was not found on backend.",
                isBackendError = true
            )
            429 -> GeminiResult.Error(
                message = "Sage has reached its temporary AI request limit. Please try again shortly.",
                diagnosticMessage = "Gemini rate limit exceeded on server (HTTP 429).",
                isRateLimit = true
            )
            500 -> GeminiResult.Error(
                message = "Sage couldn't generate this material right now. Please try again.",
                diagnosticMessage = "Backend server error (HTTP 500): ${errorBody ?: "Internal error"}",
                isBackendError = true
            )
            502, 503 -> GeminiResult.Error(
                message = "Sage couldn't generate this material right now. Please try again.",
                diagnosticMessage = "Backend or Gemini AI service unavailable (HTTP $code).",
                isBackendError = true
            )
            504 -> GeminiResult.Error(
                message = "You're offline. Connect to the internet to generate new study material.",
                diagnosticMessage = "Backend gateway timeout (HTTP 504).",
                isTimeout = true
            )
            else -> GeminiResult.Error(
                message = "Sage couldn't generate this material right now. Please try again.",
                diagnosticMessage = "HTTP error $code: ${errorBody ?: "Unknown"}",
                isBackendError = true
            )
        }
    }

    private fun handleException(e: Exception): GeminiResult.Error {
        return when (e) {
            is UnknownHostException -> GeminiResult.Error(
                message = "You're offline. Connect to the internet to generate new study material.",
                diagnosticMessage = "Domain resolution failed: ${e.message}",
                isNetworkError = true
            )
            is ConnectException -> GeminiResult.Error(
                message = "You're offline. Connect to the internet to generate new study material.",
                diagnosticMessage = "Connection failed to backend: ${e.message}",
                isBackendError = true,
                isNetworkError = true
            )
            is SocketTimeoutException -> GeminiResult.Error(
                message = "You're offline. Connect to the internet to generate new study material.",
                diagnosticMessage = "Socket timeout: ${e.message}",
                isTimeout = true,
                isNetworkError = true
            )
            is JsonDataException, is JsonEncodingException -> GeminiResult.Error(
                message = "Sage couldn't parse the generated study material. Please retry.",
                diagnosticMessage = "Malformed JSON returned by backend: ${e.message}",
                isBackendError = true
            )
            is IOException -> {
                val msg = e.localizedMessage ?: e.message ?: "Connection error"
                if (msg.startsWith("HTML_RESPONSE:")) {
                    GeminiResult.Error(
                        message = "Study service is temporarily unavailable. Please try again.",
                        diagnosticMessage = "API returned HTML instead of JSON — production routing/deployment problem.",
                        isBackendError = true
                    )
                } else if (msg.startsWith("REDIRECT_")) {
                    GeminiResult.Error(
                        message = "Study service is temporarily unavailable. Please try again.",
                        diagnosticMessage = msg,
                        isBackendError = true
                    )
                } else {
                    GeminiResult.Error(
                        message = "You're offline. Connect to the internet to generate new study material.",
                        diagnosticMessage = "Network error: $msg",
                        isNetworkError = true
                    )
                }
            }
            else -> GeminiResult.Error(
                message = "Unexpected error: ${e.localizedMessage ?: e.javaClass.simpleName}",
                diagnosticMessage = "Unexpected: ${e.javaClass.name}: ${e.message}"
            )
        }
    }
}
