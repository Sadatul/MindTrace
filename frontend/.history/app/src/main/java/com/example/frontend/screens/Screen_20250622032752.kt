package com.example.frontend.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Main: Screen()    @Serializable
    data class Register(val autoTriggerSignIn: Boolean = false): Screen()

    @Serializable
    data object DashBoardPatient: Screen()

    @Serializable
    data object DashboardCareGiver: Screen()

    @Serializable
    data object Chat: Screen()
}
