package com.example.frontend.screens

import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.SelfUserInfoCache
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
import com.example.frontend.api.getSelfUserInfo
import kotlinx.coroutines.launch

private const val TAG = "ScreenCareGiver"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCareGiver(
    onNavigateToChat: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onSignOut: () -> Unit,
    onLoginWithAnotherAccount: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Comprehensive responsive sizing (consistent with patient screen)
    val fabWidth = (screenWidth * 0.28f).coerceAtMost(140.dp).coerceAtLeast(100.dp)
    val fabHeight = (screenHeight * 0.11f).coerceAtMost(100.dp).coerceAtLeast(80.dp)
    val iconSize = (fabWidth * 0.35f).coerceAtMost(48.dp).coerceAtLeast(32.dp)

    // Spacing between floating action buttons
    val spacerWidth = when {
        screenWidth >= 400.dp -> 24.dp
        screenWidth >= 360.dp -> 18.dp
        else -> 12.dp
    }

    // Main button sizing
    val buttonWidthFraction = when {
        screenWidth >= 400.dp -> 0.9f
        screenWidth >= 360.dp -> 0.85f
        else -> 0.95f
    }

    val mainButtonHeight = when {
        screenHeight < 600.dp -> 48.dp
        screenHeight < 800.dp -> 56.dp
        else -> 64.dp
    }

    // Spacing and padding
    val verticalSpacing = when {
        screenHeight < 600.dp -> 16.dp
        screenHeight < 800.dp -> 20.dp
        else -> 24.dp
    }

    val horizontalPadding = when {
        screenWidth < 360.dp -> 12.dp
        screenWidth < 400.dp -> 16.dp
        else -> 20.dp
    }

    // Responsive bottom padding for dual floating action buttons (consistent with patient screen)
    val bottomPadding = when {
        screenHeight < 600.dp -> 160.dp
        screenHeight < 800.dp -> 180.dp
        else -> 200.dp
    }

    var isLoading by remember { mutableStateOf(false) } // General loading state for the screen
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showProfileMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var userInfo: UserInfo? by remember { mutableStateOf(null) }


    LaunchedEffect(Unit) {
        val cachedUserInfo = SelfUserInfoCache.getUserInfo()
        if (cachedUserInfo != null) {
            userInfo = cachedUserInfo
        } else {
            isLoading = true // Show loading for initial user info fetch
        }

        try {
            val freshUserInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
            if (freshUserInfo != null) {
                userInfo = freshUserInfo
                SelfUserInfoCache.setUserInfo(freshUserInfo) // Update cache
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info", e)
            errorMsg = "Could not load user details."
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
                        "Caregiver Dashboard",
                        color = colorResource(R.color.dark_on_surface),
                        fontWeight = FontWeight.Bold
                    )
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
                                            imageVector = Icons.Default.People,
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // My Patients Button
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colorResource(R.color.gradient_caregiver_start),
                    shadowElevation = 20.dp,
                    modifier = Modifier
                        .size(width = fabWidth, height = fabHeight)
                        .clickable(onClick = onNavigateToPatients)
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
                            contentDescription = "My Patients",
                            modifier = Modifier.size(iconSize),
                            tint = colorResource(R.color.white)
                        )
                        Text(
                            text = "My Patients",
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
                    color = colorResource(R.color.gradient_caregiver_start),
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.gradient_caregiver_start),
                            colorResource(R.color.gradient_caregiver_end),
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
                    .padding(bottom = bottomPadding), // Responsive padding for floating action button
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                if (isLoading && userInfo == null) {
                    CircularProgressIndicator(color = colorResource(id = R.color.dark_primary))
                } else if (userInfo != null) {
                    CaregiverInfoCard(
                        name = userInfo!!.name,
                        email = userInfo!!.email,
                        gender = userInfo!!.gender,
                        dob = userInfo!!.dob,
                        profilePicture = userInfo!!.profilePicture,
                        userId = userInfo!!.id // Pass id for QR code
                    )
                } else if (errorMsg != null) { // Show initial load error only if not loading user info
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                contentDescription = "Error",
                                tint = colorResource(id = R.color.error_red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMsg ?: "Could not load user details.",
                                color = colorResource(R.color.error_red),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }


                Button(
                    onClick = onNavigateToPatients,
                    modifier = Modifier
                        .fillMaxWidth(buttonWidthFraction)
                        .height(mainButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.gradient_caregiver_start),
                        contentColor = colorResource(R.color.white)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 12.dp,
                        pressedElevation = 16.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = colorResource(R.color.white).copy(alpha = 0.25f)
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(5.dp),
                                tint = colorResource(R.color.white)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "My Patients",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaregiverInfoCard(
    name: String,
    email: String,
    gender: String,
    dob: String,
    profilePicture: String?,
    userId: String
) {
    var showQrDialog by remember { mutableStateOf(false) }
    var qrCodeData by remember { mutableStateOf<String?>(null) }
    var isLoadingQrData by remember { mutableStateOf(false) }
    var qrError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun handleQrDialogClose() {
        showQrDialog = false
        qrCodeData = null
        qrError = null
    }

    fun generateQrCodeData() {
        coroutineScope.launch {
            isLoadingQrData = true
            qrError = null
            try {
                val caregiverAuthToken = RetrofitInstance.dementiaAPI.getIdToken()
                val response = RetrofitInstance.dementiaAPI.getOtp("Bearer $caregiverAuthToken")

                if (response.isSuccessful && response.body() != null) {
                    val otp = response.body()!!.otp
                    if (otp != null) {
                        qrCodeData = "$userId|$otp"
                        showQrDialog = true
                    } else {
                        qrError = "Failed to generate registration code"
                    }
                } else {
                    qrError = "Failed to fetch registration code: ${response.message()}"
                }
            } catch (e: Exception) {
                qrError = "Error generating QR code: ${e.message}"
                Log.e(TAG, "Error generating QR code data", e)
            } finally {
                isLoadingQrData = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.dark_surface_variant).copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "My Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.info_blue),
                    modifier = Modifier.weight(1f)
                )
                if (profilePicture != null) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(getCaregiverProfilePictureSize())
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        error = painterResource(R.drawable.ic_launcher_foreground)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Profile",
                        tint = colorResource(R.color.gradient_caregiver_start).copy(alpha = 0.8f),
                        modifier = Modifier.size(getCaregiverProfilePictureSize())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(
                "Name",
                Icons.Filled.AccountCircle,
                name,
                colorResource(R.color.info_blue),
                colorResource(R.color.dark_on_surface)
            )
            InfoRow(
                "Email",
                Icons.Filled.Email,
                email,
                colorResource(R.color.info_blue),
                colorResource(R.color.dark_on_surface)
            )
            InfoRow(
                "Date of Birth",
                Icons.Filled.CalendarToday,
                dob,
                colorResource(R.color.info_blue),
                colorResource(R.color.dark_on_surface)
            )
            InfoRow(
                "Gender",
                Icons.Filled.Person,
                formatGender(gender),
                colorResource(R.color.info_blue),
                colorResource(R.color.dark_on_surface)
            )
            if (userId.isNotBlank()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 8.dp)
                ) {
                    if (isLoadingQrData) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(38.dp),
                            color = colorResource(R.color.info_blue),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(68.dp)
                                .shadow(10.dp, CircleShape, clip = false)
                                .background(Color.White, shape = CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = colorResource(R.color.info_blue).copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                                .clickable(enabled = !isLoadingQrData) {
                                    generateQrCodeData()
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Show QR Code",
                                modifier = Modifier.size(40.dp),
                                tint = Color.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (isLoadingQrData) "Generating QR Code..." else "My QR Code",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(R.color.info_blue)
                    )
                }
                qrError?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.error_red).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                contentDescription = "Error",
                                tint = colorResource(R.color.error_red),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = colorResource(R.color.error_red),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
        if (showQrDialog && qrCodeData != null) {
            AlertDialog(
                onDismissRequest = { handleQrDialogClose() },
                title = {
                    Text("Registration QR Code", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        com.example.frontend.screens.components.QRCode(
                            data = qrCodeData!!,
                            modifier = Modifier.size(240.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Scan this code to register as a patient",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Contains: Caregiver ID + Registration Code",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { handleQrDialogClose() }) {
                        Text("CLOSE")
                    }
                }
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    icon: ImageVector,
    value: String,
    iconColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.18f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$label Icon",
                tint = iconColor,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

fun formatGender(genderCode: String): String {
    return when (genderCode.uppercase()) {
        "M" -> "Male"
        "F" -> "Female"
        "O", "OTHER" -> "Other"
        else -> genderCode.ifBlank { "Not specified" }
    }
}

@Composable
fun getCaregiverProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    return when {
        screenWidth >= 400.dp -> 72.dp
        screenWidth >= 360.dp -> 64.dp
        else -> 56.dp
    }
}