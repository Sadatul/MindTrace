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
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // Initialize Google Sign-In Client
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
                onNameChange = { name ->
                    viewModel.updateCaregiverFormData(uiState.caregiverFormData.copy(name = name))
                },
                onEmailChange = { email ->
                    viewModel.updateCaregiverFormData(uiState.caregiverFormData.copy(email = email))
                },
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
                primaryContact = uiState.patientFormData.primaryContact,
                otp = uiState.patientFormData.otp,
                onNameChange = { name ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(name = name))
                },
                onEmailChange = { email ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(email = email))
                },
                onDobChange = { dob ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(dob = dob))
                },
                onGenderChange = { gender ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(gender = gender))
                },
                onPrimaryContactChange = { contact ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(primaryContact = contact))
                },
                onOtpChange = { otp ->
                    viewModel.updatePatientFormData(uiState.patientFormData.copy(otp = otp))
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

@Composable
private fun RegisterScreenUI(
    isLoading: Boolean,
    errorMsg: String?,
    onGoogleSignIn: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF512DA8), Color(0xFF9575CD), Color(0xFF03DAC6))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mindtrace_logo),
                contentDescription = "Mind Trace Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Mind Trace", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "Dementia Caregiver App",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            if (isLoading) {
                Log.d(TAG, "RegisterScreenUI: Displaying CircularProgressIndicator.")
                CircularProgressIndicator(color = Color.White)
            } else {
                Log.d(TAG, "RegisterScreenUI: Displaying GoogleSignUpSection.")
                GoogleSignUpSection(onGoogleSignIn = onGoogleSignIn)
            }

            errorMsg?.let {
                Log.d(TAG, "RegisterScreenUI: Displaying error message: $it")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GoogleSignUpSection(onGoogleSignIn: () -> Unit) {
    Button(
        onClick = onGoogleSignIn,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = "Google Logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign in with Google", color = Color.White)
    }
}

@Composable
fun RegisterPromptDialog(onDismiss: () -> Unit, onRegister: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Registration") },
        text = { Text("It looks like you don't have an account yet. Would you like to register?") },
        confirmButton = {
            TextButton(onClick = onRegister) { Text("Register") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RoleSelectionDialog(onDismiss: () -> Unit, onRoleSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Your Role") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Please choose whether you are registering as a Patient or a Caregiver.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { onRoleSelected("patient") }) { Text("Patient") }
                    Button(onClick = { onRoleSelected("caregiver") }) { Text("Caregiver") }
                }
            }
        },
        confirmButton = { /* No explicit confirm, selection is the action */ },
        dismissButton = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun ConfirmationDialog(
    role: String,
    onConfirm: () -> Unit, // Proceeds to the next step (form)
    onBackToRoleSelection: () -> Unit, // Goes back to RoleSelectionDialog
    onDismissDialog: () -> Unit // Handles general dismiss (e.g., tapping outside, system back)
) {
    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = { Text("Confirm Role: ${role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}") },
        text = { Text("You have selected the role of a ${role.lowercase()}. Do you want to continue registration as a ${role.lowercase()}?") },
        confirmButton = {
            // Row to arrange buttons horizontally
            Row(
                modifier = Modifier.fillMaxWidth(), // Occupy full width to allow spacing
                horizontalArrangement = Arrangement.End // Align buttons to the end (right)
            ) {
                // "Back" button (now the dismiss action in this context)
                TextButton(
                    onClick = onBackToRoleSelection,
                    modifier = Modifier.padding(end = 8.dp) // Add some spacing between buttons
                ) {
                    Text("Back")
                }
                // "Continue" button (the confirm action)
                TextButton(onClick = onConfirm) {
                    Text("Continue as ${role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class) // Added OptIn for OutlinedTextField
@Composable
fun CaregiverRegisterDialog(
    name: String, email: String, dob: String, gender: String,
    onNameChange: (String) -> Unit, onEmailChange: (String) -> Unit, onDobChange: (String) -> Unit, onGenderChange: (String) -> Unit,
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Caregiver Registration") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dob,
                    onValueChange = onDobChange,
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = onGenderChange,
                    label = { Text("Gender") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Please fill in all details for registration.",
                    style = MaterialTheme.typography.bodySmall, // Used MaterialTheme style
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Register") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class) // Added OptIn for OutlinedTextField
@Composable
fun PatientRegisterDialog(
    name: String, email: String, dob: String, gender: String,
    primaryContact: String, otp: String,
    onNameChange: (String) -> Unit, onEmailChange: (String) -> Unit, onDobChange: (String) -> Unit, onGenderChange: (String) -> Unit,
    onPrimaryContactChange: (String) -> Unit, onOtpChange: (String) -> Unit,
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Patient Registration") },
        text = {
            Column(modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dob,
                    onValueChange = onDobChange,
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = onGenderChange,
                    label = { Text("Gender") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = primaryContact,
                    onValueChange = onPrimaryContactChange,
                    label = { Text("Primary Contact (Caregiver UID)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = onOtpChange,
                    label = { Text("OTP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Please fill in all details for registration. The OTP and Primary Contact ID are provided by the Caregiver.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Register") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}