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

    var scanResult by remember { mutableStateOf("") }
    val qrScanner = rememberQRScanner(
        onResult = { result ->
            Log.d("DialogAddPartner", "Scanned QR result: $result")
            scanResult = result
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

    if (qrScannerActive) {
        QRCodeScannerButton(
            onResult = { scannedId ->
                Log.d("DialogAddPartner", "QRCodeScannerButton scannedId: $scannedId")
                patientId = scannedId
                qrScannerActive = false
                patientInfoLoading = true
                patientInfoError = null
                coroutineScope.launch {
                    try {
                        val api = RetrofitInstance.dementiaAPI
                        val token = api.getIdToken()
                        Log.d("DialogAddPartner", "QRCodeScannerButton using patientId: $patientId, token: $token")
                        val response = api.getUserInfo("Bearer $token", userId = patientId)
                        Log.d("DialogAddPartner", "QRCodeScannerButton API response isSuccessful: ${response.isSuccessful}")
                        if (response.isSuccessful) {
                            patientInfo = response.body()
                            Log.d("DialogAddPartner", "QRCodeScannerButton patientInfo: $patientInfo")
                            if (patientInfo != null) {
                                showConfirmDialog = true
                            } else {
                                patientInfoError = "Patient not found."
                            }
                        } else {
                            patientInfoError = "Patient not found."
                        }
                    } catch (e: Exception) {
                        Log.e("DialogAddPartner", "QRCodeScannerButton error fetching patient info", e)
                        patientInfoError = "Error: ${e.message}"
                    } finally {
                        patientInfoLoading = false
                    }
                }
            },
            onError = { err ->
                Log.e("DialogAddPartner", "QRCodeScannerButton scan error: $err")
                patientInfoError = err
                qrScannerActive = false
            },
            text = "Scan QR Code"
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
                            val success = RetrofitInstance.dementiaAPI.sendPatientAddOTP(patientId)
                            otpSendingLoading = false
                            if (success) {
                                showConfirmDialog = false
                                onOtpRequested(patientId)
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
