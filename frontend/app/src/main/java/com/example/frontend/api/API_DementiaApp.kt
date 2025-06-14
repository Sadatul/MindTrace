package com.example.frontend.api

import retrofit2.Response
import retrofit2.http.*

// Placeholder to prevent build errors from existing code
data class HealthResponse(
    val status: String
)

object AuthSession {
    var token: String? = null
    var userType: String? = null
}

data class AuthManagerResponse(
    val response: String?,
    val userType: String?
)

// New data class for caregiver details
data class CaregiverDetailsResponse(
    val name: String,
    val dob: String,
    val gender: String
)

data class PatientRegisterRequest(
    val name: String,
    val dob: String, // YYYY-MM-DD
    val gender: String, // M, F, etc.
    val primaryContact: String, // Corrected name based on previous context
    val otp: String // Corrected name based on previous context
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
    val otp: String?,
    val primaryContact: String? // Added based on usage in Dashboard
)

interface DementiaAPI {
    @GET("/actuator/health")
    suspend fun getHealth(): Response<HealthResponse>

    @POST("/v1/auth")
    suspend fun postAuth(
        @Header("Authorization") bearerToken: String
    ): Response<AuthManagerResponse>

    @POST("/v1/auth/register/caregiver")
    suspend fun registerCaregiver(
        @Header("Authorization") bearerToken: String,
        @Body request: CaregiverRegisterRequest
    ): Response<CaregiverRegisterResponse>

    @POST("/v1/auth/register/patient")
    suspend fun registerPatient(
        @Header("Authorization") bearerToken: String,
        @Body request: PatientRegisterRequest
    ): Response<CaregiverRegisterResponse> // Assuming same response type

    @GET("/v1/auth/register/otp")
    suspend fun getOtp(
        @Header("Authorization") bearerToken: String
    ): Response<OtpResponse>

    // New endpoint for caregiver details
    @GET("/v1/caregiver/details") // Example path, adjust to your actual API
    suspend fun getCaregiverDetails(
        @Header("Authorization") bearerToken: String
    ): Response<CaregiverDetailsResponse>
}