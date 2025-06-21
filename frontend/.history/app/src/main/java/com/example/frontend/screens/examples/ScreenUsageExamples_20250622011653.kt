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
 * Enhanced example usage of ScreenCareGiver with new features:
 * - Google profile picture integration
 * - Enhanced colorful button styling
 * - Integrated loading states
 * - Profile menu with sign out and account switching
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
            // You can either show a login selection dialog or navigate to login screen
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
 * Enhanced example usage of ScreenPatient with new features:
 * - Google profile picture integration
 * - Enhanced colorful FAB styling
 * - Profile menu with sign out and account switching
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
            // You can either show a login selection dialog or navigate to login screen
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
 * NEW FEATURES IMPLEMENTED:
 * 
 * 1. 🖼️ GOOGLE PROFILE PICTURE INTEGRATION:
 *    - Profile icon in TopAppBar shows actual Google profile picture
 *    - Circular crop with colored border
 *    - Fallback to default icon if no picture available
 * 
 * 2. 🎨 ENHANCED COLORFUL BUTTON STYLING:
 *    - Higher elevation (8dp default, 12dp pressed)
 *    - Rounded corners (20dp radius)
 *    - Icon backgrounds with subtle transparency
 *    - Better typography (titleMedium)
 *    - Integrated loading states
 * 
 * 3. 📱 PROFILE MENU FUNCTIONALITY:
 *    - Two options: "Sign Out" and "Login with Another Account"
 *    - Proper icons and colors for each option
 *    - Dark themed dropdown menu
 * 
 * 4. 🚪 CLOSE APP DIALOG:
 *    - "Close MindTrace?" confirmation dialog
 *    - Warning icon and clear messaging
 *    - Proper action buttons with colors
 * 
 * 5. 🎯 ENHANCED FLOATING ACTION BUTTON (Patient Screen):
 *    - Colorful design with icon background
 *    - Enhanced elevation and rounded corners
 *    - Better visual hierarchy
 * 
 * INTEGRATION GUIDE:
 * 
 * To use these enhanced screens in your navigation:
 * 
 * @Composable
 * fun MainNavigation() {
 *     var currentScreen by remember { mutableStateOf("login") }
 *     var showLoginOptions by remember { mutableStateOf(false) }
 *     
 *     when (currentScreen) {
 *         "caregiver" -> {
 *             CaregiverScreenExample(
 *                 onNavigateToChat = { 
 *                     currentScreen = "chat"
 *                 },
 *                 onNavigateToMainScreen = {
 *                     currentScreen = "login"
 *                 },
 *                 onNavigateToLoginOptions = {
 *                     showLoginOptions = true
 *                     // Or directly: currentScreen = "login"
 *                 }
 *             )
 *         }
 *         "patient" -> {
 *             PatientScreenExample(
 *                 onNavigateToChat = { 
 *                     currentScreen = "chat"
 *                 },
 *                 onNavigateToMainScreen = {
 *                     currentScreen = "login"
 *                 },
 *                 onNavigateToLoginOptions = {
 *                     showLoginOptions = true
 *                     // Or directly: currentScreen = "login"
 *                 }
 *             )
 *         }
 *         "login" -> {
 *             // Your login screen here
 *         }
 *         "chat" -> {
 *             // Your chat screen here
 *         }
 *     }
 *     
 *     // Handle login options dialog if needed
 *     if (showLoginOptions) {
 *         // Show account selection dialog or navigate to login
 *         showLoginOptions = false
 *         currentScreen = "login"
 *     }
 * }
 * 
 * COLORS USED:
 * - card_info: Blue for chat button
 * - card_caregiver: Purple for caregiver-specific button
 * - card_patient: Green for patient-specific button
 * - warning_orange: Orange for sign out actions
 * - dark_primary: Light blue for primary actions
 * - white: For high contrast text and icons
 */
