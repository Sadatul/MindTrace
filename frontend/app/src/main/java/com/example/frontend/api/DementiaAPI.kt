package com.example.frontend.api

import retrofit2.Response
import retrofit2.http.GET

interface DementiaAPI {
    @GET("/actuator/health")
    suspend fun getHealth(): Response<HealthResponse>
}