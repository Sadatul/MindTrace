package com.example.frontend.api

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.frontend.api.models.PatientLog
import com.example.frontend.api.models.PartnerInfo
import com.example.frontend.api.models.PatientLogRaw
import com.example.frontend.api.models.RequestChat
import com.example.frontend.api.models.RequestStoreLog
import com.example.frontend.api.models.RequestStoreLogRaw
import com.example.frontend.api.models.RequestUpdateLog
import com.example.frontend.api.models.RequestUpdateLogRaw
import com.example.frontend.api.models.ResponseChat
import com.example.frontend.api.models.ResponseLogMetadata
import com.example.frontend.api.models.ResponseLogsMetadataRaw
import com.example.frontend.screens.NavigationManager
import com.example.frontend.screens.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

data class OtpResponse(val otp: String?)

data class PrimaryContact(
    val id: String,
    val name: String,
    val gender: String,
    val profilePicture: String?,
    val createdAt: String
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

data class RequestPatientAdd(
    val patientId: String,
    val otp: String
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


    @GET("/v1/users/caregivers")
    suspend fun getCaregiversWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Query("includeDeleted") includeDeleted: Boolean = false
    ): Response<List<PartnerInfo>>

    @GET("/v1/caregivers/patients")
    suspend fun getPatientsWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Query("includeDeleted") includeDeleted: Boolean = false
    ): Response<List<PartnerInfo>>


    @GET("/v1/users/caregivers/{caregiverId}/otp")
    suspend fun sendCaregiverRemovalOTPWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("caregiverId") caregiverId: String
    ): Response<ResponseBody>


    @DELETE("/v1/users/caregivers/{caregiverId}")
    suspend fun deleteCaregiverWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("caregiverId") caregiverId: String,
        @Query("otp") otp: String
    ): Response<ResponseBody>


    @GET("/v1/caregivers/patients/{id}/otp")
    suspend fun sendPatientAddOTPWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("id") patientId: String
    ): Response<ResponseBody>

    @POST("/v1/caregivers/patients")
    suspend fun addPatientWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Body requestPatientAdd: RequestPatientAdd
    ): Response<ResponseBody>

    @DELETE("/v1/caregivers/patients/{id}")
    suspend fun removePatientWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("id") patientId: String
    ): Response<ResponseBody>

    @POST("/v1/logs")
    suspend fun storeLogWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Body requestStoreLogRaw: RequestStoreLogRaw
    ): Response<ResponseBody>


    @GET("/v1/logs")
    suspend fun getLogsWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Query("userId") userId: String? = null,
        @Query("start") start: String?,
        @Query("end") end: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ResponseLogsMetadataRaw>

    @GET("/v1/logs/{id}")
    suspend fun getLogWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("id") id: String
    ): Response<PatientLogRaw>

    @DELETE("/v1/logs/{id}")
    suspend fun deleteLogWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @PUT("/v1/logs/{id}")
    suspend fun updateLogWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("id") id: String,
        @Body updateLogRaw: RequestUpdateLogRaw
    ): Response<ResponseBody>
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

suspend fun DementiaAPI.getFCMToken(): String? = suspendCoroutine { cont ->
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Unknown FCM error"))
            }
        }
}

suspend fun DementiaAPI.getIdToken(
    forceRefresh: Boolean = false,
    autoRedirect: Boolean = true
): String? {
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


suspend fun DementiaAPI.getPartners(includeDeleted: Boolean = false): List<PartnerInfo> {
    val token: String = getIdToken() ?: return listOf()
    val userInfo = getSelfUserInfo() ?: return listOf()

    return if (userInfo.role == "CAREGIVER") {
        getPatientsWithAuth(firebaseIdToken = "Bearer $token", includeDeleted).body() ?: listOf()
    } else {
        getCaregiversWithAuth(firebaseIdToken = "Bearer $token", includeDeleted).body() ?: listOf()
    }
}

suspend fun DementiaAPI.sendCaregiverRemovalOTP(caregiverId: String): Boolean {
    val token = getIdToken() ?: return false
    val response = sendCaregiverRemovalOTPWithAuth(firebaseIdToken = "Bearer $token", caregiverId)
    return response.isSuccessful
}

suspend fun DementiaAPI.deleteCaregiver(caregiverId: String, otp: String): Boolean {
    val token = getIdToken() ?: return false
    val response = deleteCaregiverWithAuth(firebaseIdToken = "Bearer $token", caregiverId, otp)
    return response.isSuccessful
}

suspend fun DementiaAPI.sendPatientAddOTP(patientId: String): Boolean {
    val token = getIdToken() ?: return false
    val response = sendPatientAddOTPWithAuth(firebaseIdToken = "Bearer $token", patientId)
    return response.isSuccessful
}

suspend fun DementiaAPI.addPatient(requestPatientAdd: RequestPatientAdd): Boolean {
    val token = getIdToken() ?: return false
    val response = addPatientWithAuth(firebaseIdToken = "Bearer $token", requestPatientAdd)
    return response.isSuccessful
}

suspend fun DementiaAPI.removePatient(patientId: String): Boolean {
    val token = getIdToken() ?: return false
    val response = removePatientWithAuth(firebaseIdToken = "Bearer $token", patientId)
    return response.isSuccessful
}


suspend fun DementiaAPI.storeLog(log: RequestStoreLog): Boolean {
    val token = getIdToken() ?: return false
    val response = storeLogWithAuth(firebaseIdToken = "Bearer $token", log.toRaw())
    return response.isSuccessful
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun DementiaAPI.getLogs(userId: String?, start: String?, end: String?, page: Int, size: Int): ResponseLogMetadata? {
    val token = getIdToken() ?: return null
    val logs = getLogsWithAuth(
        firebaseIdToken = "Bearer $token",
        userId,
        if (start != null) convertZonedToUtc(start) else null,
        if (end != null) convertZonedToUtc(end) else null,
        page,
        size
    ).body()
        ?: return null
    return logs.toWrapper()
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun DementiaAPI.getLog(id: String): PatientLog? {
    val token = getIdToken() ?: return null
    val logRaw = getLogWithAuth(firebaseIdToken = "Bearer $token", id).body() ?: return null
    return logRaw.toWrapper()
}

suspend fun DementiaAPI.deleteLog(id: String): Boolean {
    val token = getIdToken() ?: return false
    val response = deleteLogWithAuth(firebaseIdToken = "Bearer $token", id)
    return response.isSuccessful
}

suspend fun DementiaAPI.updateLog(id: String, updateLog: RequestUpdateLog): Boolean {
    val token = getIdToken() ?: return false
    val response = updateLogWithAuth(firebaseIdToken = "Bearer $token", id, updateLog.toRaw())
    return response.isSuccessful
}

fun redirectToLogin() {
    NavigationManager.getNavController().navigate(Screen.Register)
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertUtcToLocal(utcTimestamp: String): String {
    return try {
        val instant = Instant.parse(utcTimestamp)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        zonedDateTime.toString() // returns ISO 8601 with local time zone
    } catch (_: Exception) {
        utcTimestamp
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertZonedToUtc(zonedTimestamp: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(zonedTimestamp)
        val instant = zonedDateTime.toInstant()
        instant.toString() // UTC timestamp in ISO 8601 format
    } catch (_: Exception) {
        zonedTimestamp
    }
}