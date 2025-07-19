package com.example.frontend.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.screens.components.CaregiverRegisterDialog
import com.example.frontend.screens.components.ConfirmationDialog
import com.example.frontend.screens.components.PatientRegisterDialog
import com.example.frontend.screens.components.RegisterPromptDialog
import com.example.frontend.screens.components.RegisterScreenUI
import com.example.frontend.screens.components.RoleSelectionDialog
import androidx.compose.foundation.layout.fillMaxSize
import kotlinx.coroutines.launch

private const val TAG = "ScreenRegister"

@Composable
fun ScreenRegister(
    onNavigateToDashboard: (role: String) -> Unit,
    viewModel: ViewModelRegister = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()    // Initialize Google Sign-In Client
    LaunchedEffect(Unit) {
        viewModel.initializeGoogleSignInClient(context)
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(
            data = result.data,
            resultCode = result.resultCode,
            onNavigateToDashboard = onNavigateToDashboard
        )
    }

    // --- UI Composition ---
    Surface(modifier = Modifier.fillMaxSize()) {
        RegisterScreenUI(
            isLoading = uiState.isLoading,
            errorMsg = uiState.errorMsg,
            onGoogleSignIn = {
                Log.d(TAG, "Google Sign-In button clicked.")
                viewModel.setError(null)
                viewModel.clearAllDialogs()

                coroutineScope.launch {
                    viewModel.signOut()
                    Log.d(TAG, "Launching Google Sign-In Intent...")
                    signInLauncher.launch(viewModel.getGoogleSignInIntent())
                }
            }
        )

        if (uiState.showRegisterPrompt) {
            Log.d(TAG, "Displaying RegisterPromptDialog.")
            RegisterPromptDialog(
                onDismiss = {
                    Log.d(TAG, "RegisterPromptDialog dismissed.")
                    viewModel.clearAllDialogs()
                    viewModel.setError("Registration cancelled.")
                },
                onRegister = {
                    Log.d(TAG, "RegisterPromptDialog: Register clicked.")
                    viewModel.showRoleDialog()
                }
            )
        }

        if (uiState.showRoleDialog) {
            Log.d(TAG, "Displaying RoleSelectionDialog.")
            RoleSelectionDialog(
                onDismiss = {
                    Log.d(TAG, "RoleSelectionDialog dismissed.")
                    viewModel.clearAllDialogs()
                    viewModel.setError("Role selection cancelled.")
                },
                onRoleSelected = { role ->
                    Log.i(TAG, "Role selected: $role")
                    viewModel.selectRole(role)
                }
            )
        }

        if (uiState.showConfirmationDialog && uiState.selectedRole != null) {
            Log.d(TAG, "Displaying ConfirmationDialog for role: ${uiState.selectedRole}.")
            ConfirmationDialog(
                role = uiState.selectedRole!!,
                onConfirm = {
                    viewModel.confirmRole()
                },
                onBackToRoleSelection = {
                    viewModel.backToRoleSelection()
                },
                onDismissDialog = {
                    viewModel.dismissConfirmationDialog()
                }
            )
        }
        if (uiState.showCaregiverForm) {
            Log.d(TAG, "Displaying CaregiverRegisterDialog.")
            CaregiverRegisterDialog(
                name = uiState.caregiverFormData.name,
                email = uiState.caregiverFormData.email,
                dob = uiState.caregiverFormData.dob,
                gender = uiState.caregiverFormData.gender,
                profilePictureUrl = uiState.firebaseCredentials?.photoUrl,
                onDobChange = { dob ->
                    viewModel.updateCaregiverFormData(uiState.caregiverFormData.copy(dob = dob))
                },
                onGenderChange = { gender ->
                    viewModel.updateCaregiverFormData(uiState.caregiverFormData.copy(gender = gender))
                },
                onDismiss = {
                    Log.d(TAG, "CaregiverRegisterDialog dismissed (Cancel/Back).")
                    viewModel.dismissCaregiverForm()
                },
                onConfirm = {
                    Log.d(TAG, "CaregiverRegisterDialog: Confirm (Register) clicked.")
                    viewModel.handleCaregiverRegistration(onNavigateToDashboard)
                }
            )
        }
        if (uiState.showPatientForm) {
            Log.d(TAG, "Displaying PatientRegisterDialog.")
            PatientRegisterDialog(
                name = uiState.patientFormData.name,
                email = uiState.patientFormData.email,
                dob = uiState.patientFormData.dob,
                gender = uiState.patientFormData.gender,
                profilePictureUrl = uiState.firebaseCredentials?.photoUrl,
                onDobChange = { dob ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(dob = dob))
                },
                onGenderChange = { gender ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(gender = gender))
                },
                onPrimaryContactChange = { contact ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(primaryContact = contact))
                },
                onPrimaryInfoChange = {contact, otp ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(primaryContact = contact, otp = otp))
                },
                onDismiss = {
                    Log.d(TAG, "PatientRegisterDialog dismissed (Cancel/Back).")
                    viewModel.dismissPatientForm()
                },
                onConfirm = {
                    Log.d(TAG, "PatientRegisterDialog: Confirm (Register) clicked.")
                    viewModel.handlePatientRegistration(onNavigateToDashboard)
                }
            )
        }
    }
}

