package com.example.frontend.screens

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.PrimaryContact
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getSelfUserInfo

private const val TAG = "ScreenPatient"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPatient(
    errorMsg: String? = null,
    onNavigateToChat: () -> Unit = {},
    onNavigateToCaregivers: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onLoginWithAnotherAccount: () -> Unit = {}
) {
    var userInfo: UserInfo? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showProfileMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info", e)
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
                }, actions = {
                    Box {
                        IconButton(
                            onClick = { showProfileMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Account Menu",
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
                                    onSignOut()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.People, // Using People icon for "another account"
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
                                }, onClick = {
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), // Ensure FABS don't overflow small screens
                horizontalArrangement = Arrangement.SpaceEvenly, // Distribute space
                verticalAlignment = Alignment.CenterVertically
            ) {
                // My Caregivers Button
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCaregivers,
                    containerColor = colorResource(R.color.gradient_patient_start),
                    contentColor = colorResource(R.color.white),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f),
                    expanded = LocalConfiguration.current.screenWidthDp > 360,
                    text = {
                        Text(
                            "My Caregivers",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    icon = {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = colorResource(R.color.white).copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = colorResource(R.color.white),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Get Help Button
                ExtendedFloatingActionButton(
                    onClick = onNavigateToChat,
                    containerColor = colorResource(R.color.gradient_patient_start),
                    contentColor = colorResource(R.color.white),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f),
                    expanded = LocalConfiguration.current.screenWidthDp > 360,
                    text = {
                        Text(
                            "Get Help",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    icon = {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = colorResource(R.color.white).copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = colorResource(R.color.white),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                )
            }
        }, floatingActionButtonPosition = FabPosition.Center
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
                        dob = userInfo!!.dob,
                        profilePicture = userInfo!!.profilePicture,
                        primaryContact = userInfo!!.primaryContact
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                } else if (errorMsg.isNullOrBlank()) { // Only if no specific error message is passed from NavGraph
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Could not load patient information.",
                        color = colorResource(id = R.color.dark_on_surface) // Use a less alarming color
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Display error message passed from NavGraph or other sources
                if (!errorMsg.isNullOrBlank()) {
                    if (userInfo == null && !isLoading) { // If user info also failed to load
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                    ErrorDisplay(errorMsg)
                    if (userInfo == null && !isLoading) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    } else {
                        Spacer(modifier = Modifier.height(20.dp)) // Add space if user info is present
                    }
                }
            }
        }
    }
}

@Composable
fun PatientInfoCard(name: String, email: String, gender: String, dob: String, profilePicture: String?, primaryContact: PrimaryContact?) {
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
            // Header with profile picture
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Patient Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_primary),
                    modifier = Modifier.weight(1f) // Takes available space, pushing picture to the end
                )                // Profile picture in the card
                if (profilePicture != null) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(getProfilePictureSize())
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_launcher_foreground), // Replace with your placeholder
                        error = painterResource(R.drawable.ic_launcher_foreground)     // Replace with your error placeholder
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Profile",
                        tint = colorResource(R.color.dark_primary),
                        modifier = Modifier.size(getProfilePictureSize())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Spacing after header row

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

            // Primary Contact Information (Caregiver)
            if (primaryContact != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = colorResource(R.color.dark_primary).copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Primary Contact Header with profile picture
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Primary Contact (Caregiver)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.dark_primary),
                        modifier = Modifier.weight(1f)
                    )
                    // Caregiver profile picture
                    if (primaryContact.profilePicture != null) {
                        AsyncImage(
                            model = primaryContact.profilePicture,
                            contentDescription = "Caregiver Profile Picture",
                            modifier = Modifier
                                .size(getCaregiverProfilePictureSize())
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_launcher_foreground), // Replace
                            error = painterResource(R.drawable.ic_launcher_foreground)     // Replace
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Default Caregiver Profile",
                            tint = colorResource(R.color.dark_primary),
                            modifier = Modifier.size(getCaregiverProfilePictureSize())
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                InfoRowContent(label = "Caregiver Name", value = primaryContact.name, icon = Icons.Filled.ContactPhone)
                InfoRowContent(label = "Caregiver ID", value = primaryContact.id, icon = Icons.Filled.Badge)
                InfoRowContent(
                    label = "Gender",
                    value = when (primaryContact.gender.uppercase()) {
                        "M" -> "Male"
                        "F" -> "Female"
                        "O", "OTHER" -> "Other"
                        else -> primaryContact.gender.ifBlank { "Not specified" }
                    },
                    icon = Icons.Filled.Person
                )
            }
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
            containerColor = colorResource(R.color.error_red).copy(alpha = 0.1f) // Softer error background
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                Icons.Filled.ReportProblem,
                contentDescription = "Error",
                tint = colorResource(R.color.error_red)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = errorMsg,
                color = colorResource(R.color.error_red), // Ensure text color is clearly visible
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun getProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Only used for card display now, responsive to screen size
    return when {
        screenWidth >= 400.dp -> 72.dp
        screenWidth >= 360.dp -> 64.dp
        else -> 56.dp
    }
}

@Composable
private fun getCaregiverProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Slightly smaller than patient profile picture for secondary display
    return when {
        screenWidth >= 400.dp -> 56.dp
        screenWidth >= 360.dp -> 48.dp
        else -> 40.dp
    }
}