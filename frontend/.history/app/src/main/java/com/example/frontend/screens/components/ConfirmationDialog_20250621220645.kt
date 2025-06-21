package com.example.frontend.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmationDialog(
    role: String,
    onConfirm: () -> Unit, // Proceeds to the next step (form)
    onBackToRoleSelection: () -> Unit, // Goes back to RoleSelectionDialog
    onDismissDialog: () -> Unit // Handles general dismiss (e.g., tapping outside, system back)
) {
    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = { Text("Confirm Role: ${role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}") },
        text = { Text("You have selected the role of a ${role.lowercase()}. Do you want to continue registration as a ${role.lowercase()}?") },
        confirmButton = {
            // Row to arrange buttons horizontally
            Row(
                modifier = Modifier.fillMaxWidth(), // Occupy full width to allow spacing
                horizontalArrangement = Arrangement.End // Align buttons to the end (right)
            ) {
                // "Back" button (now the dismiss action in this context)
                TextButton(
                    onClick = onBackToRoleSelection,
                    modifier = Modifier.padding(end = 8.dp) // Add some spacing between buttons
                ) {
                    Text("Back")
                }
                // "Continue" button (the confirm action)
                TextButton(onClick = onConfirm) {
                    Text("Continue as ${role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}")
                }
            }
        }
    )
}
