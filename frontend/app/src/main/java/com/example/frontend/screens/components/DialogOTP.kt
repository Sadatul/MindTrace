package com.example.frontend.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DialogOTP(
    title: String = "Enter OTP",
    onConfirm: (String) -> Unit, // Corrected: Removed @Composable
    onDismiss: () -> Unit,
    errorMessage: String? = null,
    loading: Boolean = false
) {
    var otp by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Added for better theming
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface // Added for better theming
                )

                Spacer(modifier = Modifier.height(16.dp)) // Increased spacing

                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it }, // Example: Limit OTP length
                    label = { Text("One-Time Password") }, // More descriptive label
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), // Keeps OTP hidden
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    isError = !errorMessage.isNullOrBlank(), // Show error state on text field
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                    )
                )

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall, // Slightly smaller error text
                        modifier = Modifier.fillMaxWidth() // Allow error text to wrap
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !loading
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onConfirm(otp) // This is now a valid call
                        },
                        enabled = otp.isNotBlank() && !loading,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp) // Added some elevation
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary, // Good contrast on button
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}