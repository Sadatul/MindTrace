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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// Placeholder to prevent build errors from existing code
data class HealthResponse(val status: String)

data class AuthManagerResponse(val status: String?)

data class PatientRegisterRequest(
    val name: String,
    val email: String,
    val dob: String, // YYYY-MM-DD
    val gender: String, // M, F, etc.
    val profilePicture: String?, // Google profile picture URL
    val primaryContact: String, // Corrected name based on previous context
    val otp: String // Corrected name based on previous context
)

data class CaregiverRegisterRequest(
    val name: String,
    val dob: String, // YYYY-MM-DD
    val gender: String // M, F, etc.
)

data class OtpResponse(val otp: String?)

data class PrimaryContact(
    val id: String,
    val name: String,
    val gender: String,
    val profilePicture: String?
)

data class UserInfo(
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
    ): Response<UserInfo>

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
    } catch (_: FirebaseAuthInvalidUserException) {
        fallback()
        null
    } catch (_: Exception) {
        fallback()
        null
    }
}

suspend fun DementiaAPI.getIdToken(forceRefresh: Boolean = false, autoRedirect: Boolean = true): String? {
    return getIdTokenWithFallback(forceRefresh) {
        if (autoRedirect) redirectToLogin()
    }
}

suspend fun DementiaAPI.getSelfUserInfo(autoRedirect: Boolean = true): UserInfo? {
    val cachedUserInfo = SelfUserInfoCache.getUserInfo()
    if (cachedUserInfo != null) return cachedUserInfo

    val token: String = getIdToken(autoRedirect = autoRedirect) ?: return null

    val response = getUserInfo("Bearer $token")
    if (response.isSuccessful) {
        val body = response.body()
        if (body != null) {
            SelfUserInfoCache.setUserInfo(body)
        }
        return body
    } else {
        if (autoRedirect) redirectToLogin()
        return null
    }
}

fun DementiaAPI.signOutUser() {
    SelfUserInfoCache.signOutUser()
    FirebaseAuth.getInstance().signOut()
    redirectToLogin()
}

fun redirectToLogin() {
    NavigationManager.getNavController().navigate(Screen.Register)
}