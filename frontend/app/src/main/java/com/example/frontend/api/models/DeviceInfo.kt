package com.example.frontend.api.models

data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val lastTokenUpdate: Long
)