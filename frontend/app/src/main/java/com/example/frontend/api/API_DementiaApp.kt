package com.example.frontend.api

import retrofit2.Response
import retrofit2.http.*

data class AuthManager(
    val token: String?,
    val userType: String? // "caregiver" or "patient"
)
// Placeholder to prevent build errors from existing code
data class HealthResponse(
    val status: String
)

data class PatientRegisterRequest(
    val name: String,
    val dob: String, // YYYY-MM-DD
    val gender: String, // M, F, etc.
    val primaryContact: String,
    val otp: String
)

data class CaregiverRegisterRequest(
    val name: String,
    val dob: String, // YYYY-MM-DD
    val gender: String // M, F, etc.
)

data class CaregiverRegisterResponse(
    val msg: String?
)

data class OtpResponse(
    val otp: String?
)

interface DementiaAPI {
    @GET("/actuator/health")
    suspend fun getHealth(): Response<HealthResponse>

    @POST("/v1/auth")
    suspend fun postAuth(
        @Header("Authorization") bearerToken: String
    ): Response<AuthManager>

    @POST("/v1/auth/register/caregiver")
    suspend fun registerCaregiver(
        @Header("Authorization") bearerToken: String,
        @Body request: CaregiverRegisterRequest
    ): Response<CaregiverRegisterResponse>

    @POST("/v1/auth/register/patient")
    suspend fun registerPatient(
        @Header("Authorization") bearerToken: String,
        @Body request: PatientRegisterRequest
    ): Response<CaregiverRegisterResponse>

    @GET("/v1/auth/register/otp")
    suspend fun getOtp(
        @Header("Authorization") bearerToken: String
    ): Response<OtpResponse>
}