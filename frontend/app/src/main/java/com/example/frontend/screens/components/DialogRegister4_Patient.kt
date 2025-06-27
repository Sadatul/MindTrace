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
    otp: String,
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
        onResult = { scannedId ->
            onPrimaryContactChange(scannedId)
            qrScannerActive = false
            qrScanned = true
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
                    Spacer(modifier = Modifier.height(16.dp))
                    // OTP Field
                    OutlinedTextField(
                        value = otp,
                        onValueChange = onOtpChange,
                        label = {
                            Text(
                                "OTP from Primary Contact (Caregiver)",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        infoMessage = "Complete your patient profile. The OTP and Primary Contact ID are provided by your Primary Contact (Caregiver).",
        onDobChange = onDobChange,
        onGenderChange = onGenderChange,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}