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
    val status: String?
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


// Data Class for GetCaregiver API Response
data class CaregiverProfileResponse(
    val id: String?,
    val name: String?,
    val phone: String?,
    val email: String?,
    val image: String?  // URL string
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

    // New endpoint for GetCaregiver
    @GET("/v1/user/caregivers/{id}")
    suspend fun getCaregiverProfile(
        @Header("Authorization") bearerToken: String,
        @Path("id") caregiverId: String
    ): Response<CaregiverProfileResponse>
}