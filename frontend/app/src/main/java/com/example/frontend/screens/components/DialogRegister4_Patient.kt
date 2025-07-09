package com.example.frontend.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PatientRegisterDialog(
    name: String,
    email: String,
    dob: String,
    gender: String,
    profilePictureUrl: String?,
    onDobChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onPrimaryContactChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var qrScannerActive by remember { mutableStateOf(false) }
    var qrScanned by remember { mutableStateOf(false) }
    val qrScanner = rememberQRScanner(
        onResult = { scannedData ->
            // Parse the QR code data: format is "caregiverId|otp"
            val parts = scannedData.split("|")
            if (parts.size == 2) {
                val caregiverId = parts[0]
                val extractedOtp = parts[1]
                onPrimaryContactChange(caregiverId)
                onOtpChange(extractedOtp)
                qrScannerActive = false
                qrScanned = true
            } else {
                // Fallback: treat entire string as caregiver ID for backwards compatibility
                onPrimaryContactChange(scannedData)
                qrScannerActive = false
                qrScanned = true
            }
        },
        onError = { error ->
            qrScannerActive = false
        }
    )

    LaunchedEffect(qrScannerActive) {
        if (qrScannerActive) {
            qrScanner()
        }
    }

    RegisterDialog(
        title = "Patient Registration",
        name = name,
        email = email,
        dob = dob,
        gender = gender,
        profilePictureUrl = profilePictureUrl,
        additionalFields = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background // More contrast
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // QR Scan Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = if (!qrScanned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = if (!qrScanned) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Primary Contact QR",
                            tint = if (!qrScanned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(enabled = !qrScannerActive) {
                                    qrScannerActive = true
                                }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        if (!qrScanned) {
                            Text(
                                text = "Scan Primary Contact QR code",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "QR Scanned",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "QR Scanned!",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    
                    // Show extracted data when QR is scanned
                    if (qrScanned) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Registration Data Extracted:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ Primary Contact ID and OTP automatically set",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        infoMessage = "Complete your patient profile by scanning your caregiver's QR code. The QR code contains both your Primary Contact ID and OTP.",
        onDobChange = onDobChange,
        onGenderChange = onGenderChange,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}