package com.example.frontend.screens

import android.util.Log
import androidx.credentials.CredentialManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.frontend.R
import com.example.frontend.api.CaregiverRegisterRequest
import com.example.frontend.api.PatientRegisterRequest
import com.example.frontend.api.RetrofitInstance
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ScreenRegister(
    onNavigateToDashboard: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Dialog visibility states
    var showRegisterPrompt by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showCaregiverForm by remember { mutableStateOf(false) }
    var showPatientForm by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Data states
    var googleIdToken by remember { mutableStateOf<String?>(null) } // Stores the Google ID Token
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var caregiverName by remember { mutableStateOf("") }
    var caregiverDob by remember { mutableStateOf("") }
    var caregiverGender by remember { mutableStateOf("") }

    var patientName by remember { mutableStateOf("") }
    var patientDob by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }
    var primaryContact by remember { mutableStateOf("") } // Renamed from patientPrimaryContact for clarity
    var patientOtp by remember { mutableStateOf("") } // Renamed from otp for clarity

    fun handlePatientRegistration() {
        val token = googleIdToken ?: run { // Use the stored Google ID Token
            errorMsg = "Authentication token is missing. Please sign in again."
            isLoading = false
            return
        }
        val request = PatientRegisterRequest(
            name = patientName,
            dob = patientDob,
            gender = patientGender,
            primaryContact = primaryContact, // Ensure field name matches API
            otp = patientOtp // Ensure field name matches API
        )

        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                Log.d("ScreenRegister", "Registering patient with token: Bearer $token")
                val response = RetrofitInstance.dementiaAPI.registerPatient("Bearer $token", request)
                if (response.isSuccessful && response.code() == 201) {
                    Log.d("ScreenRegister", "Patient registered successfully.")
                    onNavigateToDashboard("patient")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ScreenRegister", "Patient registration failed: ${response.code()} - $errorBody")
                    errorMsg = "Patient registration failed: ${response.message()} - $errorBody"
                }
            } catch (e: Exception) {
                Log.e("ScreenRegister", "Patient registration exception", e)
                errorMsg = "An unexpected error occurred during patient registration."
            } finally {
                isLoading = false
            }
        }
    }

    fun handleCaregiverRegistration() {
        val token = googleIdToken ?: run { // Use the stored Google ID Token
            errorMsg = "Authentication token is missing. Please sign in again."
            isLoading = false
            return
        }
        val request = CaregiverRegisterRequest(
            name = caregiverName,
            dob = caregiverDob,
            gender = caregiverGender
        )

        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                Log.d("ScreenRegister", "Registering caregiver with token: Bearer $token")
                val response = RetrofitInstance.dementiaAPI.registerCaregiver("Bearer $token", request)
                if (response.isSuccessful && response.code() == 201) {
                    Log.d("ScreenRegister", "Caregiver registered successfully. Navigating to dashboard.")
                    onNavigateToDashboard("caregiver")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ScreenRegister", "Caregiver registration failed: ${response.code()} - $errorBody")
                    errorMsg = "Registration failed: ${response.message()} - $errorBody"
                }
            } catch (e: Exception) {
                Log.e("ScreenRegister", "Caregiver registration exception", e)
                errorMsg = "An unexpected error occurred during registration."
            } finally {
                isLoading = false
            }
        }
    }

    fun handleBackendAuth(gToken: String) { // Parameter renamed to gToken for clarity
        googleIdToken = gToken // Store Google ID token for registration APIs if needed
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                Log.d("ScreenRegister", "Google token received, calling backend /v1/auth")
                val response = RetrofitInstance.dementiaAPI.postAuth("Bearer $gToken")
                val authBody = response.body()

                if (response.isSuccessful && authBody != null) {
                    // Assuming backend sends "exist" or "not exists" in authBody.token
                    // OR sends the actual app token in authBody.token and role in authBody.userType
                    when {
                        authBody.token == "exist" -> {
                            Log.d("ScreenRegister", "User already registered (status 'exist').")
                            if (authBody.userType != null) {
                                Log.d("ScreenRegister", "Navigating to dashboard with role: ${authBody.userType}")
                                // NOTE: If 'exist' is in authBody.token, the actual app session token might be missing
                                // or expected to be the Google ID token itself for subsequent calls.
                                onNavigateToDashboard(authBody.userType)
                            } else {
                                Log.e("ScreenRegister", "User 'exist' but userType is null.")
                                errorMsg = "User exists but role is missing. Please contact support."
                            }
                        }
                        authBody.token == "not exists" -> {
                            Log.d("ScreenRegister", "User not registered (status 'not exists'), showing register prompt")
                            showRegisterPrompt = true
                        }
                        // Case: authBody.token is an actual app token and userType is present
                        authBody.token != null && authBody.userType != null -> {
                            Log.d("ScreenRegister", "User authenticated. Role: ${authBody.userType}. App Token received.")
                            // Here, authBody.token is the app's session token.
                            // You might want to store this app token globally (e.g., in a singleton AuthManager class).
                            // For now, just navigate.
                            onNavigateToDashboard(authBody.userType)
                        }
                        else -> {
                            Log.e("ScreenRegister", "Backend auth response unclear or user does not exist. Token: ${authBody.token}, UserType: ${authBody.userType}, Message: ${response.message()}")
                            // Default to registration prompt if status is ambiguous
                            showRegisterPrompt = true
                            // errorMsg = "Authentication status unclear. Please try again or register."
                        }
                    }
                } else if (response.code() == 404) { // Explicitly handle 404 as user not existing
                    Log.d("ScreenRegister", "User not registered (received 404), showing register prompt")
                    showRegisterPrompt = true
                }
                else {
                    val errorBodyContent = response.errorBody()?.string()
                    Log.e("ScreenRegister", "Backend auth call failed: ${response.code()} - ${response.message()} - $errorBodyContent")
                    errorMsg = "Authentication failed: ${response.message()}"
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ScreenRegister", "Backend Auth HttpException: ${e.code()} - $errorBody", e)
                errorMsg = "An error occurred on the server (Code: ${e.code()}). Please try again later."
            } catch (e: java.io.IOException) {
                Log.e("ScreenRegister", "Backend Auth IOException", e)
                errorMsg = "Could not connect to the server. Please check your internet connection."
            } catch (e: Exception) {
                Log.e("ScreenRegister", "Backend Auth Exception", e)
                errorMsg = "An unexpected error occurred during authentication: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }


    fun handleGoogleSignUp() {
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                val credentialManager = CredentialManager.create(context)
                val nonce = UUID.randomUUID().toString()

                val googleIdOption = GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id))
                    .setNonce(nonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request) // This is a suspend function
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val token = googleIdTokenCredential.idToken
                        googleIdToken = token
                        handleBackendAuth(token)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("ScreenRegister", "Google ID token parsing failed", e)
                        errorMsg = "Sign-in failed: Could not parse token."
                    }
                } else {
                    Log.e("ScreenRegister", "Unexpected credential type: ${credential::class.java.name}")
                    errorMsg = "Sign-in failed: Unexpected credential type."
                }
            } catch (e: GetCredentialException) {
                Log.e("ScreenRegister", "GetCredentialException (${e.type})", e)
                errorMsg = when (e) {
                    is androidx.credentials.exceptions.NoCredentialException ->
                        "No Google account found on this device. Please add an account and try again."
                    is androidx.credentials.exceptions.GetCredentialCancellationException ->
                        "Sign-in process was cancelled."
                    is androidx.credentials.exceptions.GetCredentialInterruptedException ->
                        "Sign-in was interrupted. Please try again."
                    // Add other specific GetCredentialException subtypes if needed
                    else -> {
                        Log.e("ScreenRegister", "Unhandled GetCredentialException: ${e.message}", e)
                        "Sign-in failed. An unknown error occurred: ${e.type}"
                    }
                }
            } catch (e: Exception) {
                Log.e("ScreenRegister", "Sign-in Exception", e)
                errorMsg = "An unexpected error occurred: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- Main UI ---
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
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Mind Trace", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "Dementia Caregiver App",
                fontSize = 20.sp,
                color = Color(0xFFEEEEEE),
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_mindtrace_logo),
                contentDescription = "Mind Trace Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                GoogleSignUpSection(onGoogleSignIn = ::handleGoogleSignUp)
            }

            errorMsg?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }

    // --- Dialogs ---
    if (showRegisterPrompt) {
        RegisterPromptDialog(
            onDismiss = { showRegisterPrompt = false },
            onRegister = {
                showRegisterPrompt = false
                showRoleDialog = true
            }
        )
    }

    if (showRoleDialog) {
        RoleSelectionDialog(
            onDismiss = { showRoleDialog = false },
            onRoleSelected = { role ->
                selectedRole = role
                showRoleDialog = false
                showConfirmDialog = true // Show confirmation dialog next
            }
        )
    }

    if (showConfirmDialog){
        ConfirmationDialog(
            role = selectedRole ?: "Unknown",
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                if (selectedRole == "caregiver") {
                    showCaregiverForm = true
                } else if (selectedRole == "patient") {
                    showPatientForm = true
                }
            },
            onBack = { // Action for the "Back" button in ConfirmationDialog
                showConfirmDialog = false
                showRoleDialog = true // Go back to role selection
            }
        )
    }


    if (showPatientForm) {
        PatientRegisterDialog(
            name = patientName,
            dob = patientDob,
            gender = patientGender,
            primaryContact = primaryContact,
            otp = patientOtp,
            onNameChange = { patientName = it },
            onDobChange = { patientDob = it },
            onGenderChange = { patientGender = it },
            onPrimaryContactChange = { primaryContact = it },
            onOtpChange = { patientOtp = it },
            onDismiss = { showPatientForm = false },
            onConfirm = {
                // Basic validation can be added here before calling handlePatientRegistration
                if (patientName.isNotBlank() && patientDob.isNotBlank() && patientGender.isNotBlank() && primaryContact.isNotBlank() && patientOtp.isNotBlank()) {
                    showPatientForm = false
                    handlePatientRegistration()
                } else {
                    errorMsg = "All fields are required for patient registration."
                }
            }
        )
    }

    if (showCaregiverForm) {
        CaregiverRegisterDialog(
            name = caregiverName,
            dob = caregiverDob,
            gender = caregiverGender,
            onNameChange = { caregiverName = it },
            onDobChange = { caregiverDob = it },
            onGenderChange = { caregiverGender = it },
            onDismiss = { showCaregiverForm = false },
            onConfirm = {
                // Basic validation can be added here
                if (caregiverName.isNotBlank() && caregiverDob.isNotBlank() && caregiverGender.isNotBlank()) {
                    showCaregiverForm = false
                    handleCaregiverRegistration()
                } else {
                    errorMsg = "All fields are required for caregiver registration."
                }
            }
        )
    }
}

