package com.example.frontend.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.R
import com.example.frontend.api.AuthManagerResponse
import com.example.frontend.api.CaregiverRegisterRequest
import com.example.frontend.api.PatientRegisterRequest
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getIdToken
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.screens.models.CaregiverFormData
import com.example.frontend.screens.models.FirebaseCredentials
import com.example.frontend.screens.models.PatientFormData
import com.example.frontend.screens.models.RegisterUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "RegisterViewModel"

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val firebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient

    fun initializeGoogleSignInClient(context: Context) {
        Log.d(TAG, "Initializing GoogleSignInClient.")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun clearAllDialogs(clearSelectedRoleToo: Boolean = true) {
        _uiState.value = _uiState.value.copy(
            showRegisterPrompt = false,
            showRoleDialog = false,
            showCaregiverForm = false,
            showPatientForm = false,
            showConfirmationDialog = false,
            selectedRole = if (clearSelectedRoleToo) null else _uiState.value.selectedRole
        )
        Log.d(TAG, "All dialogs cleared. Selected role also cleared: $clearSelectedRoleToo")
    }

    fun setError(error: String?) {
        _uiState.value = _uiState.value.copy(errorMsg = error)
    }

    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }

    fun showRegisterPrompt() {
        clearAllDialogs()
        _uiState.value = _uiState.value.copy(showRegisterPrompt = true)
    }

    fun showRoleDialog() {
        _uiState.value = _uiState.value.copy(
            showRegisterPrompt = false,
            showRoleDialog = true
        )
    }

    fun selectRole(role: String) {
        _uiState.value = _uiState.value.copy(
            selectedRole = role,
            showRoleDialog = false,
            showConfirmationDialog = true
        )
    }

    fun confirmRole() {
        val selectedRole = _uiState.value.selectedRole
        val credentials = _uiState.value.firebaseCredentials
        
        _uiState.value = _uiState.value.copy(showConfirmationDialog = false)
        
        val defaultName = credentials?.displayName ?: ""
        val currentEmail = credentials?.email ?: "Email not available"

        if (selectedRole == "caregiver") {
            _uiState.value = _uiState.value.copy(
                caregiverFormData = _uiState.value.caregiverFormData.copy(
                    name = if (_uiState.value.caregiverFormData.name.isBlank()) defaultName else _uiState.value.caregiverFormData.name,
                    email = if (_uiState.value.caregiverFormData.email.isBlank()) currentEmail else _uiState.value.caregiverFormData.email,
                    dob = "",
                    gender = ""
                ),
                showCaregiverForm = true
            )
        } else {
            _uiState.value = _uiState.value.copy(
                patientFormData = _uiState.value.patientFormData.copy(
                    name = if (_uiState.value.patientFormData.name.isBlank()) defaultName else _uiState.value.patientFormData.name,
                    email = if (_uiState.value.patientFormData.email.isBlank()) currentEmail else _uiState.value.patientFormData.email,
                    dob = "",
                    gender = "",
                    primaryContact = "",
                    otp = ""
                ),
                showPatientForm = true
            )
        }
    }

    fun backToRoleSelection() {
        _uiState.value = _uiState.value.copy(
            showConfirmationDialog = false,
            showRoleDialog = true
        )
    }

    fun dismissConfirmationDialog() {
        _uiState.value = _uiState.value.copy(showConfirmationDialog = false)
    }

    fun dismissCaregiverForm() {
        _uiState.value = _uiState.value.copy(showCaregiverForm = false)
    }

    fun dismissPatientForm() {
        _uiState.value = _uiState.value.copy(showPatientForm = false)
    }

    fun updateCaregiverFormData(formData: CaregiverFormData) {
        _uiState.value = _uiState.value.copy(caregiverFormData = formData)
    }

    fun updatePatientFormData(formData: PatientFormData) {
        _uiState.value = _uiState.value.copy(patientFormData = formData)
    }

    suspend fun signOut() {
        try {
            Log.i(TAG, "Attempting to sign out from Firebase...")
            Firebase.auth.signOut()
            Log.i(TAG, "Firebase sign-out successful.")

            Log.i(TAG, "Attempting to sign out from GoogleSignInClient...")
            googleSignInClient.signOut().await()
            Log.i(TAG, "GoogleSignInClient sign-out successful.")
        } catch (e: Exception) {
            Log.w(TAG, "Error during explicit sign-out before new sign-in attempt", e)
        } finally {
            _uiState.value = _uiState.value.copy(firebaseCredentials = null)
            Log.d(TAG, "Cleared remembered Firebase credentials.")
        }
    }

    fun getGoogleSignInIntent() = googleSignInClient.signInIntent

    fun handleGoogleSignInResult(
        data: android.content.Intent?,
        resultCode: Int,
        onNavigateToDashboard: (String) -> Unit
    ) {
        setLoading(true)
        setError(null)
        Log.d(TAG, "Google Sign-In result received. ResultCode: $resultCode")

        if (resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account == null) {
                    setError("Failed to get Google Account. Please try again.")
                    Log.e(TAG, "GoogleSignIn.getSignedInAccountFromIntent returned null account.")
                    setLoading(false)
                    return
                }

                val googleIdToken = account.idToken
                Log.d(TAG, "Google Sign-In successful.")
                Log.d(TAG, "  >> Google User Email: ${account.email}")
                Log.d(TAG, "  >> Google User Name: ${account.displayName}")
                Log.d(TAG, "  >> Google ID Token present: ${!googleIdToken.isNullOrEmpty()}")

                if (googleIdToken == null) {
                    setError("Failed to get Google ID Token. Please try again.")
                    Log.e(TAG, "Google ID Token is null.")
                    setLoading(false)
                    return
                }

                Log.d(TAG, "Attempting Firebase sign-in with Google ID Token.")
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                viewModelScope.launch {
                    try {
                        val authResult = firebaseAuth.signInWithCredential(credential).await()
                        val firebaseUser = authResult.user
                        if (firebaseUser == null) {
                            setError("Firebase auth succeeded but user is null.")
                            Log.e(TAG, "Firebase sign-in successful but firebaseUser is null.")
                            setLoading(false)
                            googleSignInClient.signOut()
                            return@launch
                        }

                        Log.i(TAG, "Firebase sign-in successful.")
                        Log.d(TAG, "  >> Firebase User UID: ${firebaseUser.uid}")
                        Log.d(TAG, "  >> Firebase Display Name: ${firebaseUser.displayName}")
                        Log.d(TAG, "  >> Firebase Email: ${firebaseUser.email}")

                        val tokenResult = firebaseUser.getIdToken(true).await()
                        Log.i(TAG, "Firebase ID Token : ${tokenResult.token}")

                        val freshFirebaseIdToken = tokenResult.token
                        if (freshFirebaseIdToken != null) {
                            Log.i(TAG, "Successfully obtained/refreshed Firebase ID Token.")
                            Log.d(TAG, "  >> Fresh Firebase ID Token (first 30 chars): ${freshFirebaseIdToken.take(30)}...")
                            handleBackendAuth(
                                firebaseIdToken = freshFirebaseIdToken,
                                firebaseUserUID = firebaseUser.uid,
                                firebaseDisplayName = firebaseUser.displayName,
                                firebaseEmail = firebaseUser.email,
                                firebasePhotoUrl = firebaseUser.photoUrl?.toString(),
                                onNavigateToDashboard = onNavigateToDashboard
                            )
                        } else {
                            setError("Failed to get Firebase ID token (null after refresh).")
                            Log.w(TAG, "Firebase ID token null after refresh.")
                            setLoading(false)
                        }
                    } catch (e: Exception) {
                        setError("Firebase operation failed: ${e.localizedMessage}")
                        Log.w(TAG, "Firebase operation (sign-in or token refresh) failed", e)
                        setLoading(false)
                        googleSignInClient.signOut()
                    }
                }
            } catch (e: ApiException) {
                setError("Google Sign-In failed (API Exception): ${e.statusCode} - ${e.message}")
                Log.e(TAG, "Google Sign-In ApiException: StatusCode: ${e.statusCode}", e)
                setLoading(false)
            } catch (e: Exception) {
                setError("Unexpected error during Google Sign-In: ${e.message}")
                Log.e(TAG, "Google Sign-In general exception", e)
                setLoading(false)
            }
        } else {
            setError("Google Sign-In cancelled or failed. (ResultCode: $resultCode)")
            Log.w(TAG, "Google sign in cancelled/failed. ResultCode: $resultCode")
            setLoading(false)
        }
    }

    private fun handleBackendAuth(
        firebaseIdToken: String,
        firebaseUserUID: String,
        firebaseDisplayName: String?,
        firebaseEmail: String?,
        firebasePhotoUrl: String?,
        onNavigateToDashboard: (String) -> Unit
    ) {
        Log.d(TAG, "handleBackendAuth initiated.")
        Log.d(TAG, "  >> Firebase User UID: $firebaseUserUID")
        Log.d(TAG, "  >> Firebase Display Name: $firebaseDisplayName")
        Log.d(TAG, "  >> Firebase Email: $firebaseEmail")
        Log.d(TAG, "  >> Firebase Photo URL: $firebasePhotoUrl")
        Log.d(TAG, "  >> Firebase ID Token (first 30 chars): ${firebaseIdToken.take(30)}...")

        viewModelScope.launch {
            setLoading(true)
            setError(null)
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
                                _uiState.value = _uiState.value.copy(
                                    firebaseCredentials = FirebaseCredentials(
                                        userUID = firebaseUserUID,
                                        displayName = firebaseDisplayName,
                                        email = firebaseEmail,
                                        idToken = firebaseIdToken,
                                        photoUrl = firebasePhotoUrl
                                    )
                                )
                                Log.d(TAG, "Stored for registration - UID: $firebaseUserUID, Token (start): ${firebaseIdToken.take(10)}")
                                clearAllDialogs()
                                showRegisterPrompt()
                            }
                            "exists" -> {
                                val userBody = RetrofitInstance.dementiaAPI.getSelfUserInfo() ?: return@launch
                                onNavigateToDashboard(userBody.role)
                            }
                            else -> {
                                setError("Backend returned unknown status: ${parsedBody.status}. Check logs.")
                                Log.e(TAG, "Unknown status from backend: ${parsedBody.status}. Parsed Body: $parsedBody")
                                clearAllDialogs()
                            }
                        }
                    } else {
                        setError("Backend auth response body was null. Check logs.")
                        Log.e(TAG, "/v1/auth successful but response body is null.")
                        clearAllDialogs()
                    }
                } else {
                    val errorBodyString = try { response.errorBody()?.string() ?: "No error body." } catch (_: Exception) { "Failed to read error body." }
                    Log.e(TAG, "/v1/auth Call Failed (Code: ${response.code()}). Error Body: $errorBodyString")
                    setError("Backend authentication failed (Code ${response.code()}). See logs.")
                    clearAllDialogs()
                }
            } catch (e: retrofit2.HttpException) {
                val errorBodyString = try { e.response()?.errorBody()?.string() } catch (_: java.io.IOException) { "N/A" }
                Log.e(TAG, "/v1/auth Retrofit HttpException: Code=${e.code()}, Msg=${e.message()}, ErrorBody='$errorBodyString'", e)
                setError("Backend server error (HTTP ${e.code()}). Check logs.")
                clearAllDialogs()
            } catch (e: java.io.IOException) {
                Log.e(TAG, "/v1/auth Network IOException: ${e.message}", e)
                setError("Network error during backend auth. Check connection.")
                clearAllDialogs()
            } catch (e: Exception) {
                Log.e(TAG, "/v1/auth General Exception: ${e.message}", e)
                setError("An unexpected error occurred during backend auth. Check logs.")
                clearAllDialogs()
            } finally {
                setLoading(false)
                Log.d(TAG, "handleBackendAuth finished. isLoading=${_uiState.value.isLoading}, errorMsg=${_uiState.value.errorMsg}, showRegisterPrompt=${_uiState.value.showRegisterPrompt}")
            }
        }
    }

    fun handleCaregiverRegistration(onNavigateToDashboard: (String) -> Unit) {
        Log.d(TAG, "handleCaregiverRegistration called.")
        val formData = _uiState.value.caregiverFormData
        val credentials = _uiState.value.firebaseCredentials

        Log.d(TAG, "  Using for Caregiver Registration:")
        Log.d(TAG, "    >> Remembered Firebase User UID: ${credentials?.userUID}")
        Log.d(TAG, "    >> Remembered Firebase ID Token (start): ${credentials?.idToken?.take(10)}...")

        setError(null)

        if (credentials?.idToken.isNullOrBlank() || credentials.userUID.isBlank()) {
            setError("Authentication session is invalid. Please sign in again.")
            Log.e(TAG, "Caregiver registration: Missing remembered Firebase ID token or UID.")
            clearAllDialogs()
            setLoading(false)
            return
        }

        Log.d(TAG, "  Caregiver Form Input:")
        Log.d(TAG, "    >> Name: '${formData.name}'")
        Log.d(TAG, "    >> DOB: '${formData.dob}'")
        Log.d(TAG, "    >> Gender: '${formData.gender}'")

        if (formData.name.isBlank() || formData.dob.isBlank() || formData.gender.isBlank()) {
            setError("All fields (Name, DOB, Gender) are required.")
            Log.w(TAG, "Caregiver registration: Form validation failed.")
            dismissCaregiverForm()
            setLoading(false)
            return
        }

        val request = CaregiverRegisterRequest(name = formData.name, dob = formData.dob, gender = formData.gender)
        val gson = Gson()
        Log.d(TAG, "Attempting Caregiver Registration with API:")
        Log.d(TAG, "  >> Authorization: Bearer ${credentials.idToken.take(20)}...")
        Log.d(TAG, "  >> Request Body (JSON): ${gson.toJson(request)}")

        viewModelScope.launch {
            setLoading(true)
            var registrationApiFailed = false

            try {
                Log.d(TAG, "Attempting caregiver registration...")
                val response = RetrofitInstance.dementiaAPI.registerCaregiver(
                    "Bearer ${credentials.idToken}", request
                )
                Log.d(TAG, "Caregiver Register RAW Response Code: ${response.code()}")
                if (response.isSuccessful && response.code() == 201) {
                    Log.i(TAG, "Caregiver registered successfully")
                    clearAllDialogs()
                    RetrofitInstance.dementiaAPI.getIdToken(true)
                    onNavigateToDashboard("CAREGIVER")
                } else {
                    registrationApiFailed = true
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    setError("Caregiver backend registration failed (Code: ${response.code()}): $errorBody")
                    Log.e(TAG, "Caregiver registration failed: Code: ${response.code()}, Error: $errorBody, Request: ${gson.toJson(request)}")
                }
            } catch (e: retrofit2.HttpException) {
                registrationApiFailed = true
                val errBody = try { e.response()?.errorBody()?.string() } catch (_: java.io.IOException) { "N/A" }
                setError("Server error during caregiver registration (Code: ${e.code()}). Details: $errBody")
                Log.e(TAG, "Caregiver registration HttpException: Code: ${e.code()}, ErrorBody: $errBody", e)
            } catch (e: java.io.IOException) {
                registrationApiFailed = true
                setError("Network error during caregiver registration. Please check connection.")
                Log.e(TAG, "Caregiver registration IOException", e)
            } catch (e: Exception) {
                registrationApiFailed = true
                setError("Caregiver registration error: ${e.localizedMessage}")
                Log.e(TAG, "Caregiver registration exception", e)
            } finally {
                setLoading(false)
                if (registrationApiFailed) {
                    dismissCaregiverForm()
                }
                Log.d(TAG, "handleCaregiverRegistration finished.")
            }
        }
    }

    fun handlePatientRegistration(onNavigateToDashboard: (String) -> Unit) {
        Log.d(TAG, "handlePatientRegistration called.")
        val formData = _uiState.value.patientFormData
        val credentials = _uiState.value.firebaseCredentials

        Log.d(TAG, "  Using for Patient Registration:")
        Log.d(TAG, "    >> Remembered Firebase User UID: ${credentials?.userUID}")
        Log.d(TAG, "    >> Remembered Firebase ID Token (start): ${credentials?.idToken?.take(10)}...")

        setError(null)

        if (credentials?.idToken.isNullOrBlank() || credentials.userUID.isBlank()) {
            setError("Authentication session is invalid. Please sign in again.")
            Log.e(TAG, "Patient registration: Missing remembered Firebase ID token or UID.")
            clearAllDialogs()
            setLoading(false)
            return
        }

        Log.d(TAG, "  Patient Form Input:")
        Log.d(TAG, "    >> Name: '${formData.name}'")
        Log.d(TAG, "    >> DOB: '${formData.dob}'")
        Log.d(TAG, "    >> Gender: '${formData.gender}'")
        Log.d(TAG, "    >> Primary Contact: '${formData.primaryContact}'")
        Log.d(TAG, "    >> OTP: '${formData.otp}'")

        if (formData.name.isBlank() || formData.dob.isBlank() || formData.gender.isBlank() || 
            formData.primaryContact.isBlank() || formData.otp.isBlank()) {
            setError("All fields are required for patient registration.")
            Log.w(TAG, "Patient registration: Form validation failed.")
            dismissPatientForm()
            setLoading(false)
            return
        }

        val request = PatientRegisterRequest(
            name = formData.name, dob = formData.dob, gender = formData.gender,
            primaryContact = formData.primaryContact, otp = formData.otp
        )
        val gson = Gson()
        Log.d(TAG, "Attempting Patient Registration with API:")
        Log.d(TAG, "  >> Authorization: Bearer ${credentials.idToken.take(20)}...")
        Log.d(TAG, "  >> Request Body (JSON): ${gson.toJson(request)}")

        viewModelScope.launch {
            setLoading(true)
            var registrationApiFailed = false

            try {
                val response = RetrofitInstance.dementiaAPI.registerPatient(
                    "Bearer ${credentials.idToken}", request
                )
                Log.d(TAG, "Patient Register RAW Response Code: ${response.code()}")
                if (response.isSuccessful && response.code() == 201) {
                    Log.i(TAG, "Patient registered successfully")
                    clearAllDialogs()
                    RetrofitInstance.dementiaAPI.getIdToken(true)
                    onNavigateToDashboard("PATIENT")
                } else {
                    registrationApiFailed = true
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    setError("Patient backend registration failed (Code: ${response.code()}): $errorBody")
                    Log.e(TAG, "Patient registration failed: Code: ${response.code()}, Error: $errorBody, Request: ${gson.toJson(request)}")
                }
            } catch (e: retrofit2.HttpException) {
                registrationApiFailed = true
                val errBody = try { e.response()?.errorBody()?.string() } catch (_: java.io.IOException) { "N/A" }
                setError("Server error during patient registration (Code: ${e.code()}). Details: $errBody")
                Log.e(TAG, "Patient registration HttpException: Code: ${e.code()}, ErrorBody: $errBody", e)
            } catch (e: java.io.IOException) {
                registrationApiFailed = true
                setError("Network error during patient registration. Please check connection.")
                Log.e(TAG, "Patient registration IOException", e)
            } catch (e: Exception) {
                registrationApiFailed = true
                setError("Patient registration error: ${e.localizedMessage}")
                Log.e(TAG, "Patient registration exception", e)
            } finally {
                setLoading(false)
                if (registrationApiFailed) {
                    dismissPatientForm()
                }
                Log.d(TAG, "handlePatientRegistration finished.")
            }
        }
    }
}
