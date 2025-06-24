package com.example.frontend.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogAddPatientFull(
    name: String,
    onNameChange: (String) -> Unit,
    id: String,
    onIdChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    dob: String,
    onDobChange: (String) -> Unit,
    loading: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Patient Details", fontSize = MaterialTheme.typography.headlineSmall.fontSize) },
        text = {
            Column(modifier = Modifier.widthIn(min = 320.dp, max = 420.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Patient Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = id,
                    onValueChange = onIdChange,
                    label = { Text("Patient ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = onGenderChange,
                    label = { Text("Gender (M/F/O)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = dob,
                    onValueChange = onDobChange,
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
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
            Button(onClick = onConfirm, enabled = !loading && name.isNotBlank() && id.isNotBlank() && gender.isNotBlank() && dob.isNotBlank()) {
                Text("Next")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !loading) {
                Text("Cancel")
            }
        }
    )
}
