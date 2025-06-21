package com.example.frontend.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp // Corrected import
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
    onSignOut: () -> Unit,
    onLoginWithAnotherAccount: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var newPatientOtp by remember { mutableStateOf<String?>(null) }
    var showNewPatientOtpDialog by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var userInfo: UserInfo? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) { // Changed key to Unit to run once if data doesn't change
        // First, try to get user info from cache for immediate display
        val cachedUserInfo = SelfUserInfoCache.getUserInfo()
        if (cachedUserInfo != null) {
            userInfo = cachedUserInfo
            isLoading = false
        } else {
            isLoading = true // Show loading only if no cached data
        }
        
        // Always fetch fresh data to ensure cache is up to date
        try {
            val freshUserInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
            if (freshUserInfo != null) {
                userInfo = freshUserInfo // This will be the cached version with Google profile picture if needed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info", e)
            errorMsg = "Could not load user details."
        } finally {
            isLoading = false // Hide loading once done
        }
    }

    fun getNewPatientRegistrationCode() {
        Log.d(TAG, "getNewPatientRegistrationCode called.")
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            newPatientOtp = null
            Log.d(TAG, "Coroutine launched for API call. isLoading=true, newPatientOtp=null")
            try {
                val caregiverAuthToken = RetrofitInstance.dementiaAPI.getIdToken()
                Log.d(TAG, "Attempting API call to RetrofitInstance.dementiaAPI.getOtp")
                val response = RetrofitInstance.dementiaAPI.getOtp("Bearer $caregiverAuthToken")
                Log.d(TAG, "API call finished. Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    Log.d(TAG, "API Response Successful. Body: $responseBody")
                    newPatientOtp = responseBody.otp
                    Log.d(TAG, "Extracted OTP from response: $newPatientOtp")
                    if (newPatientOtp != null) {
                        Log.d(TAG, "OTP is not null. Setting showNewPatientOtpDialog = true")
                        showNewPatientOtpDialog = true
                    } else {
                        errorMsg = "Received empty registration code from server."
                        Log.w(TAG, "Registration OTP is null in response body. Error: $errorMsg")
                        snackbarHostState.showSnackbar("Received empty registration code.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "API call failed. Code: ${response.code()}, Message: ${response.message()}, Error body: $errorBody")
                    errorMsg = "Failed to get code: ${response.message()} (Code: ${response.code()})"
                    snackbarHostState.showSnackbar("Failed to generate code: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call getOtp", e)
                errorMsg = "An error occurred: ${e.message}"
                snackbarHostState.showSnackbar("An error occurred while fetching the code.")
            } finally {
                isLoading = false
                Log.d(TAG, "API call coroutine finished. isLoading=false")
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
                },                actions = {
                    Box {                        IconButton(onClick = { showProfileMenu = true }) {
                            if (userInfo?.profilePicture != null) {
                                AsyncImage(
                                    model = userInfo!!.profilePicture,
                                    contentDescription = "User Profile Picture",
                                    modifier = Modifier
                                        .size(getProfilePictureSize(isInCard = false))
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                    error = painterResource(R.drawable.ic_launcher_foreground)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile Menu",
                                    tint = colorResource(R.color.dark_primary),
                                    modifier = Modifier.size(getProfilePictureSize(isInCard = false))
                                )
                            }
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
                                            // Make sure this icon is imported correctly:
                                            // import androidx.compose.material.icons.auto mirrored.filled.ExitToApp
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                verticalArrangement = Arrangement.spacedBy(20.dp) // Maintain overall spacing
            ) {
                // Show loading indicator for user info at the top if it's the initial load
                // Or let CaregiverInfoCard handle its own shimmer/placeholder if preferred
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
                }

                Column(
                    modifier = Modifier.fillMaxWidth(), // This column groups the buttons
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onNavigateToChat,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.card_info),
                            contentColor = colorResource(R.color.white)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center, // Center content
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = colorResource(R.color.white).copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    Icons.Filled.Chat, // Using Filled.Chat
                                    contentDescription = null, // Text describes button
                                    modifier = Modifier
                                        .size(28.dp) // Ensure icon is centered
                                        .padding(5.dp),
                                    tint = colorResource(R.color.white)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Help & Support Chat",
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
                        enabled = !isLoading, // Use the general isLoading state
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.card_caregiver),
                            contentColor = colorResource(R.color.white),
                            disabledContainerColor = colorResource(R.color.dark_surface_variant).copy(alpha = 0.5f), // More distinct disabled
                            disabledContentColor = colorResource(R.color.dark_on_surface).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 12.dp, disabledElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center, // Center content
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = colorResource(R.color.white).copy(alpha = 0.2f)
                            ) {
                                Row( // For multiple icons if needed, or just one
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.PersonAdd,
                                        contentDescription = null, // Button text is primary
                                        modifier = Modifier.size(16.dp), // Adjusted size
                                        tint = colorResource(R.color.white)
                                    )
                                    // Removed second icon for simplicity unless specifically needed
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (isLoading && newPatientOtp == null) "Generating Code..." else "Get Code for New Patient", // More specific loading text
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (isLoading && newPatientOtp == null) { // Show progress only for this action
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp), // Slightly larger
                                    strokeWidth = 2.dp,
                                    color = colorResource(R.color.white)
                                )
                            }
                        }
                    }
                }

                // Display error messages from API calls (like OTP generation failure)
                errorMsg?.let { currentError ->
                    Log.d(TAG, "Displaying error message: $currentError")
                    Spacer(modifier = Modifier.height(16.dp)) // Add space before error
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.error_red).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = colorResource(id = R.color.error_red))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentError,
                                color = colorResource(R.color.error_red),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            } // End of main content Column
        } // End of Box with background

        Log.d(TAG, "ScreenCareGiver recomposing. showNewPatientOtpDialog: $showNewPatientOtpDialog, newPatientOtp: $newPatientOtp")

        if (showNewPatientOtpDialog) {
            Log.d(TAG, "Rendering ShowOTP Dialog. OTP: $newPatientOtp")
            ShowOTP(
                otp = newPatientOtp ?: "N/A", // Provide a fallback for null OTP
                onCopy = {
                    Log.d(TAG, "Copy clicked in ShowOTP Dialog.")
                    newPatientOtp?.let {
                        clipboardManager.setText(AnnotatedString(it))
                        coroutineScope.launch { snackbarHostState.showSnackbar("Registration code copied!") }
                    }
                    showNewPatientOtpDialog = false // Dismiss dialog after copy
                },
                onCancel = {
                    Log.d(TAG, "Cancel/Close clicked in ShowOTP Dialog.")
                    showNewPatientOtpDialog = false
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
    profilePicture: String? // Nullable for when it's not available
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.dark_surface_variant)),
        shape = RoundedCornerShape(16.dp)
    ) {        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with profile picture
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "My Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_primary),
                    modifier = Modifier.weight(1f)
                )                // Profile picture in the card (larger size)
                if (profilePicture != null) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(getProfilePictureSize(isInCard = true))
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        error = painterResource(R.drawable.ic_launcher_foreground)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Profile",
                        tint = colorResource(R.color.dark_primary),
                        modifier = Modifier.size(getProfilePictureSize(isInCard = true))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Name", Icons.Filled.AccountCircle, name, colorResource(R.color.dark_primary), colorResource(R.color.dark_on_surface))
            InfoRow("Email", Icons.Filled.Email, email, colorResource(R.color.dark_primary), colorResource(R.color.dark_on_surface))
            InfoRow("Date of Birth", Icons.Filled.CalendarToday, dob, colorResource(R.color.dark_primary), colorResource(R.color.dark_on_surface))
            InfoRow("Gender", Icons.Filled.Person, formatGender(gender), colorResource(R.color.dark_primary), colorResource(R.color.dark_on_surface))
        }
    }
}