@Composable
private fun GoogleSignUpSection(onGoogleSignIn: () -> Unit) {
    Button(
        onClick = onGoogleSignIn,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = "Google Logo",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign up with Google")
    }
}

@Composable
private fun RegisterPromptDialog(onDismiss: () -> Unit, onRegister: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registration Required") },
        text = { Text("You are not registered. Would you like to create an account?") },
        confirmButton = { Button(onClick = onRegister) { Text("Register") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RoleSelectionDialog(onDismiss: () -> Unit, onRoleSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Your Role") },
        text = { Text("To complete your registration, please select your role.") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onRoleSelected("patient") }) { Text("Patient") }
                Button(onClick = { onRoleSelected("caregiver") }) { Text("Caregiver") }
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Composable
private fun ConfirmationDialog(
    role: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit // Added onBack parameter
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Role") },
        text = { Text("You have selected: $role. Do you want to continue?") },
        confirmButton = { Button(onClick = onConfirm) { Text("Continue") } },
        dismissButton = {
            Row {
                Button(onClick = onBack) { Text("Back") } // Use onBack for the back action
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun CaregiverRegisterDialog(
    name: String,
    dob: String,
    gender: String,
    onNameChange: (String) -> Unit,
    onDobChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register as Caregiver") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Please provide your details to complete registration.")
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Full Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = dob,
                    onValueChange = onDobChange,
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = onGenderChange,
                    label = { Text("Gender (e.g., M, F, Other)") },
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Submit") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PatientRegisterDialog(
    name: String,
    dob: String,
    gender: String,
    primaryContact: String,
    otp: String,
    onNameChange: (String) -> Unit,
    onDobChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onPrimaryContactChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register as Patient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Please provide your details to complete registration.")
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Full Name") }, singleLine = true)
                OutlinedTextField(value = dob, onValueChange = onDobChange, label = { Text("Date of Birth (YYYY-MM-DD)") }, singleLine = true)
                OutlinedTextField(value = gender, onValueChange = onGenderChange, label = { Text("Gender (e.g., M, F, Other)") }, singleLine = true)
                OutlinedTextField(value = primaryContact, onValueChange = onPrimaryContactChange, label = { Text("Caregiver's Primary Contact ID") }, singleLine = true)
                OutlinedTextField(value = otp, onValueChange = onOtpChange, label = { Text("OTP from Caregiver") }, singleLine = true)
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Submit") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}