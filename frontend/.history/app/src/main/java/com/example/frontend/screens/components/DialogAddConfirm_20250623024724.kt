package com.example.frontend.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        title = { Text(title, fontSize = MaterialTheme.typography.headlineSmall.fontSize) },
        text = {
            Column(modifier = Modifier.widthIn(min = 320.dp, max = 420.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Caregiver Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = id,
                    onValueChange = onIdChange,
                    label = { Text("Caregiver ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
                if (loading) {
                    Spacer(modifier = Modifier.height(12.dp))
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
