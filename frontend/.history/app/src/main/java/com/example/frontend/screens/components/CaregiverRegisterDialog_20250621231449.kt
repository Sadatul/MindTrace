package com.example.frontend.screens.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverRegisterDialog(
    name: String,
    email: String,
    dob: String, // Expected in "yyyy-MM-dd" format from parent, or empty
    gender: String, // Expected display string like "Male", "Female", or empty from parent
    profilePictureUrl: String?,
    onDobChange: (String) -> Unit, // Callback with "yyyy-MM-dd"
    onGenderChange: (String) -> Unit, // Callback with gender code like "M", "F"
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null // Initialize to null
    )

    // Initialize date picker with current dob if available (once)
    LaunchedEffect(Unit) { // Use Unit to run once on dialog composition
        if (dob.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = dateFormat.parse(dob)
                parsedDate?.let {
                    datePickerState.selectedDateMillis = it.time
                }
            } catch (e: Exception) {
                println("CaregiverRegisterDialog: Error parsing initial DOB '$dob' for picker: ${e.message}")
            }
        }
    }

    // This is the date string displayed in the AdvancedDateField.
    // It's derived from the 'dob' prop.
    val formattedDisplayDate by remember(dob) {
        derivedStateOf {
            if (dob.isNotEmpty()) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val parsedDobDate = inputFormat.parse(dob)
                    parsedDobDate?.let { outputFormat.format(it) } ?: "Select Date of Birth"
                } catch (e: Exception) {
                    println("CaregiverRegisterDialog: Error formatting DOB '$dob' for display: ${e.message}")
                    "Select Date of Birth" // Fallback if dob is not in "yyyy-MM-dd"
                }
            } else {
                "Select Date of Birth"
            }
        }
    }

    val genderOptions = listOf(
        "Male" to Icons.Default.Male,
        "Female" to Icons.Default.Female
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Caregiver Registration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(elevation = 4.dp, shape = CircleShape),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profilePictureUrl)
                                    .crossfade(true)
                                    .error(android.R.drawable.stat_notify_error)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AdvancedDateField(
                    value = formattedDisplayDate, // Use the derived state from 'dob'
                    onClick = {
                        // When opening the picker, ensure its state matches current 'dob'
                        if (dob.isNotEmpty()) {
                            try {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val parsedDate = dateFormat.parse(dob)
                                datePickerState.selectedDateMillis = parsedDate?.time
                            } catch (e: Exception) {
                                println("CaregiverRegisterDialog: Error setting picker from current dob '$dob': ${e.message}")
                                datePickerState.selectedDateMillis = null // Clear selection if current dob is invalid
                            }
                        } else {
                            datePickerState.selectedDateMillis = null // No current dob, so picker starts fresh
                        }
                       showDatePicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AdvancedGenderField(
                    selectedGender = gender, // Parent provides display string e.g. "Male"
                    genderOptions = genderOptions,
                    isExpanded = showGenderDropdown,
                    onExpandedChange = { showGenderDropdown = it },
                    onGenderSelected = { selectedGender ->
                        // Convert display name to backend code
                        val backendValue = when (selectedGender) {
                            "Male" -> "M"
                            "Female" -> "F"
                            else -> selectedGender
                        }
                        onGenderChange(backendValue)
                        showGenderDropdown = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        "Complete your caregiver profile by providing your date of birth and gender information.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Register", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            onDobChange(dateFormat.format(Date(millis))) // THIS IS KEY - updates parent
                        }
                        showDatePicker = false
                    }
                ) { Text("Confirm", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                title = {
                    Text(
                        "Select Date of Birth",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                headline = {
                    val headlineText = datePickerState.selectedDateMillis?.let {
                        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Choose your birth date"
                    Text(
                        headlineText,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

@Composable
private fun AdvancedDateField(
    value: String, // Expects formatted date string e.g. "01 Jan 2023" or "Select Date of Birth"
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date of Birth",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value == "Select Date of Birth")
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (value == "Select Date of Birth") FontWeight.Normal else FontWeight.Medium
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(7.dp)
                )
            }
        }
    }
}

@Composable
private fun AdvancedGenderField(
    selectedGender: String, // Expects display string e.g. "Male"
    genderOptions: List<Pair<String, ImageVector>>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onGenderSelected: (String) -> Unit, // Callback with display string e.g. "Male"
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300), label = "GenderFieldExpandIconRotation"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 300), label = "GenderFieldBorderColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300), label = "GenderFieldContainerColor"
    )

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) }
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Convert backend code to display name
                        val displayGender = when (selectedGender) {
                            "M" -> "Male"
                            "F" -> "Female"
                            else -> selectedGender
                        }
                        val selectedOption = genderOptions.find { it.first == displayGender }
                        val displayIcon = selectedOption?.second ?: Icons.Default.QuestionMark
                        val displayText = if (displayGender.isNotEmpty()) displayGender else "Select Gender"

                        Icon(
                            imageVector = displayIcon,
                            contentDescription = null,
                            tint = if (selectedGender.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedGender.isEmpty())
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selectedGender.isEmpty()) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle).size(24.dp)
                )
            }
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) },
            properties = PopupProperties(focusable = true),
            modifier = Modifier.fillMaxWidth()
        ) {
            genderOptions.forEach { (option, icon) ->
                // Convert backend code to display name for comparison
                val displayGender = when (selectedGender) {
                    "M" -> "Male"
                    "F" -> "Female"
                    else -> selectedGender
                }
                val isSelected = option == displayGender
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = icon, contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option, style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = { onGenderSelected(option) }, // Sends display string like "Male"
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent
                    )
                )
            }
        }
    }
}