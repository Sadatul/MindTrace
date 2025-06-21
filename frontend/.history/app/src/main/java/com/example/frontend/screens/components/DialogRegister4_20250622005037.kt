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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDialog(
    title: String,
    name: String,
    email: String,
    dob: String, // Expect "yyyy-MM-dd" from parent, or empty
    gender: String, // Expect "M", "F", or empty from parent
    profilePictureUrl: String?,
    additionalFields: @Composable () -> Unit = {},
    infoMessage: String,
    onDobChange: (String) -> Unit, // Callback with "yyyy-MM-dd"
    onGenderChange: (String) -> Unit, // Callback with gender code "M" or "F"
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = null)

    LaunchedEffect(Unit) { // Run once when the dialog enters composition
        if (dob.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = dateFormat.parse(dob)
                parsedDate?.let {
                    datePickerState.selectedDateMillis = it.time
                }
            } catch (e: Exception) {
                println("RegisterDialog: Error parsing initial DOB '$dob' for picker: ${e.message}")
            }
        }
    }

    val formattedDisplayDate by remember(dob) { // Recompute only if dob prop changes
        derivedStateOf {
            if (dob.isNotEmpty()) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val parsedDobDate = inputFormat.parse(dob)
                    parsedDobDate?.let { outputFormat.format(it) } ?: "Select Date of Birth"
                } catch (e: Exception) {
                    println("RegisterDialog: Error formatting DOB '$dob' for display: ${e.message}")
                    "Select Date of Birth"
                }
            } else {
                "Select Date of Birth"
            }
        }
    }

    val genderOptions = listOf(
        "Male" to Icons.Default.Male,      // Male icon
        "Female" to Icons.Default.Female   // Female icon
    )

    // Dark theme colors
    val darkBackground = colorResource(R.color.dark_background)
    val darkSurface = colorResource(R.color.dark_surface)
    val darkPrimary = colorResource(R.color.dark_primary)
    val darkOnSurface = colorResource(R.color.dark_on_surface)
    val darkOnPrimary = colorResource(R.color.dark_on_primary)
    val darkSurfaceVariant = colorResource(R.color.dark_surface_variant)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = darkBackground,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = darkOnSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = darkOnSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = darkSurface,
                        titleContentColor = darkOnSurface,
                        navigationIconContentColor = darkOnSurface
                    )
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = darkSurface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = darkOnSurface,
                                containerColor = Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, darkOnSurface.copy(alpha = 0.5f))
                        ) {
                            Text(
                                "Cancel",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = darkOnSurface
                            )
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = darkPrimary,
                                contentColor = darkOnPrimary
                            )
                        ) {
                            Text(
                                "Register",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // Profile Picture Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape
                            ),
                        shape = CircleShape,
                        color = darkSurface
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profilePictureUrl)
                                .crossfade(true)
                                .error(android.R.drawable.stat_notify_error)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Basic Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = darkSurfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Personal Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = darkPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        ReadOnlyField(
                            label = "Full Name", 
                            value = name, 
                            icon = Icons.Default.Person,
                            darkTheme = true,
                            darkPrimary = darkPrimary,
                            darkOnSurface = darkOnSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ReadOnlyField(
                            label = "Email Address", 
                            value = email, 
                            icon = Icons.Default.Email,
                            darkTheme = true,
                            darkPrimary = darkPrimary,
                            darkOnSurface = darkOnSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Date of Birth Field
                AdvancedDateField(
                    value = formattedDisplayDate,
                    onClick = {
                        if (dob.isNotEmpty()) {
                            try {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val parsedDate = dateFormat.parse(dob)
                                datePickerState.selectedDateMillis = parsedDate?.time
                            } catch (e: Exception) {
                                println("RegisterDialog: Error setting picker from current dob '$dob': ${e.message}")
                                datePickerState.selectedDateMillis = null
                            }
                        } else {
                            datePickerState.selectedDateMillis = null
                        }
                        showDatePicker = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = true,
                    darkSurface = darkSurface,
                    darkPrimary = darkPrimary,
                    darkOnSurface = darkOnSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Gender Field
                AdvancedGenderField(
                    selectedGender = gender,
                    genderOptions = genderOptions,
                    isExpanded = showGenderDropdown,
                    onExpandedChange = { showGenderDropdown = it },
                    onGenderSelected = { selectedDisplayGender ->
                        val backendValue = when (selectedDisplayGender) {
                            "Male" -> "M"
                            "Female" -> "F"
                            else -> ""
                        }
                        onGenderChange(backendValue)
                        showGenderDropdown = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = true,
                    darkSurface = darkSurface,
                    darkPrimary = darkPrimary,
                    darkOnSurface = darkOnSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Additional Fields
                additionalFields()

                Spacer(modifier = Modifier.height(24.dp))

                // Info Message
                if (infoMessage.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = darkSurfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = infoMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            color = darkOnSurface,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Extra space at bottom for better scrolling
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            onDobChange(dateFormat.format(Date(millis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(
                        "Confirm",
                        fontWeight = FontWeight.Bold,
                        color = darkPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        "Cancel",
                        color = darkOnSurface
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = darkSurface,
                    headlineContentColor = darkPrimary,
                    titleContentColor = darkOnSurface,
                    selectedDayContentColor = darkOnPrimary,
                    selectedDayContainerColor = darkPrimary,
                    todayContentColor = darkPrimary,
                    todayDateBorderColor = darkPrimary,
                    weekdayContentColor = darkOnSurface,
                    yearContentColor = darkOnSurface,
                    currentYearContentColor = darkPrimary,
                    dayContentColor = darkOnSurface,
                    navigationContentColor = darkOnSurface,
                    dividerColor = darkOnSurface.copy(alpha = 0.3f)
                ),
                title = {
                    Text(
                        "Select Date of Birth",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = darkOnSurface
                    )
                },
                headline = {
                    val headlineText = datePickerState.selectedDateMillis?.let {
                        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Choose your birth date"
                    Text(
                        headlineText,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = darkPrimary
                    )
                }
            )
        }
    }
}

@Composable
private fun ReadOnlyField(
    label: String, 
    value: String, 
    icon: ImageVector,
    darkTheme: Boolean = false,
    darkPrimary: Color = colorResource(R.color.dark_primary),
    darkOnSurface: Color = colorResource(R.color.dark_on_surface)
) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = {
            Text(
                label,
                fontWeight = FontWeight.Medium,
                color = if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary
            )
        },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = label,
                tint = if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary
            )
        },
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            color = if (darkTheme) darkOnSurface else MaterialTheme.colorScheme.onSurface
        ),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = if (darkTheme) darkOnSurface else MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = if (darkTheme) darkOnSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            disabledLeadingIconColor = if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary,
            disabledLabelColor = if (darkTheme) darkPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            disabledPlaceholderColor = if (darkTheme) darkOnSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    )
}

@Composable
private fun AdvancedDateField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    darkSurface: Color = colorResource(R.color.dark_surface),
    darkPrimary: Color = colorResource(R.color.dark_primary),
    darkOnSurface: Color = colorResource(R.color.dark_on_surface)
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (darkTheme) darkPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) darkSurface else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date of Birth",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value == "Select Date of Birth")
                        if (darkTheme) darkOnSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else
                        if (darkTheme) darkOnSurface else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (value == "Select Date of Birth") FontWeight.Normal else FontWeight.SemiBold
                )
            }
            Surface(
                shape = CircleShape,
                color = if (darkTheme) darkPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = if (darkTheme) darkPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
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
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    darkSurface: Color = Color.White,
    darkPrimary: Color = Color.Blue,
    darkOnSurface: Color = Color.Black
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "GenderExpandIconRotation"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isExpanded)
            if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary
        else
            if (darkTheme) darkPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        animationSpec = tween(durationMillis = 300),
        label = "GenderBorderColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (darkTheme) darkSurface else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300),
        label = "GenderContainerColor"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) }
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isExpanded) 6.dp else 4.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val displayGenderString = when (selectedGender) {
                            "M" -> "Male"
                            "F" -> "Female"
                            else -> ""
                        }

                        val selectedOption = genderOptions.find { it.first == displayGenderString }
                        val displayIcon = selectedOption?.second ?: Icons.Default.QuestionMark
                        val displayText = if (displayGenderString.isNotEmpty()) displayGenderString else "Select Gender"

                        Icon(
                            imageVector = displayIcon,
                            contentDescription = null,
                            tint = if (selectedGender.isNotEmpty())
                                if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary
                            else
                                if (darkTheme) darkOnSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedGender.isEmpty())
                                if (darkTheme) darkOnSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else
                                if (darkTheme) darkOnSurface else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selectedGender.isEmpty()) FontWeight.Normal else FontWeight.SemiBold
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(28.dp)
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) },
            properties = PopupProperties(focusable = true),
            modifier = Modifier
                .fillMaxWidth()
                .background(if (darkTheme) darkSurface else MaterialTheme.colorScheme.surface)
        ) {
            genderOptions.forEach { (optionText, icon) ->
                val isCurrentlySelected = when (selectedGender) {
                    "M" -> optionText == "Male"
                    "F" -> optionText == "Female"
                    else -> false
                }
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isCurrentlySelected)
                                    if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary
                                else
                                    if (darkTheme) darkOnSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isCurrentlySelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isCurrentlySelected)
                                    if (darkTheme) darkPrimary else MaterialTheme.colorScheme.primary
                                else
                                    if (darkTheme) darkOnSurface else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = { onGenderSelected(optionText) },
                    modifier = Modifier.background(
                        if (isCurrentlySelected)
                            if (darkTheme) darkPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else
                            Color.Transparent
                    )
                )
            }
        }
    }
}