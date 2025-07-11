package com.example.frontend.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.frontend.MainActivity
import com.example.frontend.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // Send the token to your server or handle it as needed
    }



    private fun sendNotification(messageBody: String?, title: String? = null) {
        Log.d(TAG, "Attempting to send notification: $title - $messageBody")

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val heavyVibrationPattern = longArrayOf(
            0,    // Start immediately
            500,  // Vibrate for 500ms
            200,  // Pause for 200ms
            500,  // Vibrate for 500ms
            200,  // Pause for 200ms
            800   // Final long vibration for 800ms
        )

        val channelId:String = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "FCM Message")
            .setContentText(messageBody ?: "No message")
            .setAutoCancel(true)
            .setVibrate(heavyVibrationPattern)
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
            sendNotification(body, title)
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
