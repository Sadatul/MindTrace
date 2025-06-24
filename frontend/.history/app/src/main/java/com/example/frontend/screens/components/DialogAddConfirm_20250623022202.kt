package com.example.frontend.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun DialogAddConfirm(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    id: String,
    onIdChange: (String) -> Unit,
    loading: Boolean,
    errorMessage: String?,
    confirmButtonText: String = "Add",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Caregiver Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = id,
                    onValueChange = onIdChange,
                    label = { Text("Caregiver ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
                if (loading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !loading && name.isNotBlank() && id.isNotBlank()) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !loading) {
                Text("Cancel")
            }
        }
    )
}
