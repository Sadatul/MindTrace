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
                toRegisterCaregiverScreen = {
                    navController.navigate(Screen.RegisterCaregiver)
                },

                toChatScreen = {
                   navController.navigate(Screen.Chat)
                }
            )
        }

        composable<Screen.RegisterCaregiver> {
            RegisterCaregiverScreen()
        }

        composable<Screen.GetOTP> {
            GetOTPScreen()
        }

        composable<Screen.RegisterPatient> {
            RegisterPatientScreen()
        }

        composable<Screen.Dashboard> {
            DashboardScreen()
        }

        composable<Screen.Chat> {
            ChatScreen()
        }
    }
}