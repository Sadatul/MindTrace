package com.example.frontend.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.signOutUser
import kotlinx.coroutines.launch

private const val TAG = "ScreenCareGiver" // Tag for logging

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCareGiver(
    onNavigateToChat: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // For the OTP/code generated for a new patient
    var newPatientOtp by remember { mutableStateOf<String?>(null) }
    var showNewPatientOtpDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var userInfo: UserInfo? by remember { mutableStateOf(null) }

    LaunchedEffect(null) {
        userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
    }

    fun getNewPatientRegistrationCode() {
        Log.d(TAG, "getNewPatientRegistrationCode called.")

        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            newPatientOtp = null // Reset previous OTP
            Log.d(TAG, "Coroutine launched for API call. isLoading=true, newPatientOtp=null")
            try {

                val caregiverAuthToken = RetrofitInstance.dementiaAPI.getIdToken()

                Log.d(TAG, "Attempting API call to RetrofitInstance.dementiaAPI.getOtp")
                val response = RetrofitInstance.dementiaAPI.getOtp("Bearer $caregiverAuthToken")
                Log.d(TAG, "API call finished. Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    Log.d(TAG, "API Response Successful. Body: $responseBody")
                    newPatientOtp = responseBody.otp
                    Log.d(TAG, "Extracted OTP from response: $newPatientOtp")

                    if (newPatientOtp != null) {
                        Log.d(TAG, "OTP is not null. Setting showNewPatientOtpDialog = true")
                        showNewPatientOtpDialog = true
                    } else {
                        errorMsg = "Received empty registration code from server."
                        Log.w(TAG, "Registration OTP is null in response body. Error: $errorMsg")
                        snackbarHostState.showSnackbar("Received empty registration code.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "API call failed or response body is null. Code: ${response.code()}, Message: ${response.message()}, Error body: $errorBody")
                    errorMsg = "Failed to generate registration code: ${response.message()} (Code: ${response.code()})"
                    snackbarHostState.showSnackbar("Failed to generate registration code. ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call or processing response", e)
                errorMsg = "An unexpected error occurred: ${e.message}"
                snackbarHostState.showSnackbar("An error occurred while fetching registration code.")
            } finally {
                isLoading = false
                Log.d(TAG, "API call coroutine finished. isLoading=false")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Caregiver Dashboard") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            if (userInfo != null) {
                CaregiverInfoCard(
                    name = userInfo!!.name,
                    email = userInfo!!.email,
                    gender = userInfo!!.gender,
                    dob = userInfo!!.dob
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToChat,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = "Chat Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Help & Support Chat")
                }

                // Button to get and show OTP for a New Patient
                Button(
                    onClick = {
                        Log.d(TAG, "'Get Code for New Patient' button clicked.")
                        getNewPatientRegistrationCode()
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    enabled = !isLoading
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) { // Icons in a row
                        Icon(
                            Icons.Filled.PersonAdd,
                            contentDescription = "New Patient Icon",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Icon(
                            Icons.Filled.Pin, // Icon for OTP/Code
                            contentDescription = "Registration Code Icon",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Get Code for New Patient")
                }
            }

            if (isLoading) {
                Log.d(TAG, "Displaying CircularProgressIndicator because isLoading is true.")
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            errorMsg?.let {
                Log.d(TAG, "Displaying error message: $it")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Button(onClick = {
            RetrofitInstance.dementiaAPI.signOutUser()
        }) {
            Text("Sign Out")
        }

        Log.d(TAG, "ScreenCareGiver recomposing. showNewPatientOtpDialog: $showNewPatientOtpDialog, newPatientOtp: $newPatientOtp")

        // Dialog to display the OTP for a new patient registration
        if (showNewPatientOtpDialog) {
            Log.d(TAG, "Rendering NewPatientRegistrationCodeDialog. OTP: $newPatientOtp")
            ShowOTP( // Renamed Dialog
                otp = newPatientOtp ?: "Code not available",
                onCopy = {
                    Log.d(TAG, "Copy clicked in NewPatientRegistrationCodeDialog.")
                    newPatientOtp?.let {
                        clipboardManager.setText(AnnotatedString(it))
                        coroutineScope.launch { snackbarHostState.showSnackbar("Registration code copied!") }
                    }
                    showNewPatientOtpDialog = false
                },
                onCancel = {
                    Log.d(TAG, "Cancel clicked in NewPatientRegistrationCodeDialog.")
                    showNewPatientOtpDialog = false
                }
            )
        }
    }
}

@Composable
fun CaregiverInfoCard(name: String, email: String, dob: String, gender: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "My Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            InfoRow(label = "Name", icon = Icons.Filled.AccountCircle, value = name)
            InfoRow(label = "Email", icon = Icons.Filled.Email, value = email)
            InfoRow(label = "Date of Birth", icon = Icons.Filled.CalendarToday, value = dob)
            InfoRow(label = "Gender", icon = Icons.Filled.PersonPin, value = formatGender(gender))
        }
    }
}

@Composable
fun InfoRow(label: String, icon: ImageVector, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatGender(genderCode: String): String {
    return when (genderCode.uppercase()) {
        "M" -> "Male"
        "F" -> "Female"
        "O", "OTHER" -> "Other"
        else -> genderCode
    }
}

// Dialog for displaying the OTP for New Patient Registration
@Composable
fun ShowOTP(otp: String, onCopy: () -> Unit, onCancel: () -> Unit) {
    Log.d(TAG, "NewPatientRegistrationCodeDialog composable is being executed (recomposing). OTP: $otp")
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("New Patient Registration Code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Share this One-Time Code with the new patient for their registration process.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        otp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        style = MaterialTheme.typography.headlineSmall, // Make code prominent
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onCopy, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.ContentCopy, "Copy Code")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Close")
            }
        }
    )
}