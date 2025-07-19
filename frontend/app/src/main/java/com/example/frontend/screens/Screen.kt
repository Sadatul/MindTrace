package com.example.frontend.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Register: Screen()

    @Serializable
    data object DashBoardPatient: Screen()

    @Serializable
    data object DashboardCareGiver: Screen()
    
    @Serializable
    data object Chat: Screen()

    @Serializable
    data object MyCaregivers: Screen()

    @Serializable
    data object MyPatients: Screen()

    @Serializable
    data object PatientLogs: Screen()
}
