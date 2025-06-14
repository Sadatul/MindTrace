package com.example.frontend.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import com.example.frontend.api.AuthSession
import com.example.frontend.api.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun TokenDialog(
    token: String,
    onCopy: () -> Unit, // Called when copy is successful
    onCancel: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Caregiver Token") },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = token,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(token) })
                    onCopy() // Notify parent that copy happened
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Token") // Corrected Icon
                }
            }
        },
        confirmButton = {}, // No explicit confirm button, actions are copy or cancel
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Close") }
        }
    )
}

@Composable
fun OtpDialog(
    primaryContact: String?,
    otp: String?,
    onCopyPrimaryContact: () -> Unit, // Specific copy for primary contact
    onCopyOtp: () -> Unit,           // Specific copy for OTP
    onCancel: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Patient Registration Info") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Primary Contact: ${primaryContact ?: "N/A"}", modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(primaryContact ?: "") })
                        onCopyPrimaryContact()
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Primary Contact") // Corrected Icon
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("OTP: ${otp ?: "N/A"}", modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(otp ?: "") })
                        onCopyOtp()
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy OTP") // Corrected Icon
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCareGiver() {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var otp by remember { mutableStateOf<String?>(null) }
    var primaryContact by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var showTokenDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }

    var caregiverName by remember { mutableStateOf("Loading...") }
    var caregiverDob by remember { mutableStateOf("Loading...") }
    var caregiverGender by remember { mutableStateOf("Loading...") }

    // Use the response field from AuthSession as the token
    val currentToken = AuthSession.token

    fun fetchCaregiverDetails() {
        val token = AuthSession.token ?: run {
            errorMsg = "Authentication token is missing. Please log in again."
            Log.e("DashboardCaregiver", "fetchCaregiverDetails: Auth token is null")
            return
        }
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                Log.d("DashboardCaregiver", "Fetching caregiver details...")
                val response = RetrofitInstance.dementiaAPI.getCaregiverDetails("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val details = response.body()!!
                    caregiverName = details.name
                    caregiverDob = details.dob
                    caregiverGender = details.gender
                    Log.d("DashboardCaregiver", "Caregiver details fetched: $details")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DashboardCaregiver", "Failed to fetch caregiver details: ${response.code()} - $errorBody")
                    errorMsg = "Failed to fetch caregiver details: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("DashboardCaregiver", "Exception fetching caregiver details", e)
                errorMsg = "An unexpected error occurred while fetching details."
            } finally {
                isLoading = false
            }
        }
    }

    fun getPatientCode() {
        val token = AuthSession.token ?: run {
            errorMsg = "Authentication token is missing. Please log in again."
            Log.e("DashboardCaregiver", "getPatientCode: Auth token is null")
            return
        }
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                Log.d("DashboardCaregiver", "Fetching OTP and Primary Contact...")
                val response = RetrofitInstance.dementiaAPI.getOtp("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    otp = responseBody.otp
                    primaryContact = responseBody.primaryContact
                    Log.d("DashboardCaregiver", "Received OTP: ${otp}, Primary Contact: $primaryContact")
                    if (otp != null || primaryContact != null) {
                        showOtpDialog = true // Show dialog only if data is available
                    } else {
                        errorMsg = "Received empty OTP/Primary Contact."
                        Log.w("DashboardCaregiver", "OTP or Primary Contact is null in response body")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DashboardCaregiver", "Failed to get code: ${response.code()} - $errorBody")
                    errorMsg = "Failed to generate code: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("DashboardCaregiver", "Exception when getting code", e)
                errorMsg = "An unexpected error occurred while generating code."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchCaregiverDetails()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Caregiver Dashboard") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Welcome, Caregiver!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Name: $caregiverName", style = MaterialTheme.typography.bodyLarge)
                    Text("DOB: $caregiverDob", style = MaterialTheme.typography.bodyLarge)
                    Text("Gender: $caregiverGender", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    if (currentToken.isNullOrEmpty()) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Token not available.") }
                    } else {
                        showTokenDialog = true
                    }
                }) {
                    Text("Show Token")
                }
                Button(onClick = { getPatientCode() }) { // OTP dialog is shown inside getPatientCode if successful
                    Text("Get Patient Code")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            }

            errorMsg?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (showTokenDialog) {
            TokenDialog(
                token = currentToken ?: "Token not available",
                onCopy = {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Token copied to clipboard!") }
                    showTokenDialog = false
                },
                onCancel = {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Token dialog closed.") }
                    showTokenDialog = false
                }
            )
        }

        if (showOtpDialog) { // Condition to show OtpDialog
            OtpDialog(
                primaryContact = primaryContact,
                otp = otp,
                onCopyPrimaryContact = {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Primary Contact copied!") }
                    // Optionally close dialog: showOtpDialog = false
                },
                onCopyOtp = {
                    coroutineScope.launch { snackbarHostState.showSnackbar("OTP copied!") }
                    // Optionally close dialog: showOtpDialog = false
                },
                onCancel = {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Patient code dialog closed.") }
                    showOtpDialog = false
                }
            )
        }
    }
}