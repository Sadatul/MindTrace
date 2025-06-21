package com.example.frontend.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PatientRegisterPage(
    name: String,
    email: String,
    dob: String,
    gender: String,
    primaryContact: String,
    otp: String,
    profilePictureUrl: String?,
    onDobChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onPrimaryContactChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    RegisterPage(
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Caregiver Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = primaryContact,
                        onValueChange = onPrimaryContactChange,
                        label = { 
                            Text(
                                "Primary Contact (Caregiver UID)",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Primary Contact",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = otp,
                        onValueChange = onOtpChange,
                        label = { 
                            Text(
                                "OTP Verification Code",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = "OTP",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        infoMessage = "Complete your patient profile. The OTP and Primary Contact ID are provided by your caregiver for secure connection.",
        onDobChange = onDobChange,
        onGenderChange = onGenderChange,
        onBack = onBack,
        onConfirm = onConfirm
    )
}
