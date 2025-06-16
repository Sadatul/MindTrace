package com.example.frontend.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Main) {
        composable<Screen.Main> {
            MainScreen(
                    toRegisterCaregiverScreen = { navController.navigate(Screen.Register) },
                    toChatScreen = { navController.navigate(Screen.Chat) }
            )
        }

        composable<Screen.Register> {
            ScreenRegister(
                    onNavigateToDashboard = { role, name, email, dob, gender, uid, token ->
                        val destination =
                                if (role == "patient") {
                                    Screen.DashBoardPatient(
                                            name = name ?: "N/A",
                                            email = email ?: "N/A",
                                            dob = dob ?: "N/A",
                                            gender = gender ?: "N/A",
                                    )
                                } else {
                                    Screen.DashboardCareGiver(
                                            name = name ?: "N/A",
                                            email = email ?: "N/A",
                                            dob = dob ?: "N/A",
                                            gender = gender ?: "N/A",
                                            uid = uid ?: "N/A",
                                            token = token ?: "N/A"
                                    )
                                }
                        navController.navigate(destination) {
                            popUpTo(Screen.Main) { inclusive = true }
                        }
                    }
            )
        }

        composable<Screen.DashBoardPatient> { backStackEntry ->
            val patientDetails = backStackEntry.toRoute<Screen.DashBoardPatient>()
            ScreenPatient(
                    patientDetails = patientDetails,
                    onNavigateToChat = { navController.navigate(Screen.Chat) }
            )
        }

        composable<Screen.DashboardCareGiver> { backStackEntry ->
            val caregiverDetails = backStackEntry.toRoute<Screen.DashboardCareGiver>()
            ScreenCareGiver(
                    caregiverDetails = caregiverDetails,
                    onNavigateToChat = { navController.navigate(Screen.Chat) }
            )
        }

        composable<Screen.Chat> {
            ChatScreen()
        }
    }
}
