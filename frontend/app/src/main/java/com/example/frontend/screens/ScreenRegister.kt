package com.example.frontend.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.frontend.R
import com.example.frontend.api.AuthSession
import com.example.frontend.api.CaregiverRegisterRequest
import com.example.frontend.api.PatientRegisterRequest
import com.example.frontend.api.RetrofitInstance
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

private const val TAG = "ScreenRegister"

/**
 * Main Composable for the Registration/Login Screen.
 * This screen handles the entire user authentication and registration flow.
 *
 * @param onNavigateToDashboard Lambda function to be invoked upon successful login/registration,
 *                              passing the user type ("caregiver" or "patient").
 */
@Composable
fun ScreenRegister(
    onNavigateToDashboard: (userType: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- STATE MANAGEMENT ---
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var showRegisterPrompt by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showCaregiverForm by remember { mutableStateOf(false) }
    var showPatientForm by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    var selectedRole by remember { mutableStateOf<String?>(null) }

    // Caregiver form states
    var caregiverName by remember { mutableStateOf("") }
    var caregiverDob by remember { mutableStateOf("") }
    var caregiverGender by remember { mutableStateOf("") }

    // Patient form states
    var patientName by remember { mutableStateOf("") }
    var patientDob by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }
    var primaryContact by remember { mutableStateOf("") }
    var patientOtp by remember { mutableStateOf("") }


    // --- GOOGLE & FIREBASE AUTH LOGIC ---
    val firebaseAuth = Firebase.auth

    // Prepare GoogleSignInClient
    val googleSignInClient = remember {
        Log.d(TAG, "Initializing GoogleSignInClient.")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // From google-services.json
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Function to authenticate with the backend after successful Firebase login
    fun handleBackendAuth(googleIdToken: String) {
        Log.d(TAG, "handleBackendAuth called.")
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                Log.d(TAG, "Calling backend /v1/auth with Google ID Token (first 10 chars): ${googleIdToken.take(10)}...")
                val response = RetrofitInstance.dementiaAPI.postAuth("Bearer $googleIdToken")
                val authBody = response.body()
                val rawErrorBody = if (!response.isSuccessful && response.errorBody() != null) {
                    response.errorBody()?.string() ?: "Error body was null but response not successful"
                } else {
                    "N/A (Response successful or error body not applicable)"
                }

                Log.d(TAG, "Backend /v1/auth response: Code=${response.code()}, IsSuccessful=${response.isSuccessful}, Body=${authBody}, RawErrorBody='$rawErrorBody'")

                if (response.isSuccessful && authBody != null) {
                    AuthSession.token = authBody.response // This is now the backend session token or a status like "exist"/"not exists"
                    AuthSession.userType = authBody.userType
                    Log.d(TAG, "Backend auth successful. AuthSession.token set to: ${AuthSession.token}, UserType: ${AuthSession.userType}")

                    when {
                        // Case 1: User exists, token is a real session token from backend
                        !AuthSession.token.isNullOrEmpty() && AuthSession.token != "exist" && AuthSession.token != "not exists" && !AuthSession.userType.isNullOrEmpty() -> {
                            Log.i(TAG, "User authenticated with backend. Token received. Navigating to dashboard as ${AuthSession.userType}.")
                            onNavigateToDashboard(AuthSession.userType!!)
                        }
                        // Case 2: User exists, backend confirms, use userType from backend
                        authBody.response == "exist" && !AuthSession.userType.isNullOrEmpty() -> {
                            Log.i(TAG, "User already exists (confirmed by backend). Navigating to dashboard as ${AuthSession.userType}.")
                            // Potentially, the Google ID token could be stored if needed for subsequent calls,
                            // or the backend should provide a session token even for "exist" if it's a new login session.
                            // For now, assuming navigation is enough if backend says "exist".
                            // If a session token is needed, the backend should provide it.
                            // If AuthSession.token is "exist", we might need to re-evaluate what token to use for API calls.
                            // For now, we proceed to dashboard.
                            onNavigateToDashboard(AuthSession.userType!!)
                        }
                        // Case 3: User does not exist, backend confirms
                        authBody.response == "not exists" -> {
                            Log.i(TAG, "User not registered with backend. Showing registration prompt.")
                            AuthSession.token = googleIdToken // Store Google ID token for registration process
                            showRegisterPrompt = true; Log.d(TAG, "showRegisterPrompt set to true")
                        }
                        else -> {
                            errorMsg = "Received an unknown or incomplete response from the server during auth. Body: $authBody"
                            Log.e(TAG, "Unknown backend auth response: $authBody, Token: ${AuthSession.token}, UserType: ${AuthSession.userType}")
                        }
                    }
                } else if (response.code() == 404) { // User not found by backend
                    Log.i(TAG, "User not found by backend (404). Showing registration prompt.")
                    AuthSession.token = googleIdToken // Store Google ID token for registration process
                    showRegisterPrompt = true; Log.d(TAG, "showRegisterPrompt set to true")
                }
                else { // Other errors from backend
                    errorMsg = "Backend authentication failed (Code: ${response.code()}): $rawErrorBody"
                    Log.e(TAG, "Backend auth failed: Code: ${response.code()}, ErrorBody: $rawErrorBody")
                }
            } catch (e: retrofit2.HttpException) {
                val errorBodyString = try { e.response()?.errorBody()?.string() } catch (ioe: java.io.IOException) { "Error reading error body." }
                errorMsg = "Server error during authentication (Code: ${e.code()}). Check logs."
                Log.e(TAG, "Backend auth HttpException: Code: ${e.code()}, ErrorBody: $errorBodyString", e)
            }
            catch (e: java.io.IOException) { // Network errors
                errorMsg = "Network error during authentication. Please check your connection."
                Log.e(TAG, "Backend auth IOException (Network issue).", e)
            }
            catch (e: Exception) { // Other unexpected errors
                errorMsg = "An unexpected error occurred during backend authentication: ${e.message}"
                Log.e(TAG, "Backend auth exception", e)
            } finally {
                isLoading = false
                Log.d(TAG, "handleBackendAuth finished. isLoading: $isLoading")
            }
        }
    }

    // Launcher for the Google Sign-In intent
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        errorMsg = null
        Log.d(TAG, "Google Sign-In result received. ResultCode: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                Log.d(TAG, "Google Sign-In successful. Email: ${account.email}, ID Token present: ${!idToken.isNullOrEmpty()}")

                if (idToken == null) {
                    errorMsg = "Failed to get Google ID Token. Please try again."
                    Log.e(TAG, "Google ID Token is null after successful sign-in.")
                    isLoading = false
                    return@rememberLauncherForActivityResult
                }

                Log.d(TAG, "Attempting Firebase sign-in with Google ID Token.")
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Log.i(TAG, "Firebase sign-in successful. UID: ${authTask.result.user?.uid}")
                            // Firebase auth is successful, now authenticate with YOUR backend
                            handleBackendAuth(idToken)
                        } else {
                            isLoading = false
                            errorMsg = "Firebase authentication failed: ${authTask.exception?.message}"
                            Log.e(TAG, "Firebase auth failed", authTask.exception)
                            if (authTask.exception is FirebaseAuthInvalidCredentialsException) {
                                Log.e(TAG, "Firebase: Invalid credentials. Check SHA-1, package name, google-services.json, and API key restrictions.")
                            } else if (authTask.exception is FirebaseAuthUserCollisionException) {
                                Log.e(TAG, "Firebase: User collision. This shouldn't happen with Google Sign-In if linking is not explicitly done.")
                            }
                        }
                    }
            } catch (e: ApiException) {
                isLoading = false
                errorMsg = "Google Sign-In failed (API): ${e.statusCode} - ${e.message}"
                Log.e(TAG, "Google Sign-In ApiException: StatusCode: ${e.statusCode}", e)
            } catch (e: Exception) {
                isLoading = false
                errorMsg = "An unexpected error occurred during Google Sign-In: ${e.message}"
                Log.e(TAG, "Google Sign-In general exception", e)
            }
        } else {
            isLoading = false
            errorMsg = "Google Sign-In was cancelled or failed. (ResultCode: ${result.resultCode})"
            Log.w(TAG, "Google sign in cancelled or failed. ResultCode: ${result.resultCode}")
        }
    }


    // --- BACKEND API REGISTRATION LOGIC ---
    fun handleCaregiverRegistration() {
        Log.d(TAG, "handleCaregiverRegistration called.")
        // This token should be the Google ID token stored in AuthSession if user is new
        val googleIdTokenForRegistration = AuthSession.token
        if (googleIdTokenForRegistration == null || googleIdTokenForRegistration == "exist" || googleIdTokenForRegistration == "not exists") {
            errorMsg = "Authentication session is invalid for registration. Please sign in again."
            isLoading = false
            Log.e(TAG, "Caregiver registration: Invalid or missing Google ID token in AuthSession. Current token: $googleIdTokenForRegistration")
            return
        }

        Log.d(TAG, "Caregiver registration form data: Name='${caregiverName}', DOB='${caregiverDob}', Gender='${caregiverGender}'")
        if (caregiverName.isBlank() || caregiverDob.isBlank() || caregiverGender.isBlank()) {
            errorMsg = "All fields are required for caregiver registration."
            Log.w(TAG, "Caregiver registration: Form validation failed. Fields: Name='${caregiverName}', DOB='${caregiverDob}', Gender='${caregiverGender}'")
            return
        }
        val request = CaregiverRegisterRequest(name = caregiverName, dob = caregiverDob, gender = caregiverGender)

        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            Log.i(TAG, "Attempting to register caregiver: $caregiverName with Google ID Token (first 10 chars): ${googleIdTokenForRegistration.take(10)}...")
            try {
                val response = RetrofitInstance.dementiaAPI.registerCaregiver("Bearer $googleIdTokenForRegistration", request)
                Log.d(TAG, "Caregiver registration response: Code=${response.code()}, IsSuccessful=${response.isSuccessful}, Body=${response.body()}")
                if (response.isSuccessful && response.code() == 201) { // 201 Created
                    Log.i(TAG, "Caregiver registered successfully: ${response.body()?.msg}")
                    // After successful registration, the user is essentially "logged in" as this new role.
                    // The backend should ideally return the new userType and a session token if applicable,
                    // or we re-trigger handleBackendAuth to get the session token.
                    // For now, we assume registration implies login for this role.
                    AuthSession.userType = "caregiver" // Set user type locally
                    // AuthSession.token should be updated if backend returns a new session token here.
                    // If not, the Google ID token is still there, might need re-auth with backend.
                    // Let's try to re-run backend auth to get a proper session token.
                    handleBackendAuth(googleIdTokenForRegistration) // Re-auth to get session token and confirm userType
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    errorMsg = "Caregiver registration failed (Code: ${response.code()}): $errorBody"
                    Log.e(TAG, "Caregiver registration failed: Code: ${response.code()}, ErrorBody: $errorBody")
                }
            } catch (e: retrofit2.HttpException) {
                val errorBodyString = try { e.response()?.errorBody()?.string() } catch (ioe: java.io.IOException) { "Error reading error body." }
                errorMsg = "Server error during caregiver registration (Code: ${e.code()}). Check logs."
                Log.e(TAG, "Caregiver registration HttpException: Code: ${e.code()}, ErrorBody: $errorBodyString", e)
            }
            catch (e: java.io.IOException) {
                errorMsg = "Network error during caregiver registration. Please check connection."
                Log.e(TAG, "Caregiver registration IOException", e)
            }
            catch (e: Exception) {
                errorMsg = "An error occurred during caregiver registration: ${e.message}"
                Log.e(TAG, "Caregiver registration exception", e)
            } finally {
                isLoading = false
                Log.d(TAG, "handleCaregiverRegistration finished. isLoading: $isLoading")
            }
        }
    }

    fun handlePatientRegistration() {
        Log.d(TAG, "handlePatientRegistration called.")
        val googleIdTokenForRegistration = AuthSession.token
        if (googleIdTokenForRegistration == null || googleIdTokenForRegistration == "exist" || googleIdTokenForRegistration == "not exists") {
            errorMsg = "Authentication session is invalid for registration. Please sign in again."
            isLoading = false
            Log.e(TAG, "Patient registration: Invalid or missing Google ID token in AuthSession. Current token: $googleIdTokenForRegistration")
            return
        }

        Log.d(TAG, "Patient registration form data: Name='${patientName}', DOB='${patientDob}', Gender='${patientGender}', Contact='${primaryContact}', OTP='${patientOtp}'")
        if (patientName.isBlank() || patientDob.isBlank() || patientGender.isBlank() || primaryContact.isBlank() || patientOtp.isBlank()) {
            errorMsg = "All fields are required for patient registration."
            Log.w(TAG, "Patient registration: Form validation failed.")
            return
        }
        val request = PatientRegisterRequest(
            name = patientName,
            dob = patientDob,
            gender = patientGender,
            primaryContact = primaryContact,
            otp = patientOtp
        )

        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            Log.i(TAG, "Attempting to register patient: $patientName with Google ID Token (first 10 chars): ${googleIdTokenForRegistration.take(10)}...")
            try {
                val response = RetrofitInstance.dementiaAPI.registerPatient("Bearer $googleIdTokenForRegistration", request)
                Log.d(TAG, "Patient registration response: Code=${response.code()}, IsSuccessful=${response.isSuccessful}, Body=${response.body()}")
                if (response.isSuccessful && response.code() == 201) { // 201 Created
                    Log.i(TAG, "Patient registered successfully: ${response.body()?.msg}")
                    AuthSession.userType = "patient"
                    handleBackendAuth(googleIdTokenForRegistration) // Re-auth to get session token
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    errorMsg = "Patient registration failed (Code: ${response.code()}): $errorBody"
                    Log.e(TAG, "Patient registration failed: Code: ${response.code()}, ErrorBody: $errorBody")
                }
            } catch (e: retrofit2.HttpException) {
                val errorBodyString = try { e.response()?.errorBody()?.string() } catch (ioe: java.io.IOException) { "Error reading error body." }
                errorMsg = "Server error during patient registration (Code: ${e.code()}). Check logs."
                Log.e(TAG, "Patient registration HttpException: Code: ${e.code()}, ErrorBody: $errorBodyString", e)
            }
            catch (e: java.io.IOException) {
                errorMsg = "Network error during patient registration. Please check connection."
                Log.e(TAG, "Patient registration IOException", e)
            }
            catch (e: Exception) {
                errorMsg = "An error occurred during patient registration: ${e.message}"
                Log.e(TAG, "Patient registration exception", e)
            } finally {
                isLoading = false
                Log.d(TAG, "handlePatientRegistration finished. isLoading: $isLoading")
            }
        }
    }


    // --- UI & DIALOGS ---
    Surface(modifier = Modifier.fillMaxSize()) {
        RegisterScreenUI(
            isLoading = isLoading,
            errorMsg = errorMsg,
            onGoogleSignIn = {
                Log.d(TAG, "Google Sign-In button clicked.")
                errorMsg = null // Clear previous errors
                signInLauncher.launch(googleSignInClient.signInIntent)
            }
        )

        if (showRegisterPrompt) {
            Log.d(TAG, "Displaying RegisterPromptDialog.")
            RegisterPromptDialog(
                onDismiss = {
                    Log.d(TAG, "RegisterPromptDialog dismissed.")
                    showRegisterPrompt = false
                },
                onRegister = {
                    Log.d(TAG, "RegisterPromptDialog: Register clicked.")
                    showRegisterPrompt = false
                    showRoleDialog = true; Log.d(TAG, "showRoleDialog set to true")
                }
            )
        }

        if (showRoleDialog) {
            Log.d(TAG, "Displaying RoleSelectionDialog.")
            RoleSelectionDialog(
                onDismiss = {
                    Log.d(TAG, "RoleSelectionDialog dismissed.")
                    showRoleDialog = false
                },
                onRoleSelected = { role ->
                    Log.i(TAG, "Role selected: $role")
                    selectedRole = role
                    showRoleDialog = false
                    showConfirmationDialog = true; Log.d(TAG, "showConfirmationDialog set to true")
                }
            )
        }

        if (showConfirmationDialog && selectedRole != null) {
            Log.d(TAG, "Displaying ConfirmationDialog for role: $selectedRole.")
            ConfirmationDialog(
                role = selectedRole!!,
                onDismiss = {
                    Log.d(TAG, "ConfirmationDialog dismissed.")
                    showConfirmationDialog = false
                    selectedRole = null; Log.d(TAG, "selectedRole reset to null")
                },
                onConfirm = {
                    Log.i(TAG, "Role confirmed: $selectedRole. Proceeding to respective form.")
                    showConfirmationDialog = false
                    // Clear previous form data when role is confirmed
                    caregiverName = ""; caregiverDob = ""; caregiverGender = ""
                    patientName = ""; patientDob = ""; patientGender = ""; primaryContact = ""; patientOtp = ""
                    Log.d(TAG, "Form fields cleared for new role entry.")

                    if (selectedRole == "caregiver") {
                        showCaregiverForm = true; Log.d(TAG, "showCaregiverForm set to true")
                    } else {
                        showPatientForm = true; Log.d(TAG, "showPatientForm set to true")
                    }
                },
                onBack = {
                    Log.d(TAG, "ConfirmationDialog: Back clicked.")
                    showConfirmationDialog = false
                    showRoleDialog = true; Log.d(TAG, "showRoleDialog set to true (back to role selection)")
                }
            )
        }

        if (showCaregiverForm) {
            Log.d(TAG, "Displaying CaregiverRegisterDialog.")
            CaregiverRegisterDialog(
                name = caregiverName,
                dob = caregiverDob,
                gender = caregiverGender,
                onNameChange = { caregiverName = it },
                onDobChange = { caregiverDob = it },
                onGenderChange = { caregiverGender = it },
                onDismiss = {
                    Log.d(TAG, "CaregiverRegisterDialog dismissed.")
                    showCaregiverForm = false
                    selectedRole = null; Log.d(TAG, "selectedRole reset to null")
                },
                onConfirm = {
                    Log.d(TAG, "CaregiverRegisterDialog: Confirm clicked.")
                    handleCaregiverRegistration()
                }
            )
        }

        if (showPatientForm) {
            Log.d(TAG, "Displaying PatientRegisterDialog.")
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
                onDismiss = {
                    Log.d(TAG, "PatientRegisterDialog dismissed.")
                    showPatientForm = false
                    selectedRole = null; Log.d(TAG, "selectedRole reset to null")
                },
                onConfirm = {
                    Log.d(TAG, "PatientRegisterDialog: Confirm clicked.")
                    handlePatientRegistration()
                }
            )
        }
    }
}

