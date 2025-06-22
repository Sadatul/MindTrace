package com.example.frontend.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
import kotlinx.coroutines.launch

@Composable
fun DialogAddPartner(
    isPatientView: Boolean,
    onDismiss: () -> Unit,
    onOtpRequested: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var patientId by remember { mutableStateOf("") }
    var patientInfo by remember { mutableStateOf<UserInfo?>(null) }
    var patientInfoError by remember { mutableStateOf<String?>(null) }
    var patientInfoLoading by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (isPatientView) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Not Supported") },
            text = { Text("You cannot add caregivers directly. Please ask your primary contact (caregiver) to add a new caregiver for you.") },
            confirmButton = {
                Button(onClick = onDismiss) { Text("OK") }
            },
            dismissButton = {}
        )
        return
    }

    if (showConfirmDialog && patientInfo != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Patient Details") },
            text = {
                Column {
                    Text("Name: ${'$'}{patientInfo!!.name}")
                    Text("Gender: ${'$'}{patientInfo!!.gender}")
                    Text("DOB: ${'$'}{patientInfo!!.dob}")
                    Text("Email: ${'$'}{patientInfo!!.email}")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    onOtpRequested(patientId)
                }) { Text("Yes, add ${'$'}{patientInfo!!.name}") }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Patient") },
        text = {
            Column {
                OutlinedTextField(
                    value = patientId,
                    onValueChange = {
                        patientId = it
                        patientInfo = null
                        patientInfoError = null
                    },
                    label = { Text("Patient ID") },
                    singleLine = true,
                    modifier = Modifier
                )
                if (patientInfoError != null) {
                    Text(patientInfoError!!, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
                if (patientInfoLoading) {
                    CircularProgressIndicator(modifier = Modifier)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    patientInfoLoading = true
                    patientInfoError = null
                    coroutineScope.launch {
                        try {
                            val api = RetrofitInstance.dementiaAPI
                            val token = api.getIdToken()
                            val response = api.getUserInfo("Bearer $token", userId = patientId)
                            if (response.isSuccessful) {
                                patientInfo = response.body()
                                if (patientInfo != null) {
                                    showConfirmDialog = true
                                } else {
                                    patientInfoError = "Patient not found."
                                }
                            } else {
                                patientInfoError = "Patient not found."
                            }
                        } catch (_: Exception) {
                            patientInfoError = "Error: ${'$'}{e.message}"
                        } finally {
                            patientInfoLoading = false
                        }
                    }
                },
                enabled = patientId.isNotBlank() && !patientInfoLoading
            ) { Text("Find Patient") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
