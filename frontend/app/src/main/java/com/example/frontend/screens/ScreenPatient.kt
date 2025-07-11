package com.example.frontend.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.PrimaryContact
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getFCMToken
import com.example.frontend.api.getSelfUserInfo

private const val TAG = "ScreenPatient"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPatient(
    errorMsg: String? = null,
    onNavigateToChat: () -> Unit = {},
    onNavigateToCaregivers: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onLoginWithAnotherAccount: () -> Unit = {},
    onBack: () -> Boolean
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    // Comprehensive responsive sizing
    val fabWidth = (screenWidth * 0.28f).coerceAtMost(140.dp).coerceAtLeast(100.dp)
    val fabHeight = (screenHeight * 0.11f).coerceAtMost(100.dp).coerceAtLeast(80.dp)
    val iconSize = (fabWidth * 0.35f).coerceAtMost(48.dp).coerceAtLeast(32.dp)
    
    // Spacing between floating action buttons
    val spacerWidth = when {
        screenWidth >= 400.dp -> 24.dp
        screenWidth >= 360.dp -> 18.dp
        else -> 12.dp
    }
    
    // Content padding and spacing
    val horizontalPadding = when {
        screenWidth < 360.dp -> 12.dp
        screenWidth < 400.dp -> 16.dp
        else -> 20.dp
    }
    
    // Responsive bottom padding for dual floating action buttons
    val bottomPadding = when {
        screenHeight < 600.dp -> 160.dp
        screenHeight < 800.dp -> 180.dp  
        else -> 200.dp
    }
    
    var userInfo: UserInfo? by remember { mutableStateOf(null) }
    var fcmToken: String? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showProfileMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
            fcmToken = RetrofitInstance.dementiaAPI.getFCMToken()
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
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(R.color.dark_primary),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // My Caregivers Button
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colorResource(R.color.gradient_patient_start),
                    shadowElevation = 20.dp,
                    modifier = Modifier
                        .size(width = fabWidth, height = fabHeight)
                        .clickable(onClick = onNavigateToCaregivers)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "My Caregivers",
                            modifier = Modifier.size(iconSize),
                            tint = colorResource(R.color.white)
                        )
                        Text(
                            text = "My Caregivers",
                            color = colorResource(R.color.white),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(spacerWidth))
                // ASK AI Button
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colorResource(R.color.gradient_patient_start),
                    shadowElevation = 20.dp,
                    modifier = Modifier
                        .size(width = fabWidth, height = fabHeight)
                        .clickable(onClick = onNavigateToChat)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "ASK AI",
                            modifier = Modifier.size(iconSize),
                            tint = colorResource(R.color.white)
                        )
                        Text(
                            text = "ASK AI",
                            color = colorResource(R.color.white),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }, floatingActionButtonPosition = FabPosition.Center
    ) {
        innerPadding ->
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
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
                    .padding(bottom = bottomPadding), // Responsive padding for floating action buttons
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(R.color.dark_primary))
                    }
                } else if (userInfo != null) {
                    PatientInfoCard(
                        profilePicture = userInfo!!.profilePicture,
                        name = userInfo!!.name,
                        email = userInfo!!.email,
                        gender = userInfo!!.gender,
                        dob = userInfo!!.dob,
                        userId = userInfo!!.id,
                        fcmToken = fcmToken,
                        // Primary contact information (caregiver)
                        primaryContact = userInfo!!.primaryContact
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                } else if (errorMsg.isNullOrBlank()) { // Only if no specific error message is passed from NavGraph
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Could not load patient information.",
                            color = colorResource(id = R.color.dark_on_surface) // Use a less alarming color
                        )
                    }
                }

                // Display error message passed from NavGraph or other sources
                if (!errorMsg.isNullOrBlank()) {
                    if (userInfo == null && !isLoading) { // If user info also failed to load
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    ErrorDisplay(errorMsg)
                    if (userInfo != null || isLoading) {
                        Spacer(modifier = Modifier.height(20.dp)) // Add space if user info is present
                    }
                }
            }
        }
    }
}

@Composable
fun PatientInfoCard(
    profilePicture: String?,
    name: String,
    email: String,
    gender: String,
    dob: String,
    userId: String?,
    fcmToken: String?,
    // Primary contact information (caregiver)
    primaryContact: PrimaryContact?,
) {
    var showQrDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

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
            if (fcmToken != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Report,
                        contentDescription = "FCM Token",
                        tint = colorResource(R.color.dark_primary),
                        modifier = Modifier.size(24.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FCM Token",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorResource(R.color.dark_primary).copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = fcmToken.take(40) + if (fcmToken.length > 40) "..." else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.dark_primary)
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(fcmToken))
                            Toast.makeText(context, "FCM Token copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy FCM Token",
                            tint = colorResource(R.color.info_blue),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
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

            // QR Code Row below info fields (no button background, larger icon/text)
            if (userId != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Show QR Code",
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { showQrDialog = true },
                        tint = colorResource(R.color.info_blue)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        "My QR Code",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(R.color.info_blue)
                    )
                }
            }
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
        // QR Code Dialog
        if (showQrDialog && userId != null) {
            AlertDialog(
                onDismissRequest = { showQrDialog = false },
                title = {
                    Text("Your QR Code", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        com.example.frontend.screens.components.QRCode(
                            data = userId,
                            modifier = Modifier.size(240.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scan this code to share your ID", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQrDialog = false }) {
                        Text("CLOSE")
                    }
                }
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