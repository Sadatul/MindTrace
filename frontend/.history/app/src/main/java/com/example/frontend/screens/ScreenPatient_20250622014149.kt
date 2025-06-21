package com.example.frontend.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.signOutUser
import kotlinx.coroutines.launch

private const val TAG = "ScreenPatient"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPatient(
    errorMsg: String? = null,
    onNavigateToChat: () -> Unit = {},
    onLoginWithAnotherAccount: () -> Unit = {},
    onShowCloseAppDialog: () -> Unit = {}
) {
    var userInfo: UserInfo? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    var showProfileMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info", e)
            // Optionally, update an error state that ScreenPatient can display
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = colorResource(R.color.dark_background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Patient Dashboard",
                        color = colorResource(R.color.dark_on_surface),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box {                        IconButton(onClick = { showProfileMenu = true }) {
                            // Always show Google account icon since app doesn't use profile pictures
                            Icon(
                                imageVector = Icons.Default.AccountBox,
                                contentDescription = "Google Account",
                                tint = colorResource(R.color.dark_primary),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false },
                            modifier = Modifier.background(colorResource(R.color.dark_surface)),
                            properties = PopupProperties(focusable = true)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Sign Out",
                                            tint = colorResource(R.color.warning_orange),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Sign Out",
                                            color = colorResource(R.color.dark_on_surface),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showProfileMenu = false
                                    coroutineScope.launch {
                                        try {
                                            RetrofitInstance.dementiaAPI.signOutUser()
                                            onShowCloseAppDialog()
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error signing out user", e)
                                            onShowCloseAppDialog() // Show dialog anyway
                                        }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBox,
                                            contentDescription = "Login with Another Account",
                                            tint = colorResource(R.color.dark_primary),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Login with Another Account",
                                            color = colorResource(R.color.dark_on_surface),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showProfileMenu = false
                                    onLoginWithAnotherAccount()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(R.color.dark_surface),
                    titleContentColor = colorResource(R.color.dark_on_surface)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToChat, // onClick is a required parameter
                modifier = Modifier.padding(16.dp),
                containerColor = colorResource(R.color.card_patient),
                contentColor = colorResource(R.color.white),
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 16.dp
                )
            ) { // This is the content lambda
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = colorResource(R.color.white).copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null, // Text of FAB acts as description
                            tint = colorResource(R.color.white),
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Get Help",
                        // color = colorResource(R.color.white), // contentColor handles this
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.gradient_patient_start),
                            colorResource(R.color.gradient_patient_end),
                            colorResource(R.color.dark_background)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(color = colorResource(R.color.dark_primary))
                    Spacer(modifier = Modifier.weight(1f))
                } else if (userInfo != null) {
                    PatientInfoCard(
                        name = userInfo!!.name,
                        email = userInfo!!.email,
                        gender = userInfo!!.gender,
                        dob = userInfo!!.dob
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                } else if (errorMsg.isNullOrBlank()) { // Only if no specific error message is passed
                    Spacer(modifier = Modifier.weight(1f)) // Try to center this text
                    Text(
                        "Could not load patient information.",
                        color = colorResource(id = R.color.dark_on_surface)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (!errorMsg.isNullOrBlank()) {
                    // Adjust spacing if user info is also missing to prevent content overlap
                    if (userInfo == null && !isLoading) {
                        Spacer(modifier = Modifier.weight(0.5f)) // Pushes error down a bit
                    }
                    ErrorDisplay(errorMsg)
                    if (userInfo == null && !isLoading) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PatientInfoCard(name: String, email: String, gender: String, dob: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.dark_surface_variant)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Patient Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.dark_primary),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            InfoRowContent(label = "Name", value = name, icon = Icons.Filled.AccountCircle)
            InfoRowContent(label = "Email", value = email, icon = Icons.Filled.Email)
            InfoRowContent(label = "Date of Birth", value = dob, icon = Icons.Filled.CalendarToday)
            InfoRowContent(
                label = "Gender",
                value = when (gender.uppercase()) {
                    "M" -> "Male"
                    "F" -> "Female"
                    "O", "OTHER" -> "Other"
                    else -> gender.ifBlank { "Not specified" }
                },
                icon = Icons.Filled.Person
            )
        }
    }
}

@Composable
private fun InfoRowContent(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label Icon",
            tint = colorResource(R.color.dark_primary),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.dark_on_surface)
            )
        }
    }
}

@Composable
fun ErrorDisplay(errorMsg: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.error_red).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // Usually errors are aligned to the start
        ) {
            Icon(
                Icons.Filled.ReportProblem,
                contentDescription = "Error",
                tint = colorResource(R.color.error_red)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = errorMsg,
                color = colorResource(R.color.error_red),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}