package com.example.frontend.api

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.frontend.api.models.DeviceRegistrationRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import kotlin.math.min
import kotlin.math.pow

/**
 * Manages device registration and FCM token operations.
 * Handles device identification, token registration with backend, and secure storage.
 */
class DeviceRegistrationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "DeviceRegistrationManager"
        private const val PREFS_FILE_NAME = "device_registration_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_LAST_TOKEN_UPDATE = "last_token_update"
        private const val DEVICE_ID_SALT = "dementia_app_device_salt_2024"
    }

    private val encryptedSharedPreferences by lazy {
        createEncryptedSharedPreferences()
    }

    /**
     * Creates encrypted SharedPreferences for secure device info storage
     */
    private fun createEncryptedSharedPreferences() = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create encrypted SharedPreferences, falling back to regular SharedPreferences", e)
        // Fallback to regular SharedPreferences if encryption fails
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Gets or creates a unique device ID that persists across app sessions.
     * Uses Android ID as base combined with app-specific salt for uniqueness.
     * Generates new ID only on fresh installs.
     * 
     * Requirements: 3.1, 3.2, 3.3
     */
    fun getOrCreateDeviceId(): String {
        // Check if device ID already exists
        val existingDeviceId = encryptedSharedPreferences.getString(KEY_DEVICE_ID, null)
        if (!existingDeviceId.isNullOrEmpty()) {
            Log.d(TAG, "Using existing device ID")
            return existingDeviceId
        }

        // Generate new device ID
        val deviceId = generateUniqueDeviceId()
        
        // Store the device ID securely
        encryptedSharedPreferences.edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .apply()
        
        Log.d(TAG, "Generated new device ID")
        return deviceId
    }

    /**
     * Generates a unique device ID using Android ID and app-specific salt
     */
    private fun generateUniqueDeviceId(): String {
        return try {
            // Get Android ID
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown_device"

            // Combine with salt and hash for uniqueness
            val combined = "$androidId$DEVICE_ID_SALT"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(combined.toByteArray())
            
            // Convert to hex string and take first 16 characters
            hashBytes.joinToString("") { "%02x".format(it) }.take(16)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to generate device ID from Android ID, using UUID fallback", e)
            // Fallback to UUID if Android ID is not available
            UUID.randomUUID().toString().replace("-", "").take(16)
        }
    }

    /**
     * Generates a human-readable device name based on device information.
     * Uses Build.MODEL and Build.MANUFACTURER with fallback naming scheme.
     * 
     * Requirements: 3.4, 3.5
     */
    fun generateDeviceName(): String {
        // Check if device name already exists and is still valid
        val existingDeviceName = encryptedSharedPreferences.getString(KEY_DEVICE_NAME, null)
        if (!existingDeviceName.isNullOrEmpty()) {
            Log.d(TAG, "Using existing device name: $existingDeviceName")
            return existingDeviceName
        }

        // Generate new device name
        val deviceName = createDeviceName()
        
        // Store the device name
        encryptedSharedPreferences.edit()
            .putString(KEY_DEVICE_NAME, deviceName)
            .apply()
        
        Log.d(TAG, "Generated device name: $deviceName")
        return deviceName
    }

    /**
     * Creates a human-readable device name from device information
     */
    private fun createDeviceName(): String {
        return try {
            val manufacturer = Build.MANUFACTURER?.takeIf { it.isNotBlank() } ?: "Unknown"
            val model = Build.MODEL?.takeIf { it.isNotBlank() } ?: "Device"
            
            // Capitalize manufacturer name
            val capitalizedManufacturer = manufacturer.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            
            // Clean up model name (remove manufacturer name if it's already included)
            val cleanModel = if (model.lowercase().contains(manufacturer.lowercase())) {
                model
            } else {
                model
            }
            
            "$capitalizedManufacturer $cleanModel"
        } catch (e: Exception) {
            Log.w(TAG, "Failed to generate device name from Build info, using fallback", e)
            "Android Device"
        }
    }

    /**
     * Updates the last token update timestamp
     */
    private fun updateLastTokenUpdateTime() {
        encryptedSharedPreferences.edit()
            .putLong(KEY_LAST_TOKEN_UPDATE, System.currentTimeMillis())
            .apply()
    }

    /**
     * Gets the last token update timestamp
     */
    fun getLastTokenUpdateTime(): Long {
        return encryptedSharedPreferences.getLong(KEY_LAST_TOKEN_UPDATE, 0L)
    }

    /**
     * Clears all stored device information (useful for logout/reset scenarios)
     */
    fun clearDeviceInfo() {
        encryptedSharedPreferences.edit()
            .remove(KEY_DEVICE_ID)
            .remove(KEY_DEVICE_NAME)
            .remove(KEY_LAST_TOKEN_UPDATE)
            .apply()
        Log.d(TAG, "Cleared device information")
    }

    /**
     * Coordinates full device registration flow during login.
     * Gets FCM token, device info, and registers with backend.
     * 
     * Requirements: 1.1, 1.2, 4.1
     */
    suspend fun registerDeviceOnLogin(): Boolean {
        Log.d(TAG, "Starting device registration on login")
        
        return try {
            // Get FCM token
            val fcmToken = getFCMToken()
            if (fcmToken == null) {
                Log.e(TAG, "Failed to get FCM token during login registration")
                return false
            }
            
            // Get device information
            val deviceId = getOrCreateDeviceId()
            val deviceName = generateDeviceName()
            
            Log.d(TAG, "Registering device on login - DeviceId: $deviceId, DeviceName: $deviceName")
            
            // Register with backend
            val success = registerWithBackend(fcmToken, deviceId, deviceName)
            
            if (success) {
                updateLastTokenUpdateTime()
                Log.i(TAG, "Device registration on login completed successfully")
            } else {
                Log.e(TAG, "Device registration on login failed")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception during device registration on login", e)
            false
        }
    }

    /**
     * Updates FCM token with backend for token refresh scenarios.
     * Uses existing device ID and name for consistency.
     * 
     * Requirements: 2.1, 2.2, 4.1, 4.2
     */
    suspend fun updateFCMToken(newToken: String): Boolean {
        Log.d(TAG, "Starting FCM token update")
        
        return try {
            // Get existing device information
            val deviceId = getOrCreateDeviceId()
            val deviceName = generateDeviceName()
            
            Log.d(TAG, "Updating FCM token - DeviceId: $deviceId, DeviceName: $deviceName")
            
            // Register updated token with backend
            val success = registerWithBackend(newToken, deviceId, deviceName)
            
            if (success) {
                updateLastTokenUpdateTime()
                Log.i(TAG, "FCM token update completed successfully")
            } else {
                Log.e(TAG, "FCM token update failed")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception during FCM token update", e)
            false
        }
    }

    /**
     * Registers device with backend API with retry logic and error handling.
     * Implements exponential backoff retry strategy with maximum 3 attempts.
     * 
     * Requirements: 4.2, 4.3
     */
    private suspend fun registerWithBackend(token: String, deviceId: String, deviceName: String): Boolean {
        val maxRetries = 3
        var attempt = 0
        
        while (attempt < maxRetries) {
            attempt++
            Log.d(TAG, "Device registration attempt $attempt/$maxRetries")
            
            try {
                // Get authentication token
                val authToken = getIdTokenWithFallback(forceRefresh = false) {
                    Log.w(TAG, "Authentication failed during device registration")
                }
                
                if (authToken == null) {
                    Log.e(TAG, "No authentication token available for device registration")
                    return false
                }
                
                // Create API request
                val request = DeviceRegistrationRequest(
                    token = token,
                    deviceId = deviceId,
                    deviceName = deviceName
                )
                
                // Make API call
                val api = RetrofitInstance.dementiaAPI
                val response = api.registerDevice("Bearer $authToken", request)
                
                if (response.isSuccessful) {
                    Log.i(TAG, "Device registration successful on attempt $attempt")
                    return true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.w(TAG, "Device registration failed on attempt $attempt. Status: ${response.code()}, Error: $errorBody")
                    
                    // Don't retry on authentication errors (4xx)
                    if (response.code() in 400..499) {
                        Log.e(TAG, "Authentication error during device registration, not retrying")
                        return false
                    }
                }
                
            } catch (e: Exception) {
                Log.w(TAG, "Exception during device registration attempt $attempt", e)
            }
            
            // Apply exponential backoff if not the last attempt
            if (attempt < maxRetries) {
                val delayMs = calculateBackoffDelay(attempt)
                Log.d(TAG, "Waiting ${delayMs}ms before retry attempt ${attempt + 1}")
                delay(delayMs)
            }
        }
        
        Log.e(TAG, "Device registration failed after $maxRetries attempts")
        return false
    }

    /**
     * Calculates exponential backoff delay with jitter.
     * Initial delay: 1 second, max delay: 30 seconds
     */
    private fun calculateBackoffDelay(attempt: Int): Long {
        val baseDelayMs = 1000L // 1 second
        val maxDelayMs = 30000L // 30 seconds
        
        // Exponential backoff: 2^(attempt-1) * baseDelay
        val exponentialDelay = (2.0.pow(attempt - 1) * baseDelayMs).toLong()
        
        // Cap at maximum delay
        val cappedDelay = min(exponentialDelay, maxDelayMs)
        
        // Add jitter (±25% randomization)
        val jitter = (cappedDelay * 0.25 * (Math.random() - 0.5)).toLong()
        
        return cappedDelay + jitter
    }

    /**
     * Gets FCM token from Firebase Messaging
     */
    private suspend fun getFCMToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }
}