package com.example.frontend.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import com.example.frontend.api.models.RequestChat
import com.example.frontend.api.models.ResponseHealth
import okhttp3.ResponseBody

interface DementiaAPI {
    @GET("/actuator/health")
    suspend fun getHealth(): Response<ResponseHealth>

    @POST("/v1/chat")
    suspend fun sendChatMessage(@Body request: RequestChat): Response<ResponseBody>
}

data class ChatRequest(
    val query: String
)