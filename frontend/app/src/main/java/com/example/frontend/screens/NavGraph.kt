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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.signOutUser
import com.example.frontend.screens.components.CloseAppDialog
import kotlin.system.exitProcess

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(navController: NavHostController) {
    var showCloseAppDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val startDestination by produceState<Screen?>(initialValue = null) {
        value = getStartDestination()
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    } else{
        Log.d("start"," $startDestination")
    }

    NavHost(navController = navController, startDestination = startDestination!!) {

        val navigationBar = NavigationBarComponent(
            onPatientLogs = {
                navController.navigate(Screen.PatientLogs(null))
            },
            onReminders = {
                navController.navigate(Screen.Reminder(null))
            },
            onPatientProfile = {
                navController.navigate(Screen.DashBoardPatient)
            },
            onChatScreen = {
                navController.navigate(Screen.Chat)
            },
            onCaregiverProfile = {
                navController.navigate(Screen.DashboardCareGiver)
            }
        )
        
        composable<Screen.Register> {
            ScreenRegister(
                onNavigateToDashboard = { role ->
                    val destination =
                        when (role) {
                            "PATIENT" -> Screen.PatientLogs(null)
                            "CAREGIVER" -> Screen.DashboardCareGiver
                            else -> throw IllegalArgumentException("Unknown role: $role")
                        }
                    navController.navigate(destination) {
                        popUpTo(Screen.Register) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.DashBoardPatient> {
            ScreenPatient(
                onNavigateToCaregivers = { navController.navigate(Screen.MyCaregivers) },
                onSignOut = {
                    RetrofitInstance.dementiaAPI.signOutUser(context)
                    navController.navigate(Screen.Register) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
                navigationBar = navigationBar
            )
        }

        composable<Screen.DashboardCareGiver> {
            ScreenCareGiver(
                onNavigateToPatients = { navController.navigate(Screen.MyPatients) },
                onSignOut = {
                    RetrofitInstance.dementiaAPI.signOutUser(context)
                    navController.navigate(Screen.Register) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                navigationBar = navigationBar
            )
        }
        composable<Screen.Chat> {
            ChatScreen(
                onCancelDialog = {
                    navController.popBackStack()
                },
                navigationBar = navigationBar
            )
        }

        composable<Screen.MyCaregivers> {
            ScreenMyCaregivers(
                onNavigateBack = {
                    navController.popBackStack()
                },
                navigationBar = navigationBar
            )
        }
        composable<Screen.MyPatients> {
            ScreenMyPatients(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onShowLogs = { partner ->
                    navController.navigate(Screen.PatientLogs(partner.id))
                },
                onShowReminders = {userId ->
                    navController.navigate(Screen.Reminder(userId))
                },
                navigationBar = navigationBar
            )
        }
        // Add composable for Screen.PatientLogs so it can be used as a start destination
        composable<Screen.PatientLogs> { backStackEntry ->
            val (partnerId) = backStackEntry.toRoute<Screen.PatientLogs>()
            val isPatient = partnerId == null
            ScreenMyLogs(
                onBack = { navController.popBackStack() },
                isPatient = isPatient,
                partnerId = partnerId,
                navigationBar = if (isPatient) navigationBar else navigationBar
            )
        }

        composable<Screen.Reminder> { backStackEntry ->
            val (userId) = backStackEntry.toRoute<Screen.Reminder>()

            ScreenReminder(
                userId = userId,
                navigationBar = navigationBar,
                onBack = { navController.popBackStack() }
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
    else if (userInfo.role == "PATIENT") Screen.PatientLogs(null)
    else Screen.DashboardCareGiver
}