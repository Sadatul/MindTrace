package com.example.frontend.screens

import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
    var isLoading by remember { mutableStateOf(false) } // General loading state for the screen
    var isFetchingOtp by remember { mutableStateOf(false) } // Specific loading state for OTP generation
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var newPatientOtp by remember { mutableStateOf<String?>(null) }
    var showNewPatientOtpDialog by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
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

    fun getNewPatientRegistrationCode() {
        Log.d(TAG, "getNewPatientRegistrationCode called.")
        coroutineScope.launch {
            isFetchingOtp = true
            errorMsg = null
            newPatientOtp = null
            Log.d(TAG, "Coroutine launched for API call. isFetchingOtp=true, newPatientOtp=null")
            try {
                val caregiverAuthToken = RetrofitInstance.dementiaAPI.getIdToken()
                Log.d(TAG, "Attempting API call to RetrofitInstance.dementiaAPI.getOtp")
                val response = RetrofitInstance.dementiaAPI.getOtp("Bearer $caregiverAuthToken")
                Log.d(
                    TAG,
                    "API call finished. Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}"
                )

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    Log.d(TAG, "API Response Successful. Body: $responseBody")
                    newPatientOtp = responseBody.otp
                    Log.d(TAG, "Extracted OTP from response: $newPatientOtp")
                    if (newPatientOtp != null) {
                        Log.d(TAG, "OTP is not null. Setting showNewPatientOtpDialog = true")
                        showNewPatientOtpDialog = true
                    } else {
                        val specificError = "Received empty registration code from server."
                        errorMsg = specificError
                        Log.w(
                            TAG,
                            "Registration OTP is null in response body. Error: $specificError"
                        )
                        snackbarHostState.showSnackbar(specificError)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(
                        TAG,
                        "API call failed. Code: ${response.code()}, Message: ${response.message()}, Error body: $errorBody"
                    )
                    val specificError =
                        "Failed to get code: ${response.message()} (Code: ${response.code()})"
                    errorMsg = specificError
                    snackbarHostState.showSnackbar("Failed to generate code: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call getOtp", e)
                val specificError = "An error occurred: ${e.message}"
                errorMsg = specificError
                snackbarHostState.showSnackbar("An error occurred while fetching the code.")
            } finally {
                isFetchingOtp = false
                Log.d(TAG, "API call coroutine finished. isFetchingOtp=false")
            }
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
                }, actions = {
                    Box {
                        IconButton(onClick = { showProfileMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Account Menu",
                                tint = colorResource(R.color.dark_primary),
                                modifier = Modifier.size(36.dp)
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
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(R.color.gradient_caregiver_start),
                shadowElevation = 20.dp,
                modifier = Modifier
                    .padding(24.dp, bottom = 8.dp)
                    .size(width = 80.dp, height = 80.dp) // Smaller size
                    .clickable(onClick = onNavigateToChat)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp), // Slightly less padding
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = "ASK AI",
                        modifier = Modifier.size(30.dp), // Smaller icon
                        tint = colorResource(R.color.white)
                    )
                    Text(
                        text = "ASK AI",
                        color = colorResource(R.color.white),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
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
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
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


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onNavigateToPatients,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp),
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

                    Button(
                        onClick = {
                            Log.d(TAG, "'Get Code for New Patient' button clicked.")
                            getNewPatientRegistrationCode()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp),
                        enabled = !isFetchingOtp,                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.gradient_caregiver_start),
                            contentColor = colorResource(R.color.white),
                            disabledContainerColor = colorResource(R.color.gradient_caregiver_start).copy(
                                alpha = 0.5f
                            ),
                            disabledContentColor = colorResource(R.color.white).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 12.dp,
                            pressedElevation = 16.dp,
                            disabledElevation = 4.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp), // Consistent icon surface size
                                shape = CircleShape,
                                color = colorResource(R.color.white).copy(alpha = 0.25f)
                            ) {
                                Icon(
                                    Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp) // Consistent icon size with padding
                                        .padding(5.dp),
                                    tint = colorResource(R.color.white)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (isFetchingOtp) "Generating Code..." else "Get Code for New Patient",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (isFetchingOtp) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = colorResource(R.color.white)
                                )
                            }
                        }
                    }
                }

                // Display error messages specifically from OTP generation
                if (!isFetchingOtp && errorMsg != null && !(isLoading && userInfo == null) && showNewPatientOtpDialog == false) {
                    // This condition ensures the error is from OTP and dialog isn't already up
                    Log.d(TAG, "Displaying non-initial load error message: $errorMsg")
                    Spacer(modifier = Modifier.height(16.dp)) // Add some space before this error
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
                                text = errorMsg!!, // errorMsg is confirmed not null here
                                color = colorResource(R.color.error_red),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }

        Log.d(
            TAG,
            "ScreenCareGiver recomposing. showNewPatientOtpDialog: $showNewPatientOtpDialog, newPatientOtp: $newPatientOtp"
        )

        if (showNewPatientOtpDialog && newPatientOtp != null) { // Ensure OTP is not null before showing dialog
            Log.d(TAG, "Rendering ShowOTP Dialog. OTP: $newPatientOtp")
            ShowOTP(
                otp = newPatientOtp!!, // OTP is confirmed not null here
                onCopy = {
                    Log.d(TAG, "Copy clicked in ShowOTP Dialog.")
                    clipboardManager.setText(AnnotatedString(newPatientOtp!!))
                    coroutineScope.launch { snackbarHostState.showSnackbar("Registration code copied!") }
                    showNewPatientOtpDialog = false
                    // errorMsg = null // Clear error message when OTP dialog is shown successfully
                },
                onCancel = {
                    Log.d(TAG, "Cancel/Close clicked in ShowOTP Dialog.")
                    showNewPatientOtpDialog = false
                    // errorMsg = null // Clear error message on cancel as well, if appropriate
                }
            )
        }
    }
}

