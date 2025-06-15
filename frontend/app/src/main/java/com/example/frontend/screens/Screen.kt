package com.example.frontend.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Main: Screen()

    @Serializable
    data object Register: Screen()

    @Serializable
    data class DashBoardPatient(
        val name: String,
        val email: String,
        val dob: String,
        val gender: String
    ): Screen()

    @Serializable
    data class DashboardCareGiver(
        val name: String,
        val email: String,
        val dob: String,
        val gender: String,
        val uid: String,
        val token: String
    ): Screen()

    @Serializable
<<<<<<< HEAD
    object Chat: Screen()
}
=======
    data object Dashboard: Screen()
    @Serializable
    data class Chat(val token: String): Screen()
}
>>>>>>> main
