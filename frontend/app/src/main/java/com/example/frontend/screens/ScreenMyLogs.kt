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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Add
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
    onAddLog: () -> Unit = {} // Callback for Add Log
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
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { if (isPatient) menuExpanded = true },
                        enabled = true,
                        modifier = Modifier
                            .padding(end = 20.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        if (profilePicture != null) {
                            AsyncImage(
                                model = profilePicture,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, colorResource(R.color.gradient_patient_start), CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                error = painterResource(R.drawable.ic_launcher_foreground)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Default Profile",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, colorResource(R.color.gradient_patient_start), CircleShape),
                                tint = colorResource(R.color.gradient_patient_start)
                            )
                        }
                        if (isPatient) {
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
                                            imageVector = Icons.Filled.AccountCircle,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
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
                            imageVector = Icons.Filled.Chat,
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
                        LogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: PatientLog) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = log.type.name, style = typography.titleMedium)
            Text(text = log.description, style = typography.bodyMedium)
            Text(text = log.createdAt, style = typography.bodySmall)
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
    viewModel: LogsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        DialogLog(
            show = showAddDialog,
            onDismiss = { showAddDialog = false },
            onAdd = { logType, logDescription ->
                viewModel.addLog(logType, logDescription)
                showAddDialog = false
            }
        )
    }

    // Fetch logs for the correct user (patient or selected partner)
    LaunchedEffect(partnerId) {
        viewModel.fetchLogs(partnerId)
    }

    when (uiState) {
        is LogsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is LogsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Failed to load logs.", style = typography.bodyMedium)
            }
        }
        is LogsUiState.Success -> {
            val state = uiState as LogsUiState.Success
            MyLogs(
                logs = state.logs,
                patientName = state.patientName,
                profilePicture = state.profilePicture,
                onBack = onBack,
                onAskAi = onAskAi,
                onMyProfile = onMyProfile,
                isPatient = isPatient,
                onAddLog = { showAddDialog = true }
            )
        }
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
