package com.example.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // Added for explicit spacing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator // Added for loading state
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.signOutUser


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPatient(
    errorMsg: String? = null,
    onNavigateToChat: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onLoginWithAnotherAccount: () -> Unit = {},
    onShowCloseAppDialog: () -> Unit = {}
) {

    var userInfo: UserInfo? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) } // Added loading state
    
    // Profile menu state
    var showProfileMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { // Changed key to Unit to run once
        isLoading = true
        try {
            userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
        } catch (e: Exception) {
            // Handle error, maybe update errorMsg or show a snackbar
            println("Error fetching user info: ${e.localizedMessage}")
        } finally {
            isLoading = false
        }
    }
    Scaffold(
        containerColor = colorResource(R.color.dark_background),        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Patient Dashboard",
                        color = colorResource(R.color.dark_on_surface),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showProfileMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile Menu",
                                tint = colorResource(R.color.dark_on_surface),
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
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
                                    onShowCloseAppDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
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
                text = {
                    Text(
                        "Get Help",
                        color = colorResource(R.color.dark_on_primary),
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        Icons.Filled.HelpOutline,
                        contentDescription = "Help",
                        tint = colorResource(R.color.dark_on_primary)
                    )
                },
                onClick = onNavigateToChat,
                modifier = Modifier.padding(16.dp), // Consider FAB guidelines for padding
                containerColor = colorResource(R.color.card_patient),
                contentColor = colorResource(R.color.dark_on_primary),
                shape = RoundedCornerShape(16.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box( // This Box is primarily for the background gradient
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
            Column( // This Column now correctly scopes the .weight modifier
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Apply padding from Scaffold
                    .padding(16.dp),       // Apply overall screen padding
                horizontalAlignment = Alignment.CenterHorizontally
                // Removed verticalArrangement = Arrangement.spacedBy(20.dp) to let weight work
            ) {
                if (isLoading) {
                    // Show a loading indicator in the center
                    Spacer(modifier = Modifier.weight(1f)) // Push loading to center
                    CircularProgressIndicator(
                        color = colorResource(R.color.dark_primary)
                    )
                    Spacer(modifier = Modifier.weight(1f)) // Push loading to center
                } else if (userInfo != null) {
                    PatientInfoCard(
                        name = userInfo!!.name,
                        email = userInfo!!.email,
                        gender = userInfo!!.gender,
                        dob = userInfo!!.dob
                    )
                    Spacer(modifier = Modifier.height(20.dp)) // Space after info card
                } else if (errorMsg.isNullOrBlank()) {
                    // Case where not loading, userInfo is null, but no external errorMsg
                    // You might want to show a generic error message here
                    Text(
                        "Could not load patient information.",
                        color = colorResource(id = R.color.dark_on_surface)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }


                if (!errorMsg.isNullOrBlank()) {
                    ErrorDisplay(errorMsg)
                    Spacer(modifier = Modifier.height(20.dp)) // Space after error display
                }

                // This Spacer will push the Button to the bottom
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        // Consider handling the sign-out process more robustly,
                        // e.g., showing a loading state, navigating away on success/failure.
                        RetrofitInstance.dementiaAPI.signOutUser()
                        // Example: onNavigateToLoginScreen() // Or similar action
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.warning_orange),
                        contentColor = colorResource(R.color.dark_on_primary)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth() // Make button full width for better UX
                ) {
                    Icon(
                        Icons.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = colorResource(R.color.dark_on_primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sign Out",
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.dark_on_primary)
                    )
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

            // Row for Name
            InfoRowContent(
                label = "Name",
                value = name,
                icon = Icons.Filled.AccountCircle
            )

            // Row for Email
            InfoRowContent(
                label = "Email",
                value = email,
                icon = Icons.Filled.Email
            )

            // Row for Date of Birth
            InfoRowContent(
                label = "Date of Birth",
                value = dob, // Consider formatting this date for display
                icon = Icons.Filled.CalendarToday
            )

            // Row for Gender
            InfoRowContent(
                label = "Gender",
                value = when (gender.uppercase()) { // Inlined formatting
                    "M" -> "Male"
                    "F" -> "Female"
                    "O", "OTHER" -> "Other"
                    else -> gender // Fallback, or "Not specified"
                },
                icon = Icons.Filled.Person
            )
        }
    }
}

// Extracted the content of each info row into a reusable composable
// for better readability and maintainability within PatientInfoCard
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
            containerColor = colorResource(R.color.error_red).copy(alpha = 0.1f) // Use your defined error color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ReportProblem,
                contentDescription = "Error",
                tint = colorResource(R.color.error_red) // Use your defined error color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = errorMsg,
                color = colorResource(R.color.error_red), // Use your defined error color
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}