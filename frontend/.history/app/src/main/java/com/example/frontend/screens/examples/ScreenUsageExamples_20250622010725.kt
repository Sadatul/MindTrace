package com.example.frontend.screens.examples

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.signOutUser
import com.example.frontend.screens.ScreenCareGiver
import com.example.frontend.screens.ScreenPatient
import com.example.frontend.screens.components.CloseAppDialog

/**
 * Example usage of ScreenCareGiver with the new profile menu functionality
 */
@Composable
fun CaregiverScreenExample(
    onNavigateToChat: () -> Unit,
    onNavigateToMainScreen: () -> Unit, // Navigate back to login/main screen
    onNavigateToLoginOptions: () -> Unit // Show login options for another account
) {
    var showCloseDialog by remember { mutableStateOf(false) }

    ScreenCareGiver(
        onNavigateToChat = onNavigateToChat,
        onSignOut = {
            // This is called when "Sign Out" is clicked from the profile menu
            showCloseDialog = true
        },
        onLoginWithAnotherAccount = {
            // This is called when "Login with Another Account" is clicked
            onNavigateToLoginOptions()
        },
        onShowCloseAppDialog = {
            // This is called when the sign out option is selected
            showCloseDialog = true
        }
    )

    // Show the close app dialog when needed
    if (showCloseDialog) {
        CloseAppDialog(
            onDismiss = {
                showCloseDialog = false
            },
            onConfirmClose = {
                // Perform sign out and navigate to main screen
                RetrofitInstance.dementiaAPI.signOutUser()
                showCloseDialog = false
                onNavigateToMainScreen()
            }
        )
    }
}

/**
 * Example usage of ScreenPatient with the new profile menu functionality
 */
@Composable
fun PatientScreenExample(
    onNavigateToChat: () -> Unit,
    onNavigateToMainScreen: () -> Unit, // Navigate back to login/main screen
    onNavigateToLoginOptions: () -> Unit, // Show login options for another account
    errorMsg: String? = null
) {
    var showCloseDialog by remember { mutableStateOf(false) }

    ScreenPatient(
        errorMsg = errorMsg,
        onNavigateToChat = onNavigateToChat,
        onSignOut = {
            // This is called when "Sign Out" is clicked from the profile menu
            showCloseDialog = true
        },
        onLoginWithAnotherAccount = {
            // This is called when "Login with Another Account" is clicked
            onNavigateToLoginOptions()
        },
        onShowCloseAppDialog = {
            // This is called when the sign out option is selected
            showCloseDialog = true
        }
    )

    // Show the close app dialog when needed
    if (showCloseDialog) {
        CloseAppDialog(
            onDismiss = {
                showCloseDialog = false
            },
            onConfirmClose = {
                // Perform sign out and navigate to main screen
                RetrofitInstance.dementiaAPI.signOutUser()
                showCloseDialog = false
                onNavigateToMainScreen()
            }
        )
    }
}

/**
 * Example of how to use these screens in a navigation setup:
 * 
 * In your main navigation or activity:
 * 
 * @Composable
 * fun MainNavigation() {
 *     var currentScreen by remember { mutableStateOf("login") }
 *     
 *     when (currentScreen) {
 *         "caregiver" -> {
 *             CaregiverScreenExample(
 *                 onNavigateToChat = { 
 *                     // Navigate to chat screen
 *                     currentScreen = "chat"
 *                 },
 *                 onNavigateToMainScreen = {
 *                     // Navigate back to login/main screen
 *                     currentScreen = "login"
 *                 },
 *                 onNavigateToLoginOptions = {
 *                     // Show login options or navigate to login screen
 *                     currentScreen = "login"
 *                 }
 *             )
 *         }
 *         "patient" -> {
 *             PatientScreenExample(
 *                 onNavigateToChat = { 
 *                     // Navigate to chat screen
 *                     currentScreen = "chat"
 *                 },
 *                 onNavigateToMainScreen = {
 *                     // Navigate back to login/main screen
 *                     currentScreen = "login"
 *                 },
 *                 onNavigateToLoginOptions = {
 *                     // Show login options or navigate to login screen
 *                     currentScreen = "login"
 *                 }
 *             )
 *         }
 *         // ... other screens
 *     }
 * }
 */
