package com.example.frontend.screens.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.unit.dp
import com.example.frontend.api.models.LogType
import com.example.frontend.api.models.logTypeToString
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogLog(
    show: Boolean,
    onDismiss: () -> Unit,
    onAdd: (LogType, String, Int) -> Unit,
    initialLogType: LogType? = null,
    initialDescription: String = "",
    initialTime: String? = null,
    title: String = "Add Log"
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedLogType by remember(initialLogType) { mutableStateOf(initialLogType) }
    var logDescription by remember(initialDescription) { mutableStateOf(initialDescription) }
    var timeSliderValue by remember { mutableStateOf(0f) }
    
    val logTypeOptions = LogType.entries.toTypedArray()
    
    // Time options: Now, 5min, 10min, 15min, 30min, 1hr, 2hr, 3hr, 4hr, 5hr
    val timeOptions = listOf(
        0,      // Now
        5,      // 5 minutes ago
        10,     // 10 minutes ago
        15,     // 15 minutes ago
        30,     // 30 minutes ago
        60,     // 1 hour ago
        120,    // 2 hours ago
        180,    // 3 hours ago
        240,    // 4 hours ago
        300     // 5 hours ago
    )
    
    fun getTimeText(minutes: Int): String {
        return when (minutes) {
            0 -> "Now"
            in 1..59 -> "$minutes minutes ago"
            60 -> "1 hour ago"
            else -> "${minutes / 60} hours ago"
        }
    }

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
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
                    
                    // Time Slider Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "When did this happen?",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val selectedTimeIndex = timeSliderValue.roundToInt()
                        val selectedMinutes = timeOptions[selectedTimeIndex]
                        
                        Text(
                            text = getTimeText(selectedMinutes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Slider(
                            value = timeSliderValue,
                            onValueChange = { timeSliderValue = it },
                            valueRange = 0f..(timeOptions.size - 1).toFloat(),
                            steps = timeOptions.size - 2, // steps between min and max
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedLogType?.let {
                            val selectedTimeIndex = timeSliderValue.roundToInt()
                            val selectedMinutes = timeOptions[selectedTimeIndex]
                            Log.d("DialogLog", "Add log: type=${it.name}, description=$logDescription, minutes ago=$selectedMinutes")
                            onAdd(it, logDescription, selectedMinutes)
                        }
                    },
                    enabled = selectedLogType != null && logDescription.isNotBlank()
                ) {
                    Text("Save")
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