@Composable
fun InfoRow(label: String, icon: ImageVector, value: String, iconColor: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, "$label Icon", tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}

fun formatGender(genderCode: String): String {
    return when (genderCode.uppercase()) {
        "M" -> "Male"
        "F" -> "Female"
        "O", "OTHER" -> "Other"
        else -> genderCode.ifBlank { "Not specified" } // Handle blank gender
    }
}

@Composable
fun ShowOTP(otp: String, onCopy: () -> Unit, onCancel: () -> Unit) {
    Log.d(TAG, "ShowOTP Dialog recomposing. OTP: $otp")
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("New Patient Registration Code", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { // Increased spacing
                Text(
                    "Share this One-Time Code with the new patient for their registration process.",
                    style = MaterialTheme.typography.bodyMedium // Slightly larger than bodySmall
                )
                Surface( // Add a subtle background to the OTP row
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            otp,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineMedium, // More prominent
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onCopy, modifier = Modifier.size(48.dp)) { // Larger touch target
                            Icon(Icons.Filled.ContentCopy, "Copy Code", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) { // Using TextButton for confirm action often
                Text("CLOSE")
            }
        },
        dismissButton = { // Optionally, add a more explicit copy button if preferred over icon-only
            // TextButton(onClick = { onCopy(); onCancel() }) { // Copy and then close
            //     Text("COPY & CLOSE")
            // }
        }
    )
}

// Helper function for responsive profile picture sizing
@Composable
private fun getProfilePictureSize(isInCard: Boolean = false): androidx.compose.ui.unit.Dp {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return if (isInCard) {
        // Larger size for card display, responsive to screen size
        when {
            screenWidth >= 400.dp -> 72.dp
            screenWidth >= 360.dp -> 64.dp
            else -> 56.dp
        }
    } else {
        // Smaller size for top bar, responsive to screen size
        when {
            screenWidth >= 400.dp -> 44.dp
            screenWidth >= 360.dp -> 40.dp
            else -> 36.dp
        }
    }
}