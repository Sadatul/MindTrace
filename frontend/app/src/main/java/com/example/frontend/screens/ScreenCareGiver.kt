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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
    onSignOut: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val mainButtonHeight = when {
        screenHeight < 600.dp -> 60.dp
        screenHeight < 800.dp -> 66.dp
        else -> 72.dp
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
                                            imageVector = Icons.Default.People,
                                            contentDescription = "Sign Out",
                                            tint = colorResource(R.color.dark_primary),
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
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.dark_surface))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    colorResource(id = R.color.gradient_caregiver_start),
                                    colorResource(id = R.color.gradient_caregiver_end)
                                )
                            )
                        )
                        .padding(horizontal = 24.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // My Patients Button (icon on top, text below)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(mainButtonHeight)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        colorResource(R.color.gradient_caregiver_start),
                                        colorResource(R.color.gradient_caregiver_end)
                                    )
                                )
                            )
                            .border(2.dp, colorResource(R.color.info_blue), RoundedCornerShape(20.dp))
                            .clickable(onClick = onNavigateToPatients),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "My Patients",
                                modifier = Modifier.size(28.dp),
                                tint = colorResource(R.color.white)
                            )
                            Text(
                                text = "My Patients",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorResource(R.color.white),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    val showQrDialogBottomBar = remember { mutableStateOf(false) }
                    val qrCodeDataBottomBar = remember { mutableStateOf<String?>(null) }
                    val isLoadingQrDataBottomBar = remember { mutableStateOf(false) }
                    val qrErrorBottomBar = remember { mutableStateOf<String?>(null) }
                    val coroutineScope = rememberCoroutineScope()

                    fun handleQrDialogCloseBottomBar() {
                        showQrDialogBottomBar.value = false
                        qrCodeDataBottomBar.value = null
                        qrErrorBottomBar.value = null
                    }

                    fun generateQrCodeDataBottomBar(userId: String) {
                        coroutineScope.launch {
                            isLoadingQrDataBottomBar.value = true
                            qrErrorBottomBar.value = null
                            try {
                                val caregiverAuthToken = RetrofitInstance.dementiaAPI.getIdToken()
                                val response = RetrofitInstance.dementiaAPI.getOtp("Bearer $caregiverAuthToken")

                                if (response.isSuccessful && response.body() != null) {
                                    val otp = response.body()!!.otp
                                    if (otp != null) {
                                        qrCodeDataBottomBar.value = "$userId|$otp"
                                        showQrDialogBottomBar.value = true
                                    } else {
                                        qrErrorBottomBar.value = "Failed to generate registration code"
                                    }
                                } else {
                                    qrErrorBottomBar.value = "Failed to fetch registration code: ${response.message()}"
                                }
                            } catch (e: Exception) {
                                qrErrorBottomBar.value = "Error generating QR code: ${e.message}"
                                Log.e(TAG, "Error generating QR code data", e)
                            } finally {
                                isLoadingQrDataBottomBar.value = false
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-24).dp)
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(colorResource(R.color.white))
                                .border(
                                    width = 3.dp,
                                    color = colorResource(R.color.info_blue),
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (!isLoadingQrDataBottomBar.value && userInfo?.id != null) {
                                        generateQrCodeDataBottomBar(userInfo!!.id)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingQrDataBottomBar.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = colorResource(R.color.info_blue),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "My QR",
                                    modifier = Modifier.size(44.dp),
                                    tint = colorResource(R.color.info_blue)
                                )
                            }
                        }
                        Text(
                            text = "My QR",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .offset(y = (-20).dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        if (qrErrorBottomBar.value != null) {
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
                                        text = qrErrorBottomBar.value ?: "",
                                        color = colorResource(R.color.error_red),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                        if (showQrDialogBottomBar.value && qrCodeDataBottomBar.value != null) {
                            AlertDialog(
                                onDismissRequest = { handleQrDialogCloseBottomBar() },
                                title = {
                                    Text("Registration QR Code", fontWeight = FontWeight.Bold)
                                },
                                text = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        com.example.frontend.screens.components.QRCode(
                                            data = qrCodeDataBottomBar.value!!,
                                            modifier = Modifier.size(240.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("QR Code for New Patient Register", style = MaterialTheme.typography.bodyMedium)

                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { handleQrDialogCloseBottomBar() }) {
                                        Text("CLOSE")
                                    }
                                }
                            )
                        }
                    }
                    // ASK AI Button (icon on top, text below)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(mainButtonHeight)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        colorResource(R.color.gradient_caregiver_start),
                                        colorResource(R.color.gradient_caregiver_end)
                                    )
                                )
                            )
                            .border(2.dp, colorResource(R.color.info_blue), RoundedCornerShape(20.dp))
                            .clickable(onClick = onNavigateToChat),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = "Ask AI",
                                modifier = Modifier.size(28.dp),
                                tint = colorResource(R.color.white)
                            )
                            Text(
                                text = "Ask AI",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorResource(R.color.white),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(id = R.color.gradient_caregiver_start),
                            colorResource(id = R.color.gradient_caregiver_end)
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
                        profilePicture = userInfo!!.profilePicture
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
    profilePicture: String?
) {
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