@Composable
fun CaregiverInfoCard(
    name: String,
    email: String,
    dob: String,
    gender: String,
    profilePicture: String?,
    userId: String? // Add userId for QR code
) {
    var showQrDialog by remember { mutableStateOf(false) }
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
                    color = colorResource(R.color.info_blue), // Brighter blue for visibility
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
                colorResource(R.color.info_blue), // Brighter blue for visibility
                colorResource(R.color.dark_on_surface)
            )
            InfoRow(
                "Email",
                Icons.Filled.Email,
                email,
                colorResource(R.color.info_blue), // Brighter blue for visibility
                colorResource(R.color.dark_on_surface)
            )
            InfoRow(
                "Date of Birth",
                Icons.Filled.CalendarToday,
                dob,
                colorResource(R.color.info_blue), // Brighter blue for visibility
                colorResource(R.color.dark_on_surface)
            )
            InfoRow(
                "Gender",
                Icons.Filled.Person,
                formatGender(gender),
                colorResource(R.color.info_blue), // Brighter blue for visibility
                colorResource(R.color.dark_on_surface)
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
fun InfoRow(label: String, icon: ImageVector, value: String, iconColor: Color, textColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.18f) // Lighter, more visible blue background
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$label Icon",
                tint = iconColor, // Brighter blue
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
fun ShowOTP(otp: String, onCopy: () -> Unit, onCancel: () -> Unit) {
    Log.d(TAG, "ShowOTP Dialog recomposing. OTP: $otp")
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                "New Patient Registration Code",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface // Use theme colors
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Share this One-Time Code with the new patient for their registration process.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme colors
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Use theme colors
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            otp,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary // Use theme colors
                        )
                        IconButton(onClick = onCopy, modifier = Modifier.size(48.dp)) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                "Copy Code",
                                tint = MaterialTheme.colorScheme.primary // Use theme colors
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("CLOSE", color = MaterialTheme.colorScheme.primary) // Use theme colors
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun getCaregiverProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    return when {
        screenWidth >= 400.dp -> 72.dp
        screenWidth >= 360.dp -> 64.dp
        else -> 56.dp
    }
}