package com.example.frontend.screens.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.RequestPatientAdd
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.addPatient
import com.example.frontend.api.deleteCaregiver
import com.example.frontend.api.getPartners
import com.example.frontend.api.models.PartnerInfo
import com.example.frontend.api.removePatient
import com.example.frontend.api.sendCaregiverRemovalOTP
import com.example.frontend.screens.NavigationBarComponent
import com.example.frontend.screens.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMyPartners(
    currentUser: UserInfo,
    partners: List<PartnerInfo>, // Accepts List, not MutableList
    role: String, // Use simple String for role: "Caregiver" or "Patient"
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    showDeletedPartners: Boolean = false,
    onToggleDeleted: () -> Unit = {},
    onShowLogs: ((PartnerInfo) -> Unit)? = null, // Make optional for patient flow
    navigationBar: NavigationBarComponent
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Responsive bottom padding based on screen size
    val fabPadding = when {
        screenHeight < 600.dp -> 100.dp // Small screens
        screenHeight < 800.dp -> 120.dp // Medium screens  
        else -> 140.dp // Large screens
    }
    
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var selectedPartner by remember { mutableStateOf<PartnerInfo?>(null) }

    // Add dialog state for Add Partner
    var showAddPartnerDialog by remember { mutableStateOf(false) }
    var addPatientIdForOtp by remember { mutableStateOf("") }
    var addLoading by remember { mutableStateOf(false) }
    var addError by remember { mutableStateOf<String?>(null) }

    var otpLoading by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val isCaregiver = role.equals("CAREGIVER", ignoreCase = true)
    val isPatient = role.equals("PATIENT", ignoreCase = true)

    val primaryColor = if (isCaregiver) Color(0xFF1976D2) else Color(0xFF388E3C)
    val gradientColors = if (isCaregiver) listOf(Color(0xFF1976D2), Color(0xFF101C2C)) else listOf(Color(0xFF388E3C), Color(0xFF1B2E1B))
    val profileTint = if (isCaregiver) Color(0xFF90CAF9) else Color(0xFF66BB6A)

    // --- LOCAL STATE FOR PARTNERS ---
    var partnersState by remember { mutableStateOf(partners) }
    // Keep partnersState in sync with incoming partners prop
    LaunchedEffect(partners) {
        partnersState = partners
    }

    Scaffold(
        containerColor = primaryColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${currentUser.name}'s ${if (isCaregiver) "PATIENTS" else "CAREGIVERS"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to dashboard",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    // Profile picture only
                    Box(
                        modifier = Modifier.padding(end = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background circle for profile picture visibility
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Color.White.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentUser.profilePicture != null) {
                                AsyncImage(
                                    model = currentUser.profilePicture,
                                    contentDescription = "Current user profile picture",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Default profile picture",
                                    tint = profileTint,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = "ADD ${if (isCaregiver) "PATIENT" else "CAREGIVER"}",
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add ${if (isCaregiver) "PATIENT" else "CAREGIVER"}",
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    // Only execute the action if not in a loading state
                    if (!isLoading) {
                        showAddPartnerDialog = true
                    }
                },
                expanded = true,
                modifier = Modifier.padding(16.dp),
                // Visually indicate the button's state by changing its color when loading
                containerColor = if (isLoading) primaryColor.copy(alpha = 0.5f) else primaryColor,
                contentColor = if (isLoading) Color.White.copy(alpha = 0.5f) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        },
        bottomBar = {
            if (role == "PATIENT") navigationBar.PatientNavigationBar(Screen.MyCaregivers)
            else TODO("caregiver flow should go here")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            width = 2.dp,
                            color = if (showDeletedPartners) Color(0xFFEF5350) else Color(0xFF43A047), // Red for removed, green for active
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = if (showDeletedPartners) Color(0xFFFFCDD2) else Color(0xFFB9F6CA), // Light red or light green
                    contentColor = Color.Black,
                    tonalElevation = 6.dp,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleDeleted() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = showDeletedPartners,
                            onCheckedChange = { onToggleDeleted() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (showDeletedPartners) Color(0xFFEF5350) else Color(0xFF43A047), // Vibrant red or green
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF43A047)
                            ),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = if (showDeletedPartners) {
                                "Including Removed ${if (isCaregiver) "Patients" else "Caregivers"}"
                            } else {
                                "Showing Active ${if (isCaregiver) "Patients" else "Caregivers"} only"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Determine the list of partners to display based on the filter
                val filteredPartners = if (showDeletedPartners) {
                    partnersState
                } else {
                    partnersState.filter { it.removedAt == null }
                }

                // Display content based on loading state and partner list states
                when {
                    isLoading -> {
                        // Show a loading indicator if data is being fetched
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colorResource(R.color.dark_primary) // Or your app's theme color
                            )
                        }
                    }
                    partnersState.isEmpty() -> {
                        // If the original list of partners is empty (no partners at all)
                        EmptyPartnersState(if (isCaregiver) "Patients" else "Caregivers")
                    }
                    filteredPartners.isEmpty() -> {
                        // If the original list is not empty, but the filtered list is.
                        // This means the filter for "active" partners yielded no results.
                        EmptyActivePartnersState(if (isCaregiver) "Patients" else "Caregivers")
                    }
                    else -> {
                        // If there are partners to display after filtering
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = fabPadding) // Responsive spacing for FAB
                        ) {
                            items(filteredPartners) { partner ->
                                PartnerCard(
                                    partner = partner,
                                    role = role,
                                    onDelete = {
                                        selectedPartner = partner
                                        showDeleteConfirmDialog = true
                                    },
                                    onShowLogs = {
                                        println("Selected partner for logs: id=${partner.id}, name=${partner.name}, profilePicture=${partner.profilePicture}")
                                        onShowLogs?.invoke(partner)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- DIALOGS ---
            if (showDeleteConfirmDialog && selectedPartner != null) {
                DialogDeleteConfirm(
                    partnerName = selectedPartner!!.name,
                    partnerRole = if (isCaregiver) "Patient" else "Caregiver",
                    onConfirm = {
                        showDeleteConfirmDialog = false
                        if (isPatient) {
                            // Patient removes caregiver: send OTP, then show OTP dialog only if successful
                            coroutineScope.launch {
                                try {
                                    val otpSent = RetrofitInstance.dementiaAPI.sendCaregiverRemovalOTP(selectedPartner!!.id)
                                    if (otpSent) {
                                        showOtpDialog = true
                                    } else {
                                        otpError = "Failed to send OTP. Please try again."
                                        selectedPartner = null
                                    }
                                } catch (e: Exception) {
                                    otpError = "Error sending OTP: ${e.message ?: "Unknown error"}"
                                    selectedPartner = null
                                }
                            }
                        } else {
                            // Caregiver removes patient: remove directly and refresh list
                            coroutineScope.launch {
                                try {
                                    val success = RetrofitInstance.dementiaAPI.removePatient(selectedPartner!!.id)
                                    if (success) {
                                        val updatedPartners = RetrofitInstance.dementiaAPI.getPartners(includeDeleted = true)
                                        partnersState = updatedPartners
                                        selectedPartner = null
                                    } else {
                                        otpError = "Failed to remove $role."
                                    }
                                } catch (e: Exception) {
                                    otpError = "Error removing $role: ${e.message ?: "Unknown error"}"
                                }
                            }
                        }
                    },
                    onDismiss = {
                        showDeleteConfirmDialog = false
                        selectedPartner = null
                    }
                )
            }

            if (showOtpDialog && selectedPartner != null && isPatient) {
                DialogOTP(
                    title = "Enter OTP to remove ${selectedPartner!!.name}",
                    onConfirm = { otp ->
                        otpLoading = true
                        otpError = null
                        coroutineScope.launch {
                            try {
                                val api = RetrofitInstance.dementiaAPI
                                val success = api.deleteCaregiver(selectedPartner!!.id, otp)
                                if (success) {
                                    // Refresh the partners list from backend
                                    partnersState = RetrofitInstance.dementiaAPI.getPartners(includeDeleted = showDeletedPartners)
                                    showOtpDialog = false
                                    selectedPartner = null
                                } else {
                                    otpError = "Invalid OTP or failed to remove."
                                }
                            } catch (e: Exception) {
                                otpError = "Error: ${e.message ?: "Failed to remove."}"
                            } finally {
                                otpLoading = false
                            }
                        }
                    },
                    onDismiss = {
                        showOtpDialog = false
                        otpLoading = false
                        selectedPartner = null
                        otpError = null
                    },
                    errorMessage = otpError,
                    loading = otpLoading
                )
            }

            if (showAddPartnerDialog) {
                DialogAddPartner(
                    role = role,
                    onDismiss = { showAddPartnerDialog = false },
                    onOtpRequested = { partnerId ->
                        showAddPartnerDialog = false
                        addPatientIdForOtp = partnerId
                    },
                )
            }
            if (addPatientIdForOtp.isNotBlank() && isCaregiver) {
                DialogOTP(
                    title = "Enter OTP to add Patient",
                    onConfirm = { otp ->
                        addLoading = true
                        addError = null
                        coroutineScope.launch {
                            try {
                                val success = RetrofitInstance.dementiaAPI.addPatient(
                                    RequestPatientAdd(patientId = addPatientIdForOtp, otp = otp)
                                )
                                if (success) {
                                    partnersState = RetrofitInstance.dementiaAPI.getPartners(includeDeleted = showDeletedPartners)
                                    addPatientIdForOtp = ""
                                } else {
                                    addError = "Failed to add patient. Invalid OTP or patient not found."
                                }
                            } catch (e: Exception) {
                                addError = "Error adding patient: ${e.message ?: "Unknown error"}"
                            } finally {
                                addLoading = false
                                if (addError == null) {
                                    addPatientIdForOtp = ""
                                }
                            }
                        }
                    },
                    onDismiss = {
                        addPatientIdForOtp = ""
                        addLoading = false
                        addError = null
                    },
                    errorMessage = addError,
                    loading = addLoading
                )
            }
        }
    }
}


@Composable
fun EmptyPartnersState(relationshipTitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person, // Consider Icons.Filled.People for multiple
                contentDescription = "No $relationshipTitle icon",
                tint = Color(0xFFE0E0E0).copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No $relationshipTitle Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E0E0),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the '+' button to add your first $relationshipTitle and get started!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE0E0E0).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun EmptyActivePartnersState(relationshipTitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person, // Consider Icons.Filled.GroupOff
                contentDescription = "No active $relationshipTitle icon",
                tint = Color(0xFFE0E0E0).copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Active $relationshipTitle",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E0E0),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All your $relationshipTitle have been removed or none are currently active. " +
                        "Toggle the filter above to view removed $relationshipTitle, or add a new one.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE0E0E0).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun PartnerCard(
    partner: PartnerInfo,
    role: String,
    onDelete: () -> Unit,
    onShowLogs: (PartnerInfo) -> Unit // Add callback for showing logs
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDeleted = partner.removedAt != null

    val contentOnCardColor = if (isDeleted) Color.Red.copy(alpha = 0.8f) else Color.White // High contrast for readability

    val cardGradient = if (isDeleted) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF5a5a5a), // Darker Grey
                Color(0xFF3a3a3a)  // Very Dark Grey
            )
        )
    } else {
        Brush.verticalGradient(colors = if (role.equals("Caregiver", ignoreCase = true)) listOf(Color(0xFF234D23), Color(0xFF101C10)) else listOf(Color(0xFF232B3A), Color(0xFF10151C)))
    }

    val partnerRoleDisplay = if (isDeleted) {
        "Removed ${if (role.equals("Caregiver", ignoreCase = true)) "Patient" else "Caregiver"}"
    } else {
        if (role.equals("Caregiver", ignoreCase = true)) "Patient" else "Caregiver"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)), // Slightly reduced shadow
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Shadow is handled by modifier
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Gradient is drawn by background
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .background(cardGradient) // Apply gradient to the Column
                .padding(horizontal = 16.dp, vertical = 12.dp) // Adjusted padding
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = partnerRoleDisplay,
                    color = contentOnCardColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (partner.profilePicture != null) {
                    AsyncImage(
                        model = partner.profilePicture,
                        contentDescription = "Partner profile picture",
                        modifier = Modifier
                            .size(getPartnerProfilePictureSize())
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default partner profile picture",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(getPartnerProfilePictureSize())
                    )
                }

                // Show menu only if not deleted
                if (!isDeleted) {
                    // Show menu for both caregivers and patients
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = contentOnCardColor
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Show "Show Logs" only for caregivers viewing patients
                            if (role.equals("Caregiver", ignoreCase = true)) {
                                DropdownMenuItem(
                                    text = { Text("Show Logs") },
                                    onClick = {
                                        showMenu = false
                                        onShowLogs(partner)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                            // Show delete option for both roles with appropriate text
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = if (role.equals("Caregiver", ignoreCase = true)) "Remove Patient" else "Remove Caregiver",
                                        color = Color.Red
                                    ) 
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            HorizontalDivider(color = contentOnCardColor.copy(alpha = 0.2f), thickness = 1.dp)

            PartnerInfoRow(
                label = "Name",
                value = partner.name,
                icon = Icons.Default.AccountCircle,
                contentColor = contentOnCardColor,
                isDeleted = isDeleted
            )
            PartnerInfoRow(
                label = "Gender",
                value = formatGenderDisplay(partner.gender),
                icon = Icons.Default.Person,
                contentColor = contentOnCardColor,
                isDeleted = isDeleted
            )
            PartnerInfoRow(
                label = "Joined On",
                value = formatUserFriendlyDate(partner.createdAt),
                icon = Icons.Default.CalendarToday,
                contentColor = contentOnCardColor,
                isDeleted = isDeleted
            )

            if (partner.removedAt != null) {
                PartnerInfoRow(
                    label = "Removed On",
                    value = formatUserFriendlyDate(partner.removedAt), // Safe call, checked above
                    icon = Icons.Default.CalendarToday,
                    contentColor = Color(0xFFFFCDD2), // Lighter red for visibility
                    isDeleted = true,
                    valueFontWeight = FontWeight.SemiBold // Highlight removed date
                )
            }
        }
    }
}

