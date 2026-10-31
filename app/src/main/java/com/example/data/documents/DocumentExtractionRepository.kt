package com.example.data.documents

import android.content.Context
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class DocumentExtractionRepository(
    private val context: Context,
    private val geminiClient: GeminiClient
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun extractSyllabus(
        documentName: String,
        mimeType: String,
        fileBase64: String,
        textContent: String = ""
    ): GeminiResult<DocumentExtractionResponse> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = geminiClient.getEffectiveBackendUrl()
            val url = "${baseUrl}api/documents/extract"

            val requestObj = DocumentExtractionRequest(
                documentName = documentName,
                mimeType = mimeType,
                fileBase64 = fileBase64,
                textContent = textContent
            )

            val jsonAdapter = moshi.adapter(DocumentExtractionRequest::class.java)
            val jsonBody = jsonAdapter.toJson(requestObj)
            val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseString = response.body?.string() ?: ""
                if (response.isSuccessful && responseString.isNotBlank()) {
                    val respAdapter = moshi.adapter(DocumentExtractionResponse::class.java)
                    val parsed = respAdapter.fromJson(responseString)
                    if (parsed != null && parsed.success) {
                        GeminiResult.Success(parsed)
                    } else {
                        GeminiResult.Error(message = parsed?.error ?: "Document extraction failed.")
                    }
                } else {
                    GeminiResult.Error(message = "Server returned error HTTP ${response.code}: $responseString")
                }
            }
        } catch (e: Exception) {
            GeminiResult.Error(message = "Network error during document extraction: ${e.message}", isNetworkError = true)
        }
    }
}
