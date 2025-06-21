package com.example.frontend.screens.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun RegisterPromptDialog(onDismiss: () -> Unit, onRegister: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Registration") },
        text = { Text("It looks like you don't have an account yet. Would you like to register?") },
        confirmButton = {
            TextButton(onClick = onRegister) { Text("Register") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
