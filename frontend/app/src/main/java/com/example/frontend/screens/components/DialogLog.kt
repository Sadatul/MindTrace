package com.example.frontend.screens.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.frontend.api.models.LogType
import com.example.frontend.api.models.logTypeToString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogLog(
    show: Boolean,
    onDismiss: () -> Unit,
    onAdd: (LogType, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedLogType by remember { mutableStateOf<LogType?>(null) }
    var logDescription by remember { mutableStateOf("") }
    val logTypeOptions = LogType.entries.toTypedArray()

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Log") },
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedLogType?.let { logTypeToString(it) } ?: "",
                            onValueChange = {},
                            label = { Text("Type") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clickable { expanded = true }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            logTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(logTypeToString(option)) },
                                    onClick = {
                                        selectedLogType = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = logDescription,
                        onValueChange = { logDescription = it },
                        label = { Text("Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedLogType?.let {
                            Log.d("DialogLog", "Add log: type=${it.name}, description=$logDescription")
                            onAdd(it, logDescription)
                        }
                    },
                    enabled = selectedLogType != null && logDescription.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