/**
 * The main UI for the registration screen.
 * This composable displays the app branding and the Google Sign-In button.
 *
 * @param isLoading Boolean indicating if a loading process is active.
 * @param errorMsg Optional string containing an error message to display.
 * @param onGoogleSignIn Lambda function to be invoked when the Google Sign-In button is clicked.
 */
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
                .fillMaxWidth(), // Ensure column takes width for alignment
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mindtrace_logo), // Corrected logo name
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
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error, // Use theme's error color
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyMedium // Consistent text style
                )
            }
        }
    }
}

/**
 * A Composable section that displays a "Sign in with Google" button.
 *
 * @param onGoogleSignIn Lambda function to be invoked when the button is clicked.
 */
@Composable
fun GoogleSignUpSection(
    onGoogleSignIn: () -> Unit)
{
    Button(
        onClick = onGoogleSignIn,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google_logo), // Ensure ic_google_logo drawable exists
            contentDescription = "Google Logo",
            tint = Color.Unspecified, // Keep original colors of the Google logo
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign in with Google", color = Color.White)
    }
}

/**
 * A dialog that prompts the user to complete registration if their account is not found.
 *
 * @param onDismiss Lambda function to be invoked when the dialog is dismissed.
 * @param onRegister Lambda function to be invoked when the "Register" button is clicked.
 */
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

