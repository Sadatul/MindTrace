package com.example.frontend.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Main) {
        composable<Screen.Main> {
            MainScreen(
                toRegisterScreen = {
                    navController.navigate(Screen.Register)
                },
                toChatScreen = {
                    navController.navigate(Screen.Chat)
                }
            )
        }

        composable<Screen.Register> {
            ScreenRegister(
                onNavigateToDashboard = { role ->
                    val destination = if (role == "patient") Screen.DashBoardPatient else Screen.DashboardCareGiver
                    navController.navigate(destination) {
                        popUpTo(Screen.Main) { inclusive = true }
                    }
                }
            )
        }


        composable<Screen.DashBoardPatient> {
            ScreenPatient(onNavigateToChat = { navController.navigate(Screen.Chat) })
        }

        composable<Screen.DashboardCareGiver> {
            ScreenCareGiver()
        }

        composable<Screen.Chat> {
            ScreenChatScreen()
        }
    }
}
