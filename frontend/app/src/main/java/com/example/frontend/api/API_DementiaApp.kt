package com.example.frontend.api

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.frontend.api.models.DeviceRegistrationRequest
import com.example.frontend.api.models.PatientLog
import com.example.frontend.api.models.PartnerInfo
import com.example.frontend.api.models.PatientLogRaw
import com.example.frontend.api.models.Reminder
import com.example.frontend.api.models.ReminderRaw
import com.example.frontend.api.models.RequestChat
import com.example.frontend.api.models.RequestStoreLog
import com.example.frontend.api.models.RequestStoreLogRaw
import com.example.frontend.api.models.RequestStoreReminder
import com.example.frontend.api.models.RequestStoreReminderRaw
import com.example.frontend.api.models.RequestUpdateLog
import com.example.frontend.api.models.RequestUpdateLogRaw
import com.example.frontend.api.models.ResponseChat
import com.example.frontend.api.models.ResponseGetReminders
import com.example.frontend.api.models.ResponseGetRemindersRaw
import com.example.frontend.api.models.ResponseLogMetadata
import com.example.frontend.api.models.ResponseLogsMetadataRaw
import com.example.frontend.screens.NavigationManager
import com.example.frontend.screens.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
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

data class TelegramUUIDResponse(
    val value: String
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

    @POST("/v1/auth/register/device")
    suspend fun registerDevice(
        @Header("Authorization") bearerToken: String,
        @Body request: DeviceRegistrationRequest
    ): Response<ResponseBody>
    
    @DELETE("/v1/auth/logout")
    suspend fun logoutDevice(
        @Header("Authorization") bearerToken: String,
        @Query("deviceId") deviceId: String
    ): Response<ResponseBody>

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

    @GET("/v1/telegram/register")
    suspend fun getTelegramUUID(
        @Header("Authorization") firebaseIdToken: String
    ): Response<TelegramUUIDResponse> 

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

    @POST("/v1/reminders")
    suspend fun storeReminderWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Body requestStoreReminderRaw: RequestStoreReminderRaw
    ): Response<ResponseBody>

    @GET("/v1/reminders")
    suspend fun getRemindersWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Query("userId") userId: String?,
        @Query("start") start: String?,
        @Query("end") end: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ResponseGetRemindersRaw>?

    @GET("/v1/reminders/{id}")
    suspend fun getReminderWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("id") id: String
    ): Response<ReminderRaw>?

    @DELETE("/v1/reminders/{id}")
    suspend fun deleteReminderWithAuth(
        @Header("Authorization") firebaseIdToken: String,
        @Path("id") id: String
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

/**
 * Extension function to deregister a device from the backend during logout.
 * Sends a DELETE request to the logout endpoint with the device ID.
 * 
 * @param deviceId The unique device identifier to deregister
 * @return Boolean indicating success (true) or failure (false)
 */
suspend fun DementiaAPI.deregisterDevice(deviceId: String): Boolean {
    Log.d("DementiaAPI", "Deregistering device during logout - DeviceId: $deviceId")
    
    return try {
        // Get authentication token with fallback handling
        val authToken = getIdToken(forceRefresh = false, autoRedirect = false)
        if (authToken == null) {
            Log.e("DementiaAPI", "Failed to get authentication token for device deregistration")
            return false
        }
        
        Log.d("DementiaAPI", "Making device logout API call")
        
        // Execute API call
        val response = logoutDevice("Bearer $authToken", deviceId)
        
        // Handle response
        if (response.isSuccessful) {
            Log.i("DementiaAPI", "Device deregistration successful - DeviceId: $deviceId")
            true
        } else {
            val errorBody = response.errorBody()?.string()
            val statusCode = response.code()
            Log.e("DementiaAPI", "Device deregistration failed - Status: $statusCode, Error: $errorBody, DeviceId: $deviceId")
            
            // Log specific error types for troubleshooting
            when (statusCode) {
                401 -> Log.e("DementiaAPI", "Authentication failed during device deregistration")
                403 -> Log.e("DementiaAPI", "Authorization denied for device deregistration")
                404 -> Log.e("DementiaAPI", "Device not found during deregistration")
                500 -> Log.e("DementiaAPI", "Server error during device deregistration")
                else -> Log.e("DementiaAPI", "Unexpected error during device deregistration - Status: $statusCode")
            }
            
            false
        }
    } catch (e: Exception) {
        Log.e("DementiaAPI", "Exception during device deregistration - DeviceId: $deviceId", e)
        
        // Log specific exception types for better troubleshooting
        when (e) {
            is java.net.UnknownHostException -> Log.e("DementiaAPI", "Network connectivity issue during device deregistration")
            is java.net.SocketTimeoutException -> Log.e("DementiaAPI", "Request timeout during device deregistration")
            is javax.net.ssl.SSLException -> Log.e("DementiaAPI", "SSL/TLS error during device deregistration")
            else -> Log.e("DementiaAPI", "Unexpected exception type during device deregistration: ${e.javaClass.simpleName}")
        }
        
        false
    }
}

/**
 * Signs out the current user and deregisters the device from the backend.
 * This is a non-suspending function that launches a coroutine for the deregistration.
 * 
 * @param context The application context needed to access device information
 */
fun DementiaAPI.signOutUser(context: Context) {
    // Get the device ID before signing out
    val deviceRegistrationManager = DeviceRegistrationManager(context)
    val deviceId = deviceRegistrationManager.getOrCreateDeviceId()
    
    // Launch a coroutine to deregister the device
    kotlinx.coroutines.MainScope().launch {
        try {
            Log.d("DementiaAPI", "Attempting to deregister device before sign out - DeviceId: $deviceId")
            val success = deregisterDevice(deviceId)
            
            if (success) {
                Log.i("DementiaAPI", "Device successfully deregistered during sign out")
            } else {
                Log.e("DementiaAPI", "Failed to deregister device during sign out")
            }
        } catch (e: Exception) {
            Log.e("DementiaAPI", "Exception during device deregistration on sign out", e)
        } finally {
            // Clear local device info
            deviceRegistrationManager.clearDeviceInfo()
            
            // Proceed with sign out regardless of deregistration result
            SelfUserInfoCache.signOutUser()
            FirebaseAuth.getInstance().signOut()
            redirectToLogin()
        }
    }
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

@RequiresApi(Build.VERSION_CODES.O)
suspend fun DementiaAPI.storeReminder(requestStoreReminder: RequestStoreReminder): Boolean {
    val token = getIdToken() ?: return false
    Log.d("SCREEN_REMINDER", token)
    Log.d("SCREEN_REMINDER", requestStoreReminder.toRaw().toString())
    val response =
        storeReminderWithAuth(firebaseIdToken = "Bearer $token", requestStoreReminder.toRaw())
    return response.isSuccessful
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun DementiaAPI.getReminders(
    userId: String?,
    start: String?,
    end: String?,
    page: Int,
    size: Int
): ResponseGetReminders? {
    val token = getIdToken() ?: return null
    val response =
        getRemindersWithAuth(firebaseIdToken = "Bearer $token", userId, start, end, page, size)
            ?: return null
    val rawResponse = response.body() ?: return null
    return rawResponse.toWrapper()
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun DementiaAPI.getReminder(id: String): Reminder? {
    val token = getIdToken() ?: return null
    val response = getReminderWithAuth(firebaseIdToken = "Bearer $token", id) ?: return null
    val reminder = response.body() ?: return null
    return reminder.toWrapper()
}

suspend fun DementiaAPI.deleteReminder(id: String): Boolean {
    val token = getIdToken() ?: return false
    val response = deleteReminderWithAuth(firebaseIdToken = "Bearer $token", id)
    return response.isSuccessful
}

/**
 * Extension function to register device FCM token with the backend API.
 * Handles authentication token retrieval and API call execution with proper error handling.
 * 
 * @param token FCM token to register
 * @param deviceId Unique device identifier
 * @param deviceName Human-readable device name
 * @return Boolean indicating success (true) or failure (false)
 * 
 * Requirements: 1.3, 2.3, 4.4, 4.5
 */
suspend fun DementiaAPI.registerDeviceToken(
    token: String,
    deviceId: String,
    deviceName: String
): Boolean {
    Log.d("DementiaAPI", "Starting device token registration - DeviceId: $deviceId, DeviceName: $deviceName")
    
    return try {
        // Get authentication token with fallback handling
        val authToken = getIdToken(forceRefresh = false, autoRedirect = false)
        if (authToken == null) {
            Log.e("DementiaAPI", "Failed to get authentication token for device registration")
            return false
        }
        
        // Create device registration request
        val request = DeviceRegistrationRequest(
            token = token,
            deviceId = deviceId,
            deviceName = deviceName
        )
        
        Log.d("DementiaAPI", "Making device registration API call")
        
        // Execute API call
        val response = registerDevice("Bearer $authToken", request)
        
        // Handle response
        if (response.isSuccessful) {
            Log.i("DementiaAPI", "Device token registration successful - DeviceId: $deviceId")
            true
        } else {
            val errorBody = response.errorBody()?.string()
            val statusCode = response.code()
            Log.e("DementiaAPI", "Device token registration failed - Status: $statusCode, Error: $errorBody, DeviceId: $deviceId")
            
            // Log specific error types for troubleshooting
            when (statusCode) {
                401 -> Log.e("DementiaAPI", "Authentication failed during device registration")
                403 -> Log.e("DementiaAPI", "Authorization denied for device registration")
                400 -> Log.e("DementiaAPI", "Bad request during device registration - check request format")
                500 -> Log.e("DementiaAPI", "Server error during device registration")
                else -> Log.e("DementiaAPI", "Unexpected error during device registration - Status: $statusCode")
            }
            
            false
        }
    } catch (e: Exception) {
        Log.e("DementiaAPI", "Exception during device token registration - DeviceId: $deviceId", e)
        
        // Log specific exception types for better troubleshooting
        when (e) {
            is java.net.UnknownHostException -> Log.e("DementiaAPI", "Network connectivity issue during device registration")
            is java.net.SocketTimeoutException -> Log.e("DementiaAPI", "Request timeout during device registration")
            is javax.net.ssl.SSLException -> Log.e("DementiaAPI", "SSL/TLS error during device registration")
            else -> Log.e("DementiaAPI", "Unexpected exception type during device registration: ${e.javaClass.simpleName}")
        }
        
        false
    }
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