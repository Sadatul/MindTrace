package com.example.frontend.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getSelfUserInfo

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(navController: NavHostController) {
    var showCloseAppDialog by remember { mutableStateOf(false) }
    
    val startDestination by produceState<Screen?>(initialValue = null) {
        value = getStartDestination()
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = startDestination!!) {
        composable<Screen.Main> {
            MainScreen(
                toRegisterCaregiverScreen = { navController.navigate(Screen.Register) },
                toChatScreen = { navController.navigate(Screen.Chat) }
            )
        }

        composable<Screen.Register> {
            ScreenRegister(
                onNavigateToDashboard = { role ->
                    val destination =
                        when (role) {
                            "PATIENT" -> {
                                Screen.DashBoardPatient
                            }

                            "CAREGIVER" -> {
                                Screen.DashboardCareGiver
                            }

                            else -> {
                                throw IllegalArgumentException("Unknown role: $role")
                            }
                        }
                    navController.navigate(destination) {
                        popUpTo(Screen.Main) { inclusive = true }
                    }
                }
            )
        }

        // Inside composable<Screen.DashBoardPatient>
        composable<Screen.DashBoardPatient> {
            ScreenPatient(
                onNavigateToChat = { navController.navigate(Screen.Chat) },
                onLoginWithAnotherAccount = {
                    navController.navigate(Screen.Main) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Inside composable<Screen.DashboardCareGiver>
        composable<Screen.DashboardCareGiver> {
            ScreenCareGiver(
                onNavigateToChat = { navController.navigate(Screen.Chat) },
                onLoginWithAnotherAccount = {
                    navController.navigate(Screen.Main) {
                        popUpTo(navController.graph.startDestinationId) { // Or a specific screen like Screen.Main
                            inclusive = true
                        }
                        launchSingleTop = true // Optional: Avoid multiple copies of the login screen
                    }
                }
            )
        }
        composable<Screen.Chat> {
            ChatScreen()
        }
    }
}

suspend fun getStartDestination(): Screen {
    val userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo(autoRedirect = false)
    return if (userInfo == null) Screen.Register
    else if (userInfo.role == "PATIENT") Screen.DashBoardPatient
    else Screen.DashboardCareGiver
}