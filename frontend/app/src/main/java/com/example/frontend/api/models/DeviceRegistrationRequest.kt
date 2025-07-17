package com.example.frontend.api.models

data class DeviceRegistrationRequest(
    val token: String,        // FCM token
    val deviceId: String,     // Unique device identifier
    val deviceName: String    // Human-readable device name
)