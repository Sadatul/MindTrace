package com.example.frontend.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.signOutUser
import com.example.frontend.screens.components.CloseAppDialog
import com.example.frontend.screens.components.NewAccountDialog
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(navController: NavHostController) {
    var showCloseAppDialog by remember { mutableStateOf(false) }
    var showNewAccountDialog by remember { mutableStateOf(false) }
    val startDestination by produceState<Screen?>(initialValue = null) {
        value = getStartDestination()
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = startDestination!!) {        composable<Screen.Main> {
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
                            "PATIENT" -> Screen.DashBoardPatient
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
                onSignOut = {
                    RetrofitInstance.dementiaAPI.signOutUser()
                    showCloseAppDialog = true
                },
                onLoginWithAnotherAccount = {
                    // Show dialog first, then handle sign out and navigation on confirmation
                    showNewAccountDialog = true
                }
            )
        }
        composable<Screen.DashboardCareGiver> {
            ScreenCareGiver(
                onNavigateToChat = { navController.navigate(Screen.Chat) },
                onSignOut = {
                    RetrofitInstance.dementiaAPI.signOutUser()
                    showCloseAppDialog = true
                },
                onLoginWithAnotherAccount = {
                    // Show dialog first, then handle sign out and navigation on confirmation
                    showNewAccountDialog = true
                }
            )
        }

        composable<Screen.Chat> {
            ChatScreen()
        }
    }
    if (showNewAccountDialog){
        NewAccountDialog(
            onDismiss = { showNewAccountDialog = false },            onConfirmSignInLauncher = {
                showNewAccountDialog = false
                // Sign out current user and navigate to register screen to show Google account chooser
                RetrofitInstance.dementiaAPI.signOutUser()
                navController.navigate(Screen.Register) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )
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
    else if (userInfo.role == "PATIENT") Screen.DashBoardPatient
    else Screen.DashboardCareGiver
}