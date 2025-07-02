package com.example.frontend.screens.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
import com.example.frontend.api.sendPatientAddOTP
import kotlinx.coroutines.launch

@Composable
fun DialogAddPartner(
    role: String, // Changed from PartnerScreenRole to String
    onDismiss: () -> Unit,
    onOtpRequested: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var patientId by remember { mutableStateOf("") }
    var patientInfo by remember { mutableStateOf<UserInfo?>(null) }
    var patientInfoError by remember { mutableStateOf<String?>(null) }
    var patientInfoLoading by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var otpSendingLoading by remember { mutableStateOf(false) } // Added loading state for OTP sending
    var qrScannerActive by remember { mutableStateOf(false) }
    var showScanPrompt by remember { mutableStateOf(true) }

    val qrScanner = rememberQRScanner(
        onResult = { result ->
            Log.d("DialogAddPartner", "Scanned QR result: $result")
            patientId = result
            qrScannerActive = false
            patientInfoLoading = true
            patientInfoError = null
            coroutineScope.launch {
                try {
                    val api = RetrofitInstance.dementiaAPI
                    val token = api.getIdToken()
                    val response = api.getUserInfo("Bearer $token", userId = patientId)
                    if (response.isSuccessful) {
                        patientInfo = response.body()
                        if (patientInfo != null) {
                            showConfirmDialog = true
                        } else {
                            patientInfoError = "Patient not found."
                        }
                    } else {
                        patientInfoError = "Patient not found."
                    }
                } catch (e: Exception) {
                    patientInfoError = "Error: ${e.message}"
                } finally {
                    patientInfoLoading = false
                }
            }
        },
        onError = { error ->
            patientInfoError = error
            qrScannerActive = false
        }
    )

    // If role is PATIENT, show not supported dialog
    if (role.equals("PATIENT", ignoreCase = true)) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Not Supported") },
            text = { Text("You cannot add caregivers directly. Please ask the caregiver to add you as a patient") },
            confirmButton = {
                Button(onClick = onDismiss) { Text("OK") }
            },
            dismissButton = {}
        )
        return
    }

    // Show scan prompt dialog first
    if (showScanPrompt) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Scan Patient QR") },
            text = {
                Text("You need to scan your Patient QR code to proceed. Are you ready with your patient QR code?")
            },
            confirmButton = {
                Button(onClick = {
                    showScanPrompt = false
                    qrScannerActive = true
                }) { Text("Yes, Scan Now") }
            },
            dismissButton = {
                Button(onClick = onDismiss) { Text("Cancel") }
            }
        )
        return
    }

    // Trigger scanner when prompt closes
    LaunchedEffect(qrScannerActive) {
        if (qrScannerActive) {
            qrScanner()
        }
    }

    // Show error dialog if there's an error
    if (patientInfoError != null) {
        AlertDialog(
            onDismissRequest = { 
                patientInfoError = null
                showScanPrompt = true
            },
            title = { Text("Error") },
            text = { Text(patientInfoError!!) },
            confirmButton = {
                Button(onClick = {
                    patientInfoError = null
                    showScanPrompt = true
                }) { Text("Try Again") }
            },
            dismissButton = {
                Button(onClick = {
                    patientInfoError = null
                    onDismiss()
                }) { Text("Cancel") }
            }
        )
        return
    }

    // Add loading state for patient info loading
    if (patientInfoLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Loading") },
            text = {
                Column {
                    CircularProgressIndicator()
                    Text("Fetching patient information...")
                }
            },
            confirmButton = { }
        )
        return
    }

    if (showConfirmDialog && patientInfo != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Patient Details") },
            text = {
                Column {
                    Text("Name: ${patientInfo!!.name}")
                    Text("Gender: ${patientInfo!!.gender}")
                    Text("DOB: ${patientInfo!!.dob}")
                    Text("Email: ${patientInfo!!.email}")
                    if (otpSendingLoading) {
                        CircularProgressIndicator(modifier = Modifier)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        otpSendingLoading = true
                        coroutineScope.launch {
                            try {
                                val success = RetrofitInstance.dementiaAPI.sendPatientAddOTP(patientId)
                                if (success) {
                                    showConfirmDialog = false
                                    onOtpRequested(patientId)
                                } else {
                                    patientInfoError = "Failed to send OTP. Please try again."
                                }
                            } catch (e: Exception) {
                                Log.e("DialogAddPartner", "Error sending OTP", e)
                                patientInfoError = "Network error while sending OTP. Please try again."
                            } finally {
                                otpSendingLoading = false
                            }
                        }
                    },
                    enabled = !otpSendingLoading
                ) { Text("Add Patient") }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false },
                    enabled = !otpSendingLoading
                ) { Text("Cancel") }
            }
        )
        return
    }
}