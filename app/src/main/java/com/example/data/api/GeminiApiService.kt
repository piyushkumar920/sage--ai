package com.example.data.api

import com.example.data.studytools.SageStudyToolsRequest
import com.example.data.studytools.SageStudyToolsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

import retrofit2.http.Header

interface GeminiApiService {
    @POST("api/chat")
    suspend fun chatWithBackend(
        @Body request: SageBackendChatRequest,
        @Header("Authorization") authHeader: String? = null
    ): Response<SageBackendChatResponse>

    @POST("api/study-tools")
    suspend fun generateStudyTool(
        @Body request: SageStudyToolsRequest,
        @Header("Authorization") authHeader: String? = null
    ): Response<SageStudyToolsResponse>

    @POST("api/roadmaps/generate")
    suspend fun generateRoadmap(
        @Body request: com.example.data.studytools.RoadmapGenerationRequest,
        @Header("Authorization") authHeader: String? = null
    ): Response<SageStudyToolsResponse>

    @GET("api/health")
    suspend fun checkHealth(
        @Query("checkGemini") checkGemini: Boolean = true
    ): Response<SageBackendHealthResponse>
}
