package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("api/chat")
    suspend fun chatWithBackend(
        @Body request: SageBackendChatRequest
    ): Response<SageBackendChatResponse>

    @GET("api/health")
    suspend fun checkHealth(
        @Query("checkGemini") checkGemini: Boolean = true
    ): Response<SageBackendHealthResponse>
}
