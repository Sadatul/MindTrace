package com.example.frontend.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Main: Screen()
    @Serializable
    data object RegisterCaregiver: Screen()
    @Serializable
    data object GetOTP: Screen()
    @Serializable
    data object RegisterPatient: Screen()
    @Serializable
    data object Dashboard: Screen()
    @Serializable
    data class Chat(val token: String): Screen()
}