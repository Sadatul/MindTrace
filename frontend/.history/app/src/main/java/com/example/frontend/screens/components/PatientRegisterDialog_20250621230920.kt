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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // <-- Added import
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
fun PatientRegisterDialog(
    name: String,
    email: String,
    dob: String,
    gender: String,
    primaryContact: String,
    otp: String,
    profilePictureUrl: String?,
    onDobChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onPrimaryContactChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val selectedDateByPicker = datePickerState.selectedDateMillis // Renamed for clarity

    // Initialize date picker with current dob if available, only if not already set by picker
    LaunchedEffect(dob, datePickerState.selectedDateMillis) {
        if (dob.isNotEmpty() && datePickerState.selectedDateMillis == null) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = dateFormat.parse(dob)
                parsedDate?.let {
                    datePickerState.selectedDateMillis = it.time
                }
            } catch (e: Exception) {
                // Handle parsing error gracefully, e.g., log it
                println("PatientRegisterDialog: Error parsing initial DOB '$dob': ${e.message}")
            }
        }
    }

    val formattedDate by remember {
        derivedStateOf {
            selectedDateByPicker?.let { // Use the value directly from the picker state first
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
            } ?: if (dob.isNotEmpty()) { // Fallback to parsing initial dob string if picker date is null
                try {
                    // Try to parse the dob string coming from props if it's in yyyy-MM-dd
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val parsedDob = inputFormat.parse(dob)
                    parsedDob?.let { outputFormat.format(it) } ?: dob // If parsing fails, show raw dob
                } catch (_: Exception) {
                    dob
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
                "Patient Registration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()) {

                // Profile Picture and User Info Card
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
                                .shadow(
                                    elevation = 4.dp,
                                    shape = CircleShape
                                ),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profilePictureUrl)
                                    .crossfade(true)
                                    .error(android.R.drawable.stat_notify_error) // Placeholder for error
                                    .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder while loading
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
                    value = formattedDate,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                AdvancedGenderField(
                    selectedGender = gender,
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

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = primaryContact,
                            onValueChange = onPrimaryContactChange,
                            label = { Text("Primary Contact (Caregiver UID)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = otp,
                            onValueChange = onOtpChange,
                            label = { Text("OTP") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        "Complete your patient profile. The OTP and Primary Contact ID are provided by your caregiver.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    "Register",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Use selectedDateByPicker which directly comes from the DatePickerState
                        selectedDateByPicker?.let {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            onDobChange(dateFormat.format(Date(it)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(
                        "Confirm",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Select Date of Birth",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom =12.dp), // Adjusted padding
                        style = MaterialTheme.typography.titleLarge // Changed to titleLarge for better hierarchy
                    )
                },
                headline = {
                    // Use selectedDateByPicker for the headline as well
                    val headlineText = selectedDateByPicker?.let {
                        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Choose your birth date"
                    Text(
                        headlineText,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp), // Adjusted padding
                        style = MaterialTheme.typography.labelLarge // Changed for concise display
                    )
                }
                // You can also add showModeToggle = true/false if you want to allow switching input modes
            )
        }
    }
}

@Composable
private fun AdvancedDateField(
    value: String,
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Reduced elevation slightly
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Adjusted padding
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
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Softer color for placeholder
                    else
                        MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (value == "Select Date of Birth") FontWeight.Normal else FontWeight.Medium
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp) // Slightly smaller icon background
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(7.dp) // Adjusted padding for icon
                )
            }
        }
    }
}

@Composable
private fun AdvancedGenderField(
    selectedGender: String,
    genderOptions: List<Pair<String, ImageVector>>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onGenderSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300), label = "GenderExpandIconRotation" // Added label
    )
    val borderColor by animateColorAsState( // Animate border color
        targetValue = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 300), label = "GenderBorderColor"
    )
    val containerColor by animateColorAsState( // Animate container color
        targetValue = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300), label = "GenderContainerColor"
    )


    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) }
                .border(
                    width = 1.dp,
                    color = borderColor, // Use animated border color
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = containerColor // Use animated container color
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isExpanded) 2.dp else 1.dp // Adjusted elevation
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp), // Adjusted padding
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
                    imageVector = Icons.Default.ExpandMore, // Keep ExpandMore, rely on rotation
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .rotate(rotationAngle) // Rotation will show expand/collapse
                        .size(24.dp)
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) },
            properties = PopupProperties(focusable = true), // Ensures keyboard focus works as expected
            modifier = Modifier.fillMaxWidth() // Consider setting a width constraint if needed, e.g. .width(IntrinsicSize.Min) on the Box
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = { onGenderSelected(option) },
                    modifier = Modifier.background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else
                            Color.Transparent
                    )
                )
            }
        }
    }
}