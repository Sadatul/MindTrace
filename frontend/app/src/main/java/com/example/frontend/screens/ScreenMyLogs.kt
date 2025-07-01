package com.example.frontend.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
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

@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp(timestamp: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(timestamp)
        val formatter = DateTimeFormatter.ofPattern("h:mm a, d MMMM yyyy")
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        timestamp // Return original if parsing fails
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLogs(
    logs: List<PatientLog>,
    patientName: String? = null,
    profilePicture: String? = null,
    onBack: () -> Unit = {},
    onAskAi: () -> Unit = {},
    onMyProfile: (() -> Unit)? = null, // Only for patient
    isPatient: Boolean = false, // Add isPatient parameter
    isViewOnly: Boolean = false, // Add view-only mode for caregivers
    onAddLog: () -> Unit = {}, // Callback for Add Log
    onEditLog: (PatientLog) -> Unit = {}, // Callback for Edit Log
    onDeleteLog: (String) -> Unit = {} // Callback for Delete Log
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
                        // Profile Picture or Google icon
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
                            // Google-style profile icon
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
                        
                        // Three-dot menu ONLY for patients viewing their own logs
                        if (isPatient) {
                            var menuExpanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Menu",
                                    tint = colorResource(R.color.white),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("My Profile") },
                                    onClick = {
                                        menuExpanded = false
                                        onMyProfile?.invoke()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = null
                                        )
                                    }
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
            // Only show FABs if not in view-only mode
            if (!isViewOnly) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = colorResource(R.color.gradient_patient_start),
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .size(width = 100.dp, height = 80.dp)
                            .clickable(onClick = onAddLog) // Wire up to onAddLog
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Log",
                                modifier = Modifier.size(32.dp),
                                tint = colorResource(R.color.white)
                            )
                            Text(
                                text = "Add Log",
                                color = colorResource(R.color.white),
                                style = typography.titleMedium.copy(fontSize = typography.bodyLarge.fontSize),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = colorResource(R.color.gradient_patient_start),
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .size(width = 100.dp, height = 80.dp)
                            .clickable(onClick = onAskAi)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "ASK AI",
                                modifier = Modifier.size(32.dp),
                                tint = colorResource(R.color.white)
                            )
                            Text(
                                text = "ASK AI",
                                color = colorResource(R.color.white),
                                style = typography.titleMedium.copy(fontSize = typography.bodyLarge.fontSize),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }, floatingActionButtonPosition = FabPosition.Center,
        containerColor = colorResource(R.color.dark_surface)
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
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

@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyLogsScreen(
    partnerId: String? = null,
    onBack: () -> Unit = {},
    onAskAi: () -> Unit = {},
    onMyProfile: (() -> Unit)? = null,
    isPatient: Boolean,
    viewModel: ViewModelLogs = viewModel()
) {
    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val patientInfo by viewModel.patientInfo.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val editingLog by viewModel.editingLog.collectAsState()
    
    // Determine if this is view-only mode (caregiver viewing patient's logs)
    val isViewOnly = !isPatient && partnerId != null
    
    var showAddLogDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
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
                onAskAi = onAskAi,
                onMyProfile = onMyProfile,
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
                }
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
            onAdd = { logType, description ->
                viewModel.addLog(logType, description)
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
                onAdd = { logType, description ->
                    viewModel.updateLog(log.id, logType, description)
                },
                initialLogType = log.type,
                initialDescription = log.description,
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenMyLogs(
    partnerId: String? = null,
    onBack: () -> Unit = {},
    onAskAi: () -> Unit = {},
    onMyProfile: (() -> Unit)? = null,
    isPatient: Boolean,
) {
    MyLogsScreen(
        partnerId = partnerId,
        onBack = onBack,
        onAskAi = onAskAi,
        onMyProfile = onMyProfile,
        isPatient = isPatient
    )
}
