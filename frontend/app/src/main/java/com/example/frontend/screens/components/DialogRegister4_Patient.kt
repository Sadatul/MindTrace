package com.example.frontend.screens.components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onPrimaryInfoChange: (String, String) -> Unit,
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
                onPrimaryInfoChange(caregiverId, extractedOtp)
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
            // Row: People icon, text, larger QR scan icon, no background
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = "Caregivers",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (!qrScanned) {
                    Text(
                        text = "Link Caregiver",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "QR Scanned",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Linked!",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clickable(enabled = !qrScannerActive) {
                            qrScannerActive = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Primary Contact QR",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        },
        onDobChange = onDobChange,
        onGenderChange = onGenderChange,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}