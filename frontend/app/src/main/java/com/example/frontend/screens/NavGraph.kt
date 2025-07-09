package com.example.frontend.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.signOutUser
import com.example.frontend.screens.components.CloseAppDialog
import kotlin.system.exitProcess

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
    }else{
        Log.d("start"," $startDestination")
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
                            "PATIENT" -> Screen.PatientLogs // Changed from DashBoardPatient
                            "CAREGIVER" -> Screen.DashboardCareGiver
                            else -> throw IllegalArgumentException("Unknown role: $role")
                        }
                    navController.navigate(destination) {
                        popUpTo(Screen.Main) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.DashBoardPatient> {
            ScreenPatient(
                onNavigateToChat = { navController.navigate(Screen.Chat) },
                onNavigateToCaregivers = { navController.navigate(Screen.MyCaregivers) },
                onSignOut = {
                    RetrofitInstance.dementiaAPI.signOutUser()
                    showCloseAppDialog = true
                },
                onLoginWithAnotherAccount = {
                    // Simply navigate to main registration screen
                    navController.navigate(Screen.Register) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            ) { navController.popBackStack() }
        }
        composable<Screen.DashboardCareGiver> {
            ScreenCareGiver(
                onNavigateToChat = { navController.navigate(Screen.Chat) },
                onNavigateToPatients = { navController.navigate(Screen.MyPatients) },
                onSignOut = {
                    RetrofitInstance.dementiaAPI.signOutUser()
                    showCloseAppDialog = true
                },
                onLoginWithAnotherAccount = {
                    // Simply navigate to main registration screen
                    navController.navigate(Screen.Register) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<Screen.Chat> {
            ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCancelDialog = {
                    navController.popBackStack()
                }
            )
        }
        composable<Screen.MyCaregivers> {
            ScreenMyCaregivers(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<Screen.MyPatients> {
            ScreenMyPatients(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onShowLogs = { partner ->
                    navController.navigate("patient_logs?partnerId=${partner.id}")
                }
            )
        }
        // Add composable for Screen.PatientLogs so it can be used as a start destination
        composable<Screen.PatientLogs> {
            ScreenMyLogs(
                onBack = { navController.popBackStack() },
                onAskAi = { navController.navigate(Screen.Chat) },
                onMyProfile = { navController.navigate(Screen.DashBoardPatient) },
                isPatient = true,
                partnerId = null
            )
        }
        composable(
            route = "patient_logs?partnerId={partnerId}",
            arguments = listOf(
                navArgument("partnerId") {
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId")
            ScreenMyLogs(
                onBack = { navController.popBackStack() },
                onAskAi = { navController.navigate(Screen.Chat) },
                onMyProfile = { navController.navigate(Screen.DashBoardPatient) },
                isPatient = partnerId == null,
                partnerId = partnerId
            )
        }
    }

    if (showCloseAppDialog) {
        CloseAppDialog(
            onDismiss = { showCloseAppDialog = false },
            onConfirmClose = { exitProcess(0) }
        )
    }
}

suspend fun getStartDestination(): Screen {
    val userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo(autoRedirect = false)
    return if (userInfo == null) Screen.Register
    else if (userInfo.role == "PATIENT") Screen.PatientLogs // Changed from DashBoardPatient
    else Screen.DashboardCareGiver
}