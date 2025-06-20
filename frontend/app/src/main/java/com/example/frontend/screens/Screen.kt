package com.example.frontend.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Main: Screen()

    @Serializable
    data object Register: Screen()

    @Serializable
    data object DashBoardPatient: Screen()

    @Serializable
    data object DashboardCareGiver: Screen()

    @Serializable
    data object Chat: Screen()
}
