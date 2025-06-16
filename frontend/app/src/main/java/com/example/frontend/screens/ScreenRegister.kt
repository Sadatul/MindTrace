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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.api.AuthManagerResponse // Ensure this matches your data class (should have 'role' field)
import com.example.frontend.api.CaregiverRegisterRequest
import com.example.frontend.api.PatientRegisterRequest
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getIdToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

private const val TAG = "ScreenRegister"

@Composable
fun ScreenRegister(
    onNavigateToDashboard: (role: String, name: String?, email: String?, dob: String?, gender: String?, uid: String?, token: String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var showRegisterPrompt by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showCaregiverForm by remember { mutableStateOf(false) }
    var showPatientForm by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    var selectedRole by remember { mutableStateOf<String?>(null) }

    // Form fields
    var caregiverName by remember { mutableStateOf("") }
    var caregiverEmail by remember { mutableStateOf("") }
    var caregiverDob by remember { mutableStateOf("") }
    var caregiverGender by remember { mutableStateOf("") }

    var patientName by remember { mutableStateOf("") }
    var patientEmail by remember { mutableStateOf("") }
    var patientDob by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }
    var primaryContact by remember { mutableStateOf("") }
    var patientOtp by remember { mutableStateOf("") }

    // Remembered Firebase credentials
    var rememberedFirebaseUserUID by remember { mutableStateOf<String?>(null) }
    var rememberedFirebaseDisplayName by remember { mutableStateOf<String?>(null) }
    var rememberedFirebaseEmail by remember { mutableStateOf<String?>(null) }
    var rememberedFirebaseIdToken by remember { mutableStateOf<String?>(null) }


    val firebaseAuth = Firebase.auth
    val googleSignInClient = remember {
        Log.d(TAG, "Initializing GoogleSignInClient.")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Ensure this string resource exists
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun clearAllDialogs(clearSelectedRoleToo: Boolean = true) {
        showRegisterPrompt = false
        showRoleDialog = false
        showCaregiverForm = false
        showPatientForm = false
        showConfirmationDialog = false
        if (clearSelectedRoleToo) {
            selectedRole = null
        }
        Log.d(TAG, "All dialogs cleared. Selected role also cleared: $clearSelectedRoleToo")
    }

    fun handleBackendAuth(
        firebaseIdToken: String,
        firebaseUserUID: String,
        firebaseDisplayName: String?,
        firebaseEmail: String?
    ) {
        Log.d(TAG, "handleBackendAuth initiated.")
        Log.d(TAG, "  >> Firebase User UID: $firebaseUserUID")
        Log.d(TAG, "  >> Firebase Display Name: $firebaseDisplayName")
        Log.d(TAG, "  >> Firebase Email: $firebaseEmail")
        Log.d(TAG, "  >> Firebase ID Token (first 30 chars): ${firebaseIdToken.take(30)}...")

        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                Log.i(TAG, "Calling backend /v1/auth with Firebase ID Token...")
                val response = RetrofitInstance.dementiaAPI.postAuth("Bearer $firebaseIdToken")
                Log.d(TAG, "/v1/auth RAW Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val parsedBody: AuthManagerResponse? = response.body()
                    Log.i(TAG, "/v1/auth Call Successful (Code: ${response.code()})")
                    Log.d(TAG, "/v1/auth PARSED Body: $parsedBody")

                    if (parsedBody != null) {
                        when (parsedBody.status) {
                            "new user" -> {
                                Log.i(TAG, "Backend: New user detected. Storing Firebase credentials.")
                                rememberedFirebaseUserUID = firebaseUserUID
                                rememberedFirebaseDisplayName = firebaseDisplayName
                                rememberedFirebaseEmail = firebaseEmail
                                rememberedFirebaseIdToken = firebaseIdToken
                                Log.d(TAG, "Stored for registration - UID: $rememberedFirebaseUserUID, Token (start): ${rememberedFirebaseIdToken?.take(10)}")
                                clearAllDialogs()
                                showRegisterPrompt = true
                            }
                            "exists" -> {
                                Log.i(TAG, "Backend: Existing user detected.")
                                val token = RetrofitInstance.dementiaAPI.getIdToken()
                                Log.d(TAG, " ID Token (first 30 chars): ${token.take(30)}...")
                                val userBody = RetrofitInstance.dementiaAPI.getUserInfo("Bearer $token")
                                Log.d(TAG, "User Info Response: ${userBody.body()}")
                                val role = userBody.body()?.role
                                Log.d(TAG, "User role from backend: $role")

                                val userName = userBody.body()?.name
                                val userEmail = userBody.body()?.email
                                val userDob = userBody.body()?.dob
                                val userGender = userBody.body()?.gender
                                val userId = userBody.body()?.id

                                if(role == "CAREGIVER") {
                                    onNavigateToDashboard(role, userName, userEmail, userDob, userGender, userId, firebaseIdToken)
                                }else if(role == "PATIENT") {
                                    onNavigateToDashboard(role, userName, userEmail, userDob, userGender, userId, firebaseIdToken)
                                } else {
                                    errorMsg = "Unknown role returned from backend: $role. Check logs."
                                    Log.e(TAG, "Unknown role from backend: $role")
                                    clearAllDialogs()
                                }
                            }
                            else -> {
                                errorMsg = "Backend returned unknown status: ${parsedBody.status}. Check logs."
                                Log.e(TAG, "Unknown status from backend: ${parsedBody.status}. Parsed Body: $parsedBody")
                                clearAllDialogs()
                            }
                        }
                    } else {
                        errorMsg = "Backend auth response body was null. Check logs."
                        Log.e(TAG, "/v1/auth successful but response body is null.")
                        clearAllDialogs()
                    }
                } else {
                    val errorBodyString = try { response.errorBody()?.string() ?: "No error body." } catch (e: Exception) { "Failed to read error body." }
                    Log.e(TAG, "/v1/auth Call Failed (Code: ${response.code()}). Error Body: $errorBodyString")
                    errorMsg = "Backend authentication failed (Code ${response.code()}). See logs."
                    clearAllDialogs()
                }
            } catch (e: retrofit2.HttpException) {
                val errorBodyString = try { e.response()?.errorBody()?.string() } catch (ioe: java.io.IOException) { "N/A" }
                Log.e(TAG, "/v1/auth Retrofit HttpException: Code=${e.code()}, Msg=${e.message()}, ErrorBody='$errorBodyString'", e)
                errorMsg = "Backend server error (HTTP ${e.code()}). Check logs."
                clearAllDialogs()
            } catch (e: java.io.IOException) {
                Log.e(TAG, "/v1/auth Network IOException: ${e.message}", e)
                errorMsg = "Network error during backend auth. Check connection."
                clearAllDialogs()
            } catch (e: Exception) {
                Log.e(TAG, "/v1/auth General Exception: ${e.message}", e)
                errorMsg = "An unexpected error occurred during backend auth. Check logs."
                clearAllDialogs()
            } finally {
                isLoading = false
                Log.d(TAG, "handleBackendAuth finished. isLoading=$isLoading, errorMsg=$errorMsg, showRegisterPrompt=$showRegisterPrompt")
            }
        }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        errorMsg = null
        Log.d(TAG, "Google Sign-In result received. ResultCode: ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account == null) {
                    errorMsg = "Failed to get Google Account. Please try again."
                    Log.e(TAG, "GoogleSignIn.getSignedInAccountFromIntent returned null account.")
                    isLoading = false
                    return@rememberLauncherForActivityResult
                }

                val googleIdToken = account.idToken
                Log.d(TAG, "Google Sign-In successful.")
                Log.d(TAG, "  >> Google User Email: ${account.email}")
                Log.d(TAG, "  >> Google User Name: ${account.displayName}")
                Log.d(TAG, "  >> Google ID Token present: ${!googleIdToken.isNullOrEmpty()}")

                if (googleIdToken == null) {
                    errorMsg = "Failed to get Google ID Token. Please try again."
                    Log.e(TAG, "Google ID Token is null.")
                    isLoading = false
                    return@rememberLauncherForActivityResult
                }

                Log.d(TAG, "Attempting Firebase sign-in with Google ID Token.")
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                coroutineScope.launch {
                    try {
                        val authResult = firebaseAuth.signInWithCredential(credential).await()
                        val firebaseUser = authResult.user
                        if (firebaseUser == null) {
                            errorMsg = "Firebase auth succeeded but user is null."
                            Log.e(TAG, "Firebase sign-in successful but firebaseUser is null.")
                            isLoading = false
                            googleSignInClient.signOut()
                            return@launch
                        }

                        Log.i(TAG, "Firebase sign-in successful.")
                        Log.d(TAG, "  >> Firebase User UID: ${firebaseUser.uid}")
                        Log.d(TAG, "  >> Firebase Display Name: ${firebaseUser.displayName}")
                        Log.d(TAG, "  >> Firebase Email: ${firebaseUser.email}")

                        val tokenResult = firebaseUser.getIdToken(true).await() // Force refresh
                        Log.i(TAG, "Firebase ID Token : ${tokenResult.token}")

                        val freshFirebaseIdToken = tokenResult.token
                        if (freshFirebaseIdToken != null) {
                            Log.i(TAG, "Successfully obtained/refreshed Firebase ID Token.")
                            Log.d(TAG, "  >> Fresh Firebase ID Token (first 30 chars): ${freshFirebaseIdToken.take(30)}...")
                            handleBackendAuth(
                                firebaseIdToken = freshFirebaseIdToken,
                                firebaseUserUID = firebaseUser.uid,
                                firebaseDisplayName = firebaseUser.displayName,
                                firebaseEmail = firebaseUser.email
                            )
                        } else {
                            errorMsg = "Failed to get Firebase ID token (null after refresh)."
                            Log.w(TAG, "Firebase ID token null after refresh.")
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        errorMsg = "Firebase operation failed: ${e.localizedMessage}"
                        Log.w(TAG, "Firebase operation (sign-in or token refresh) failed", e)
                        isLoading = false
                        googleSignInClient.signOut()
                    }
                }
            } catch (e: ApiException) {
                errorMsg = "Google Sign-In failed (API Exception): ${e.statusCode} - ${e.message}"
                Log.e(TAG, "Google Sign-In ApiException: StatusCode: ${e.statusCode}", e)
                isLoading = false
            } catch (e: Exception) {
                errorMsg = "Unexpected error during Google Sign-In: ${e.message}"
                Log.e(TAG, "Google Sign-In general exception", e)
                isLoading = false
            }
        } else {
            errorMsg = "Google Sign-In cancelled or failed. (ResultCode: ${result.resultCode})"
            Log.w(TAG, "Google sign in cancelled/failed. ResultCode: ${result.resultCode}")
            isLoading = false
        }
    }



    fun handleCaregiverRegistration() {
        Log.d(TAG, "handleCaregiverRegistration called.")
        val tokenForRegistration = rememberedFirebaseIdToken
        val userUidForRegistration = rememberedFirebaseUserUID

        Log.d(TAG, "  Using for Caregiver Registration:")
        Log.d(TAG, "    >> Remembered Firebase User UID: $userUidForRegistration")
        Log.d(TAG, "    >> Remembered Firebase ID Token (start): ${tokenForRegistration?.take(10)}...")

        // Clear previous error messages specific to this form
        errorMsg = null

        if (tokenForRegistration.isNullOrBlank() || userUidForRegistration.isNullOrBlank()) {
            // This is a more fundamental auth issue, likely hide form and show error on main screen
            errorMsg = "Authentication session is invalid. Please sign in again."
            Log.e(TAG, "Caregiver registration: Missing remembered Firebase ID token or UID.")
            clearAllDialogs() // Hide all dialogs, including the form
            isLoading = false
            return
        }

        Log.d(TAG, "  Caregiver Form Input:")
        Log.d(TAG, "    >> Name: '$caregiverName'")
        Log.d(TAG, "    >> DOB: '$caregiverDob'")
        Log.d(TAG, "    >> Gender: '$caregiverGender'")

        if (caregiverName.isBlank() || caregiverDob.isBlank() || caregiverGender.isBlank()) {
            errorMsg = "All fields (Name, DOB, Gender) are required."
            Log.w(TAG, "Caregiver registration: Form validation failed. Error: $errorMsg")
            showCaregiverForm=false
            isLoading = false // Ensure loading is stopped if validation fails early
            return
        }

        val request = CaregiverRegisterRequest(name = caregiverName, dob = caregiverDob, gender = caregiverGender)
        val gson = Gson()
        Log.d(TAG, "Attempting Caregiver Registration with API:")
        Log.d(TAG, "  >> Authorization: Bearer ${tokenForRegistration.take(20)}...")
        Log.d(TAG, "  >> Request Body (JSON): ${gson.toJson(request)}")

        coroutineScope.launch {
            isLoading = true
            // errorMsg is reset at the start of the function, or if validation fails.
            // For network/server errors, it will be set in catch blocks.

            var registrationApiFailed = false // Flag to control dialog visibility on API failure

            try {
                Log.d(TAG, "Attempting")
                val response = RetrofitInstance.dementiaAPI.registerCaregiver(
                    "Bearer $tokenForRegistration", request
                )
                Log.d(TAG,"HEllO")
                Log.d(TAG, "Caregiver Register RAW Response Code: ${response.code()}")
                if (response.isSuccessful && response.code() == 201) {
                    Log.i(TAG, "Caregiver registered successfully")
                    clearAllDialogs()
                    val token = RetrofitInstance.dementiaAPI.getIdToken(true)
                    onNavigateToDashboard("CAREGIVER", caregiverName, caregiverEmail, caregiverDob, caregiverGender, userUidForRegistration, token)
                } else {
                    registrationApiFailed = true
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    errorMsg = "Caregiver backend registration failed (Code: ${response.code()}): $errorBody"
                    Log.e(TAG, "Caregiver registration failed: Code: ${response.code()}, Error: $errorBody, Request: ${gson.toJson(request)}")
                }
            } catch (e: retrofit2.HttpException) {
                registrationApiFailed = true
                val errBody = try { e.response()?.errorBody()?.string() } catch (ioe: java.io.IOException) { "N/A" }
                errorMsg = "Server error during caregiver registration (Code: ${e.code()}). Details: $errBody"
                Log.e(TAG, "Caregiver registration HttpException: Code: ${e.code()}, ErrorBody: $errBody", e)
            } catch (e: java.io.IOException) {
                registrationApiFailed = true
                errorMsg = "Network error during caregiver registration. Please check connection."
                Log.e(TAG, "Caregiver registration IOException", e)
            } catch (e: Exception) {
                registrationApiFailed = true
                errorMsg = "Caregiver registration error: ${e.localizedMessage}"
                Log.e(TAG, "Caregiver registration exception", e)
            } finally {
                isLoading = false
                if (registrationApiFailed) {
                    // For API failures, hide the form and show error on main screen
                    showCaregiverForm = false
                    showConfirmationDialog = false // Also hide confirmation if it was somehow still visible
                }
                // If it was a client-side validation error, showCaregiverForm remains true
                // and errorMsg is already set to the validation message.
                Log.d(TAG, "handleCaregiverRegistration finished. isLoading: $isLoading, errorMsg: $errorMsg, showCaregiverForm: $showCaregiverForm")
            }
        }
    }

    fun handlePatientRegistration() {
        Log.d(TAG, "handlePatientRegistration called.")
        val tokenForRegistration = rememberedFirebaseIdToken
        val userUidForRegistration = rememberedFirebaseUserUID

        Log.d(TAG, "  Using for Patient Registration:")
        Log.d(TAG, "    >> Remembered Firebase User UID: $userUidForRegistration")
        Log.d(TAG, "    >> Remembered Firebase ID Token (start): ${tokenForRegistration?.take(10)}...")

        // Clear previous error messages specific to this form
        errorMsg = null

        if (tokenForRegistration.isNullOrBlank() || userUidForRegistration.isNullOrBlank()) {
            // This is a more fundamental auth issue, likely hide form and show error on main screen
            errorMsg = "Authentication session is invalid. Please sign in again."
            Log.e(TAG, "Patient registration: Missing remembered Firebase ID token or UID.")
            clearAllDialogs() // Hide all dialogs, including the form
            isLoading = false
            return
        }

        Log.d(TAG, "  Patient Form Input:")
        Log.d(TAG, "    >> Name: '$patientName'")
        Log.d(TAG, "    >> DOB: '$patientDob'")
        Log.d(TAG, "    >> Gender: '$patientGender'")
        Log.d(TAG, "    >> Primary Contact: '$primaryContact'")
        Log.d(TAG, "    >> OTP: '$patientOtp'")

        if (patientName.isBlank() || patientDob.isBlank() || patientGender.isBlank() || primaryContact.isBlank() || patientOtp.isBlank()) {
            errorMsg = "All fields are required for patient registration."
            Log.w(TAG, "Patient registration: Form validation failed. Error: $errorMsg")
            showPatientForm=false
            isLoading = false // Ensure loading is stopped
            return
        }

        val request = PatientRegisterRequest(
            name = patientName, dob = patientDob, gender = patientGender,
            primaryContact = primaryContact, otp = patientOtp
        )
        val gson = Gson()
        Log.d(TAG, "Attempting Patient Registration with API:")
        Log.d(TAG, "  >> Authorization: Bearer ${tokenForRegistration.take(20)}...")
        Log.d(TAG, "  >> Request Body (JSON): ${gson.toJson(request)}")

        coroutineScope.launch {
            isLoading = true
            // errorMsg is reset at the start or on validation fail.
            // For network/server errors, it will be set in catch blocks.

            var registrationApiFailed = false // Flag to control dialog visibility

            try {
                val response = RetrofitInstance.dementiaAPI.registerPatient(
                    "Bearer $tokenForRegistration", request
                )
                Log.d(TAG, "Patient Register RAW Response Code: ${response.code()}")
                if (response.isSuccessful && response.code() == 201) {
                    Log.i(TAG, "Patient registered successfully")
                    clearAllDialogs()
                    val token = RetrofitInstance.dementiaAPI.getIdToken(true)
                    onNavigateToDashboard("PATIENT", patientName, patientEmail, patientDob, patientGender, userUidForRegistration,token)
                } else {
                    registrationApiFailed = true
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    errorMsg = "Patient backend registration failed (Code: ${response.code()}): $errorBody"
                    Log.e(TAG, "Patient registration failed: Code: ${response.code()}, Error: $errorBody, Request: ${gson.toJson(request)}")
                }
            } catch (e: retrofit2.HttpException) {
                registrationApiFailed = true
                val errBody = try { e.response()?.errorBody()?.string() } catch (ioe: java.io.IOException) { "N/A" }
                errorMsg = "Server error during patient registration (Code: ${e.code()}). Details: $errBody"
                Log.e(TAG, "Patient registration HttpException: Code: ${e.code()}, ErrorBody: $errBody", e)
            } catch (e: java.io.IOException) {
                registrationApiFailed = true
                errorMsg = "Network error during patient registration. Please check connection."
                Log.e(TAG, "Patient registration IOException", e)
            } catch (e: Exception) {
                registrationApiFailed = true
                errorMsg = "Patient registration error: ${e.localizedMessage}"
                Log.e(TAG, "Patient registration exception", e)
            } finally {
                isLoading = false
                if (registrationApiFailed) {
                    // For API failures, hide the form
                    showPatientForm = false
                    showConfirmationDialog = false // Also hide confirmation
                }
                // If client-side validation error, showPatientForm remains true
                // and errorMsg is already set.
                Log.d(TAG, "handlePatientRegistration finished. isLoading: $isLoading, errorMsg: $errorMsg, showPatientForm: $showPatientForm")
            }
        }
    }

    // --- UI Composition ---
    Surface(modifier = Modifier.fillMaxSize()) {
        RegisterScreenUI(
            isLoading = isLoading,
            errorMsg = errorMsg,
            onGoogleSignIn = {
                Log.d(TAG, "Google Sign-In button clicked.")
                errorMsg = null // Clear any previous error messages
                clearAllDialogs()

                // **Launch a coroutine here to perform suspend functions**
                coroutineScope.launch {
                    try {
                        Log.i(TAG, "Attempting to sign out from Firebase...")
                        Firebase.auth.signOut()
                        Log.i(TAG, "Firebase sign-out successful.")

                        Log.i(TAG, "Attempting to sign out from GoogleSignInClient...")
                        googleSignInClient.signOut().await() // Now this is called within a coroutine
                        Log.i(TAG, "GoogleSignInClient sign-out successful.")
                    } catch (e: Exception) {
                        Log.w(TAG, "Error during explicit sign-out before new sign-in attempt", e)
                        // It's generally safe to continue with the sign-in attempt even if sign-out failed,
                        // as the new sign-in flow might override the previous state.
                    } finally {
                        // Clear remembered credentials after sign-out attempt and before new sign-in
                        rememberedFirebaseIdToken = null
                        rememberedFirebaseUserUID = null
                        rememberedFirebaseDisplayName = null
                        rememberedFirebaseEmail = null
                        Log.d(TAG, "Cleared remembered Firebase credentials.")
                        Log.d(TAG, "Launching Google Sign-In Intent...")
                        signInLauncher.launch(googleSignInClient.signInIntent)
                    }
                }
            }
        )

        if (showRegisterPrompt) {
            Log.d(TAG, "Displaying RegisterPromptDialog.")
            RegisterPromptDialog(
                onDismiss = {
                    Log.d(TAG, "RegisterPromptDialog dismissed.")
                    showRegisterPrompt = false
                    errorMsg = "Registration cancelled."
                },
                onRegister = {
                    Log.d(TAG, "RegisterPromptDialog: Register clicked.")
                    showRegisterPrompt = false
                    showRoleDialog = true
                }
            )
        }

        if (showRoleDialog) {
            Log.d(TAG, "Displaying RoleSelectionDialog.")
            RoleSelectionDialog(
                onDismiss = {
                    Log.d(TAG, "RoleSelectionDialog dismissed.")
                    showRoleDialog = false
                    selectedRole = null
                    errorMsg = "Role selection cancelled."
                },
                onRoleSelected = { role ->
                    Log.i(TAG, "Role selected: $role")
                    selectedRole = role
                    showRoleDialog = false
                    showConfirmationDialog = true
                }
            )
        }

        if (showConfirmationDialog && selectedRole != null) {
            Log.d(TAG, "Displaying ConfirmationDialog for role: $selectedRole.")
            ConfirmationDialog(
                role = selectedRole!!,
                onConfirm = {
                    showConfirmationDialog = false
                    // Reset form fields before showing the form
                    // Name is pre-filled from Firebase Display Name if available
                    // Email is pre-filled from Firebase Email
                    val defaultName = rememberedFirebaseDisplayName ?: ""
                    val currentEmail = rememberedFirebaseEmail ?: "Email not available"

                    if (selectedRole == "caregiver") {
                        caregiverName = if (caregiverName.isBlank()) defaultName else caregiverName
                        caregiverEmail = if (caregiverEmail.isBlank()) currentEmail else caregiverEmail
                        caregiverDob = ""
                        caregiverGender = ""
                        showCaregiverForm = true
                    } else { // Patient
                        patientName = if (patientName.isBlank()) defaultName else patientName
                        patientEmail = if (patientEmail.isBlank()) currentEmail else patientEmail
                        patientDob = ""
                        patientGender = ""
                        primaryContact = ""
                        patientOtp = ""
                        showPatientForm = true
                    }
                },
                onBackToRoleSelection = {
                    showConfirmationDialog = false
                    showRoleDialog = true // Return to role selection
                },
                onDismissDialog = { // Dismiss via tap outside or system back
                    showConfirmationDialog = false
                }
            )
        }

        if (showCaregiverForm) {
            Log.d(TAG, "Displaying CaregiverRegisterDialog.")
            CaregiverRegisterDialog(
                name = caregiverName, email = caregiverEmail, dob = caregiverDob, gender = caregiverGender,
                onNameChange = { caregiverName = it },
                onEmailChange = { caregiverEmail = it },
                onDobChange = { caregiverDob = it },
                onGenderChange = { caregiverGender = it },
                onDismiss = {
                    Log.d(TAG, "CaregiverRegisterDialog dismissed (Cancel/Back).")
                    showCaregiverForm = false
                },
                onConfirm = {
                    Log.d(TAG, "CaregiverRegisterDialog: Confirm (Register) clicked.")
                    handleCaregiverRegistration()
                }
            )
        }

        if (showPatientForm) {
            Log.d(TAG, "Displaying PatientRegisterDialog.")
            PatientRegisterDialog(
                name = patientName, email = patientEmail,dob = patientDob, gender = patientGender,
                primaryContact = primaryContact, otp = patientOtp,
                onNameChange = { patientName = it }, onEmailChange = { patientEmail = it },
                onDobChange = { patientDob = it },
                onGenderChange = { patientGender = it }, onPrimaryContactChange = { primaryContact = it },
                onOtpChange = { patientOtp = it },
                onDismiss = {
                    Log.d(TAG, "PatientRegisterDialog dismissed (Cancel/Back).")
                    showPatientForm = false
                },
                onConfirm = {
                    Log.d(TAG, "PatientRegisterDialog: Confirm (Register) clicked.")
                    handlePatientRegistration()
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