/**
 * A dialog that allows the user to select their role (Patient or Caregiver).
 *
 * @param onDismiss Lambda function to be invoked when the dialog is dismissed.
 * @param onRoleSelected Lambda function to be invoked with the selected role string ("patient" or "caregiver").
 */
@Composable
fun RoleSelectionDialog(onDismiss: () -> Unit, onRoleSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Role") },
        text = {
            Column {
                Text("Please select your role:")
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { onRoleSelected("patient") }) { Text("Patient") }
                    Button(onClick = { onRoleSelected("caregiver") }) { Text("Caregiver") }
                }
            }
        },
        confirmButton = { /* No explicit confirm button, selection is direct */ },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * A dialog to confirm the user's selected role before proceeding.
 *
 * @param role The role selected by the user.
 * @param onDismiss Lambda function to be invoked when the dialog is dismissed (e.g., "Cancel" clicked).
 * @param onConfirm Lambda function to be invoked when the "Continue" button is clicked.
 * @param onBack Lambda function to be invoked when the "Back" button is clicked (to go back to role selection).
 */
@Composable
fun ConfirmationDialog(role: String, onDismiss: () -> Unit, onConfirm: () -> Unit, onBack: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Role") },
        text = { Text("You selected: ${role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}. Continue?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Continue") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.width(8.dp)) // Add some space between Back and Cancel
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

/**
 * A dialog form for patient registration.
 *
 * @param name Current value for the patient's name.
 * @param dob Current value for the patient's date of birth.
 * @param gender Current value for the patient's gender.
 * @param primaryContact Current value for the patient's primary contact (Caregiver's email).
 * @param otp Current value for the OTP (received from Caregiver).
 * @param onNameChange Lambda function invoked when the name field changes.
 * @param onDobChange Lambda function invoked when the DOB field changes.
 * @param onGenderChange Lambda function invoked when the gender field changes.
 * @param onPrimaryContactChange Lambda function invoked when the primary contact field changes.
 * @param onOtpChange Lambda function invoked when the OTP field changes.
 * @param onDismiss Lambda function to be invoked when the dialog is dismissed.
 * @param onConfirm Lambda function to be invoked when the "Register" button is clicked.
 */
@Composable
fun PatientRegisterDialog(
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
        title = { Text("Patient Registration") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = dob, onValueChange = onDobChange, label = { Text("Date of Birth (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = gender, onValueChange = onGenderChange, label = { Text("Gender") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = primaryContact, onValueChange = onPrimaryContactChange, label = { Text("Primary Contact (Caregiver Email)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = otp, onValueChange = onOtpChange, label = { Text("OTP (From Caregiver)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Register") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * A dialog form for caregiver registration.
 *
 * @param name Current value for the caregiver's name.
 * @param dob Current value for the caregiver's date of birth.
 * @param gender Current value for the caregiver's gender.
 * @param onNameChange Lambda function invoked when the name field changes.
 * @param onDobChange Lambda function invoked when the DOB field changes.
 * @param onGenderChange Lambda function invoked when the gender field changes.
 * @param onDismiss Lambda function to be invoked when the dialog is dismissed.
 * @param onConfirm Lambda function to be invoked when the "Register" button is clicked.
 */
@Composable
fun CaregiverRegisterDialog(
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
        title = { Text("Caregiver Registration") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = dob, onValueChange = onDobChange, label = { Text("Date of Birth (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = gender, onValueChange = onGenderChange, label = { Text("Gender") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Register") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}