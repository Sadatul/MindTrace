package com.example.frontend.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.frontend.MainActivity
import com.example.frontend.R
import com.example.frontend.api.DeviceRegistrationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    // Create a coroutine scope for background operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Called when a new FCM token is generated.
     * Updates the token with the backend if user is authenticated.
     * Requirements: 2.1, 2.2, 2.4, 4.1, 4.2
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "FCM token refreshed")
        
        // Check authentication state before attempting registration
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.d(TAG, "User not authenticated, skipping FCM token update until next login")
            return
        }
        
        Log.d(TAG, "User authenticated, proceeding with FCM token update")
        
        // Run token update on background thread
        serviceScope.launch {
            try {
                val deviceRegistrationManager = DeviceRegistrationManager(applicationContext)
                val success = deviceRegistrationManager.updateFCMToken(token)
                
                if (success) {
                    Log.i(TAG, "FCM token update completed successfully")
                } else {
                    Log.e(TAG, "FCM token update failed - will retry on next login")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred during FCM token update", e)
            }
        }
    }



    private fun sendNotification(messageBody: String?, title: String? = null) {
        Log.d(TAG, "Attempting to send notification: $title - $messageBody")

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val channelId:String = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_mindtrace_logo)
            .setContentTitle(title ?: "FCM Message")
            .setContentText(messageBody ?: "No message")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
            Log.d(TAG, "Notification sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification", e)
        }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.
        Log.d(TAG, "From: ${remoteMessage.from}")

        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["description"]

        if (title != null || body != null) {
            Log.d(TAG, "Data Notification Body: $body")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body, it.title)
        }
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

}
