package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @field:Json(name = "contents") val contents: List<GeminiContent>,
    @field:Json(name = "systemInstruction") val systemInstruction: GeminiSystemInstruction? = null,
    @field:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSystemInstruction(
    @field:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @field:Json(name = "role") val role: String? = null,
    @field:Json(name = "parts") val parts: List<GeminiPart> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @field:Json(name = "text") val text: String = ""
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @field:Json(name = "temperature") val temperature: Float = 0.7f,
    @field:Json(name = "topP") val topP: Float = 0.95f,
    @field:Json(name = "maxOutputTokens") val maxOutputTokens: Int = 2048
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @field:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @field:Json(name = "error") val error: GeminiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @field:Json(name = "content") val content: GeminiContent? = null,
    @field:Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorDetails(
    @field:Json(name = "code") val code: Int? = null,
    @field:Json(name = "message") val message: String? = null,
    @field:Json(name = "status") val status: String? = null
)

/**
 * Backend Proxy API Models
 */
@JsonClass(generateAdapter = true)
data class SageBackendChatRequest(
    @field:Json(name = "history") val history: List<SageHistoryItem>,
    @field:Json(name = "mode") val mode: String = "normal",
    @field:Json(name = "systemPrompt") val systemPrompt: String? = null
)

@JsonClass(generateAdapter = true)
data class SageHistoryItem(
    @field:Json(name = "role") val role: String,
    @field:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class SageBackendChatResponse(
    @field:Json(name = "success") val success: Boolean = false,
    @field:Json(name = "reply") val reply: String? = null,
    @field:Json(name = "model") val model: String? = null,
    @field:Json(name = "error") val error: String? = null,
    @field:Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class SageBackendHealthResponse(
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "service") val service: String? = null,
    @field:Json(name = "model") val model: String? = null,
    @field:Json(name = "hasApiKey") val hasApiKey: Boolean? = null,
    @field:Json(name = "geminiConnected") val geminiConnected: Boolean? = null,
    @field:Json(name = "testVerified") val testVerified: Boolean? = null
)

