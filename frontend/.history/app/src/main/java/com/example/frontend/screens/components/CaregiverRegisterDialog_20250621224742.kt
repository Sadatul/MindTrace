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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Female
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverRegisterDialog(
    name: String, 
    email: String, 
    dob: String, 
    gender: String,
    profilePictureUrl: String?,
    onNameChange: (String) -> Unit, 
    onEmailChange: (String) -> Unit, 
    onDobChange: (String) -> Unit, 
    onGenderChange: (String) -> Unit,
    onDismiss: () -> Unit, 
    onConfirm: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis
    
    // Initialize date picker with current dob if available
    remember(dob) {
        if (dob.isNotEmpty() && datePickerState.selectedDateMillis == null) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(dob)
                date?.let {
                    datePickerState.selectedDateMillis = it.time
                }
            } catch (e: Exception) {
                // Handle parsing error gracefully
            }
        }
    }
    
    val formattedDate by remember {
        derivedStateOf {
            selectedDate?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
            } ?: if (dob.isNotEmpty()) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val date = inputFormat.parse(dob)
                    date?.let { outputFormat.format(it) } ?: dob
                } catch (e: Exception) {
                    dob
                }
            } else {
                "Select Date of Birth"
            }
        }
    }
    
    val genderOptions = listOf(
        "Male" to Icons.Default.Male,
        "Female" to Icons.Default.Female,
        "Other" to Icons.Default.Person,
        "Prefer not to say" to Icons.Default.QuestionMark
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
                        // Enhanced Profile Picture with shadow
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

                // Enhanced Date of Birth Field
                AdvancedDateField(
                    value = formattedDate,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enhanced Gender Field
                AdvancedGenderField(
                    selectedGender = gender,
                    genderOptions = genderOptions,
                    isExpanded = showGenderDropdown,
                    onExpandedChange = { showGenderDropdown = it },
                    onGenderSelected = { selectedGender ->
                        onGenderChange(selectedGender)
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
    
    // Enhanced Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate?.let {
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
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                headline = {
                    Text(
                        selectedDate?.let {
                            SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "Choose your birth date",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (value == "Select Date of Birth") FontWeight.Normal else FontWeight.Medium
                )
            }
            
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
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
        animationSpec = tween(durationMillis = 300), label = ""
    )

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) }
                .border(
                    width = 1.dp,
                    color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isExpanded) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else 
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isExpanded) 4.dp else 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        val selectedOption = genderOptions.find { it.first == selectedGender }
                        selectedOption?.let { (_, icon) ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = selectedGender.ifEmpty { "Select Gender" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedGender.isEmpty()) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selectedGender.isEmpty()) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(24.dp)
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
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (option == selectedGender) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (option == selectedGender) FontWeight.Bold else FontWeight.Normal,
                                color = if (option == selectedGender) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = { onGenderSelected(option) },
                    modifier = Modifier.background(
                        if (option == selectedGender) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else 
                            Color.Transparent
                    )
                )
            }
        }
    }
}
