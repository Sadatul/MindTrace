package com.example.frontend.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.http.Header
import com.example.frontend.api.models.RequestChat
import com.example.frontend.api.models.ResponseHealth
import com.example.frontend.api.models.ResponseChat
import okhttp3.ResponseBody

interface DementiaAPI {
    @GET("/actuator/health")
    suspend fun getHealth(): Response<ResponseHealth>

    @GET("/v1/chat")
    suspend fun getChatHistory(
        @Header("Authorization") firebaseIdToken: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ResponseChat>

    @POST("/v1/chat")
    suspend fun sendChatMessage(
        @Header("Authorization") firebaseIdToken: String,
        @Body request: RequestChat
    ): Response<ResponseBody>
}

data class ChatRequest(
    val query: String
)