@Composable
private fun PartnerInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    contentColor: Color, // Pass the base content color for the card
    isDeleted: Boolean,
    valueFontWeight: FontWeight = FontWeight.Medium // Allow customizing value font weight
) {
    val alpha = if (isDeleted) 0.7f else 1.0f // Reduce opacity for all elements if deleted

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor.copy(alpha = alpha * 0.8f), // Slightly less prominent icon
            modifier = Modifier.size(18.dp) // Smaller icon
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall, // Smaller label
                color = contentColor.copy(alpha = alpha * 0.7f) // Subtler label
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = valueFontWeight,
                color = contentColor.copy(alpha = alpha),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// Utility function to format gender for display (using string resources is best)
@Composable
fun formatGenderDisplay(genderCode: String?): String { // Made genderCode nullable
    return when (genderCode?.uppercase()) { // Safe call
        "M" -> "Male"
        "F" -> "Female"
        "O", "OTHER" -> "Other"
        null -> "Not specified"
        else -> genderCode.ifBlank { "Not specified" }
    }
}

// Utility function to format dates in a user-friendly way
fun formatUserFriendlyDate(dateString: String?): String { // Made dateString nullable
    if (dateString.isNullOrBlank()) return "Date not available"
    return try {
        // Handle different possible date formats
        val inputFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", // ISO format with microseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",    // ISO format with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss'Z'",        // ISO format without milliseconds
            "yyyy-MM-dd HH:mm:ss.SSSSSSZ",     // With timezone offset
            "yyyy-MM-dd HH:mm:ssZ",            // With timezone offset
            "yyyy-MM-dd HH:mm:ss.SSSSSS",      // Without timezone
            "yyyy-MM-dd HH:mm:ss",             // Simple format
            "yyyy-MM-dd"                       // Date only
        )

        var parsedDate: Date? = null
        for (formatString in inputFormats) {
            try {
                // Ensure UTC is handled for formats ending with 'Z'
                val sdf = if (formatString.endsWith("'Z'")) {
                    SimpleDateFormat(formatString, Locale.ENGLISH).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                } else {
                    SimpleDateFormat(formatString, Locale.ENGLISH)
                }
                parsedDate = sdf.parse(dateString)
                if (parsedDate != null) break
            } catch (_: Exception) { /* Try next format */ }
        }

        if (parsedDate != null) {
            val now = Calendar.getInstance()
            val then = Calendar.getInstance().apply { time = parsedDate }

            return when {
                isSameDay(now, then) -> "Today, ${SimpleDateFormat("h:mm a", Locale.ENGLISH).format(parsedDate)}"
                isYesterday(now, then) -> "Yesterday, ${SimpleDateFormat("h:mm a", Locale.ENGLISH).format(parsedDate)}"
                then.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> SimpleDateFormat("MMM d, h:mm a", Locale.ENGLISH).format(parsedDate)
                else -> SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.ENGLISH).format(parsedDate)
            }
        } else {
            // If parsing fails, return a cleaned up version of the original
            dateString.replace("T", " at ").substringBefore(".").substringBefore("Z")
        }
    } catch (_: Exception) {
        // Fallback: return the original string with some cleanup
        dateString.replace("T", " at ").substringBefore(".").substringBefore("Z")
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(now: Calendar, then: Calendar): Boolean {
    val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    return isSameDay(yesterday, then)
}


@Composable
private fun getPartnerProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth >= 400.dp -> 48.dp
        screenWidth >= 360.dp -> 44.dp
        else -> 40.dp
    }
}
