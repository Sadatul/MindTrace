package com.example.frontend.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.LocalDate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.frontend.api.models.PatientLog
import com.example.frontend.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.screens.components.DialogLog

// Helper function to check if notification permission is granted
fun checkNotificationPermission(context: Context): Boolean {
    return if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // For versions below Android 13, notification permissions were granted by default
        true
    }
}

// Helper function to determine if we should request notification permission
fun shouldRequestNotificationPermission(): Boolean {
    // Only request notification permission on Android 13 (API 33) and above
    return VERSION.SDK_INT >= VERSION_CODES.TIRAMISU
}

@RequiresApi(VERSION_CODES.O)
fun formatTimestamp(timestamp: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(timestamp)
        val formatter = DateTimeFormatter.ofPattern("h:mm a, d MMMM yyyy")
        zonedDateTime.format(formatter)
    } catch (_: Exception) {
        timestamp // Return original if parsing fails
    }
}

@RequiresApi(VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLogs(
    logs: List<PatientLog>,
    patientName: String? = null,
    profilePicture: String? = null,
    onBack: () -> Unit = {},
    isPatient: Boolean = false, // Add isPatient parameter
    isViewOnly: Boolean = false, // Add view-only mode for caregivers
    onAddLog: () -> Unit = {}, // Callback for Add Log
    onEditLog: (PatientLog) -> Unit = {}, // Callback for Edit Log
    onDeleteLog: (String) -> Unit = {}, // Callback for Delete Log
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
    onStartDateChange: (LocalDate?) -> Unit = {},
    onEndDateChange: (LocalDate?) -> Unit = {},
    onClearDateFilters: () -> Unit = {},
    hasNotificationPermission: Boolean = true, // Add notification permission status
    onRequestNotificationPermission: () -> Unit = {}, // Add callback to request permission
    navigationBar: NavigationBarComponent
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (patientName != null) "${patientName}'s Logs" else "My Logs",
                            style = typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                navigationIcon = {
                    if (!isPatient) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        if (profilePicture != null) {
                            AsyncImage(
                                model = profilePicture,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, colorResource(R.color.white), CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                error = painterResource(R.drawable.ic_launcher_foreground)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, colorResource(R.color.white), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Default Profile",
                                    modifier = Modifier.size(28.dp),
                                    tint = colorResource(R.color.white)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.card_patient),
                    titleContentColor = colorResource(R.color.white),
                    navigationIconContentColor = colorResource(R.color.white),
                    actionIconContentColor = colorResource(R.color.white)
                )
            )
        },
        floatingActionButton = {
            if (!isViewOnly) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 24.dp, bottom = 24.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = colorResource(R.color.gradient_patient_start),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .height(52.dp)
                            .width(150.dp)
                            .clickable(onClick = onAddLog)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Log",
                                modifier = Modifier.size(24.dp),
                                tint = colorResource(R.color.white)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Log",
                                color = colorResource(R.color.white),
                                style = typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            navigationBar.PatientNavigationBar(Screen.PatientLogs(null))

        },
        containerColor = colorResource(R.color.dark_surface)
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            // Notification Permission Warning Banner for patients
            if (isPatient && !hasNotificationPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Notifications are disabled. You won't receive important reminders.",
                                style = typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Button(
                            onClick = onRequestNotificationPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }

            // Date Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter by Date",
                            style = typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (startDate != null || endDate != null) {
                            TextButton(
                                onClick = onClearDateFilters,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear filters",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Date Picker
                        DatePickerButton(
                            label = "Start Date",
                            selectedDate = startDate,
                            onDateChange = onStartDateChange,
                            modifier = Modifier.weight(1f)
                        )

                        // End Date Picker
                        DatePickerButton(
                            label = "End Date",
                            selectedDate = endDate,
                            onDateChange = onEndDateChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main Content
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No logs available.", style = typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp), // Padding for FAB
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(logs) { log ->
                            LogItem(
                                log = log,
                                isViewOnly = isViewOnly, // Pass view-only mode to LogItem
                                onEditLog = { onEditLog(log) },
                                onDeleteLog = { onDeleteLog(log.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerButton(
    label: String,
    selectedDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (selectedDate != null) {
                    IconButton(
                        onClick = { onDateChange(null) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear $label",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "Select date",
                    style = typography.bodyMedium,
                    color = if (selectedDate != null)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            onDateChange(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@RequiresApi(VERSION_CODES.O)
@Composable
private fun LogItem(
    log: PatientLog,
    isViewOnly: Boolean = false, // Add view-only parameter
    onEditLog: () -> Unit = {},
    onDeleteLog: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.type.name, style = typography.titleMedium)
                Text(text = log.description, style = typography.bodyMedium)
                Text(text = formatTimestamp(log.createdAt), style = typography.bodySmall)
            }

            // Only show the menu if not in view-only mode
            if (!isViewOnly) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            tint = colorResource(R.color.gradient_patient_start)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Log") },
                            onClick = {
                                showMenu = false
                                onEditLog()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = colorResource(R.color.gradient_patient_start)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Log") },
                            onClick = {
                                showMenu = false
                                onDeleteLog()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = colorResource(R.color.gradient_patient_start)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(VERSION_CODES.O)
@Composable
fun MyLogsScreen(
    partnerId: String? = null,
    onBack: () -> Unit = {},
    isPatient: Boolean,
    viewModel: ViewModelLogs = viewModel(),
    navigationBar: NavigationBarComponent
) {
    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val patientInfo by viewModel.patientInfo.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val editingLog by viewModel.editingLog.collectAsState()
    val startDateString by viewModel.startDate.collectAsState()
    val endDateString by viewModel.endDate.collectAsState()

    // Convert date strings to LocalDate
    val startDate = startDateString?.let {
        try {
            ZonedDateTime.parse(it).toLocalDate()
        } catch (_: Exception) {
            null
        }
    }
    val endDate = endDateString?.let {
        try {
            ZonedDateTime.parse(it).toLocalDate()
        } catch (_: Exception) {
            null
        }
    }

    // Determine if this is view-only mode (caregiver viewing patient's logs)
    val isViewOnly = !isPatient && partnerId != null

    var showAddLogDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Notification permission handling
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    // Permission request launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Request notification permission when the screen is first displayed for patients
    LaunchedEffect(isPatient) {
        if (isPatient && !hasNotificationPermission && shouldRequestNotificationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Load logs when the composable is first displayed or when partnerId changes
    LaunchedEffect(partnerId) {
        viewModel.loadLogs(partnerId)
    }

    // Show error message if any
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorResource(R.color.gradient_patient_start))
        }
    } else {
        Box {
            MyLogs(
                logs = logs,
                patientName = patientInfo?.name,
                profilePicture = patientInfo?.profilePicture,
                onBack = onBack,
                isPatient = isPatient,
                isViewOnly = isViewOnly, // Pass view-only mode
                onAddLog = {
                    // Only allow if not in view-only mode
                    if (!isViewOnly) {
                        showAddLogDialog = true
                    }
                },
                onEditLog = { log ->
                    if (!isViewOnly) {
                        viewModel.startEditingLog(log)
                    }
                },
                onDeleteLog = { logId ->
                    if (!isViewOnly) {
                        logToDelete = logId
                        showDeleteConfirmDialog = true
                    }
                },
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = { date -> viewModel.setStartDate(date) },
                onEndDateChange = { date -> viewModel.setEndDate(date) },
                onClearDateFilters = { viewModel.clearDateFilters() },
                hasNotificationPermission = hasNotificationPermission,
                onRequestNotificationPermission = {
                    Log.d("ScreenMyLogs", "Enable button clicked, requesting notification permission")

                    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                        // Open app settings directly
                        // This is the most reliable way to ensure users can enable notifications
                        // especially if they've previously denied the permission
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)

                        Log.d("ScreenMyLogs", "Opening app settings for notification permission")
                    }
                },
                navigationBar = navigationBar
            )

            // Snackbar positioned at the bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                SnackbarHost(snackbarHostState)
            }
        }
    }

    // Add Log Dialog - only show if not in view-only mode
    if (showAddLogDialog && !isViewOnly) {
        DialogLog(
            show = true,
            onDismiss = { showAddLogDialog = false },
            onAdd = { logType, description, time ->
                viewModel.addLog(logType, description, time)
                showAddLogDialog = false
            }
        )
    }

    // Edit Log Dialog - only show if not in view-only mode
    if (!isViewOnly) {
        editingLog?.let { log ->
            DialogLog(
                show = true,
                onDismiss = { viewModel.stopEditingLog() },
                onAdd = { logType, description, _ ->
                    viewModel.updateLog(log.id, logType, description)
                },
                initialLogType = log.type,
                initialDescription = log.description,
                initialTime = log.createdAt,
                title = "Edit Log"
            )
        }
    }

    // Delete Confirmation Dialog - only show if not in view-only mode
    if (showDeleteConfirmDialog && !isViewOnly) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                logToDelete = null
            },
            title = { Text("Delete Log") },
            text = { Text("Are you sure you want to delete this log? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        logToDelete?.let { viewModel.deleteLog(it) }
                        showDeleteConfirmDialog = false
                        logToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        logToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@RequiresApi(VERSION_CODES.O)
@Composable
fun ScreenMyLogs(
    partnerId: String? = null,
    onBack: () -> Unit = {},
    isPatient: Boolean,
    navigationBar: NavigationBarComponent
) {
    MyLogsScreen(
        partnerId = partnerId,
        onBack = onBack,
        isPatient = isPatient,
        navigationBar = navigationBar
    )
}