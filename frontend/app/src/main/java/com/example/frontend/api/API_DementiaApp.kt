package com.example.frontend.api

import com.example.frontend.api.models.RequestChat
import com.example.frontend.api.models.ResponseChat
import com.example.frontend.screens.NavigationManager
import com.example.frontend.screens.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// Placeholder to prevent build errors from existing code
data class HealthResponse(val status: String)

data class AuthManagerResponse(val status: String?)

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


// Data Class for GetCaregiver API Response
data class CaregiverProfileResponse(
    val id: String?,
    val name: String?,
    val phone: String?,
    val email: String?,
    val image: String? // URL string
)

data class OtpResponse(val otp: String?)

data class PrimaryContact(
    val id: String,
    val name: String,
    val gender: String,
    val profilePicture: String?
)

data class UserInfoResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val gender: String,
    val dob: String,
    val profilePicture: String?,
    val primaryContact: PrimaryContact?,
    val createdAt: String,
    val telegramChatId: String?
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
    ): Response<ResponseBody>

    @POST("/v1/auth/register/patient")
    suspend fun registerPatient(
        @Header("Authorization") bearerToken: String,
        @Body request: PatientRegisterRequest
    ): Response<ResponseBody> // Assuming same response type

    @GET("/v1/auth/register/otp")
    suspend fun getOtp(@Header("Authorization") bearerToken: String): Response<OtpResponse>

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

    @GET("/v1/users")
    suspend fun getUserInfo(
        @Header("Authorization") firebaseIdToken: String,
        @Query("userId") userId: String? = null
    ): Response<UserInfoResponse>

}

suspend fun getIdTokenWithFallback(forceRefresh: Boolean = false, fallback: () -> Unit): String? {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        fallback()
        return null
    }

    return try {
        val result = currentUser.getIdToken(forceRefresh).await()
        result.token ?: run {
            fallback()
            null
        }
    } catch (e: FirebaseAuthInvalidUserException) {
        fallback()
        null
    } catch (e: Exception) {
        fallback()
        null
    }
}

suspend fun DementiaAPI.getIdToken(forceRefresh: Boolean = false): String {
    return getIdTokenWithFallback(forceRefresh) {
        NavigationManager.getNavController().navigate(Screen.Main)
    }!!
}
