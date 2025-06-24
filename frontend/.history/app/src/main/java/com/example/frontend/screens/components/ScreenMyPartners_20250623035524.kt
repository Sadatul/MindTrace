package com.example.frontend.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
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
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.deleteCaregiver
import com.example.frontend.api.models.PartnerInfo
import com.example.frontend.api.removePatient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Define a sealed class for role and theme
sealed class PartnerScreenRole(
    val relationshipTitle: String,
    val singleRelationshipTitle: String,
    val primaryColor: Color,
    val gradientColors: List<Color>,
    val cardGradient: List<Color>,
    val cardColor: Color,
    val profileTint: Color
) {
    object Patient : PartnerScreenRole(
        relationshipTitle = "Caregivers",
        singleRelationshipTitle = "Caregiver",
        primaryColor = Color(0xFF388E3C),
        gradientColors = listOf(Color(0xFF388E3C), Color(0xFF1B2E1B)),
        cardGradient = listOf(Color(0xFF232B3A), Color(0xFF10151C)),
        cardColor = Color(0xFF64B5F6),
        profileTint = Color(0xFF66BB6A)
    )
    object Caregiver : PartnerScreenRole(
        relationshipTitle = "Patients",
        singleRelationshipTitle = "Patient",
        primaryColor = Color(0xFF1976D2),
        gradientColors = listOf(Color(0xFF1976D2), Color(0xFF101C2C)),
        cardGradient = listOf(Color(0xFF234D23), Color(0xFF101C10)),
        cardColor = Color(0xFF81C784),
        profileTint = Color(0xFF90CAF9)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMyPartners(
    currentUser: UserInfo,
    partners: List<PartnerInfo>,
    role: PartnerScreenRole, // Use sealed class for role and theme
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    onPartnerDeleted: (() -> Unit)? = null // callback to refresh after delete
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var selectedPartner by remember { mutableStateOf<PartnerInfo?>(null) }

    // Add dialog state for Add Partner
    var showAddPartnerDialog by remember { mutableStateOf(false) }
    var addPatientIdForOtp by remember { mutableStateOf<String?>(null) }
    var addLoading by remember { mutableStateOf(false) }
    var addError by remember { mutableStateOf<String?>(null) }

    var otpLoading by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = role.primaryColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${currentUser.name}'s ${role.relationshipTitle}",
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
                                    tint = role.profileTint,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = role.primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    showAddPartnerDialog = true
                },
                modifier = Modifier.padding(16.dp),
                containerColor = role.primaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add ${role.singleRelationshipTitle}",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add ${role.singleRelationshipTitle}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = role.gradientColors
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.dark_primary)
                        )
                    }
                } else if (partners.isEmpty()) {
                    EmptyPartnersState(role.relationshipTitle, role.singleRelationshipTitle)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(partners) { partner ->
                            PartnerCard(
                                partner = partner,
                                role = role,
                                onDelete = {
                                    selectedPartner = partner
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            // Delete confirmation dialog, then OTP dialog
            if (showDeleteConfirmDialog && selectedPartner != null) {
                DialogDeleteConfirm(
                    partnerName = selectedPartner!!.name,
                    partnerRole = role.singleRelationshipTitle,
                    onConfirm = {
                        showDeleteConfirmDialog = false
                        showDeleteDialog = true
                    },
                    onDismiss = {
                        showDeleteConfirmDialog = false
                        selectedPartner = null
                    }
                )
            }
            if (showDeleteDialog && selectedPartner != null) {
                DialogOTP(
                    title = "Enter OTP to delete ${role.singleRelationshipTitle.lowercase()}",
                    onConfirm = { otp ->
                        otpLoading = true
                        otpError = null
                        coroutineScope.launch {
                            try {
                                val api = RetrofitInstance.dementiaAPI
                                val success = when (role) {
                                    is PartnerScreenRole.Patient -> api.deleteCaregiver(selectedPartner!!.id, otp)
                                    is PartnerScreenRole.Caregiver -> api.removePatient(selectedPartner!!.id)
                                }
                                if (success) {
                                    showDeleteDialog = false
                                    onPartnerDeleted?.invoke()
                                } else {
                                    otpError = "Invalid OTP or failed to delete."
                                }
                            } catch (_: Exception) {
                                otpError = "Invalid OTP or failed to delete."
                            } finally {
                                otpLoading = false
                            }
                        }
                    },
                    onDismiss = {
                        showDeleteDialog = false
                        otpLoading = false
                    },
                    errorMessage = otpError,
                    loading = otpLoading
                )
            }
            if (showOtpDialog && selectedPartner != null) {
                DialogOTP(
                    title = "Confirm deletion of ${selectedPartner!!.name}",
                    onConfirm = { otp ->
                        otpLoading = true
                        otpError = null
                        coroutineScope.launch {
                            try {
                                val api = RetrofitInstance.dementiaAPI
                                val success = when (role) {
                                    is PartnerScreenRole.Patient -> api.deleteCaregiver(selectedPartner!!.id, otp)
                                    is PartnerScreenRole.Caregiver -> api.removePatient(selectedPartner!!.id)
                                }
                                if (success) {
                                    showOtpDialog = false
                                    onPartnerDeleted?.invoke()
                                } else {
                                    otpError = "Invalid OTP or failed to delete."
                                }
                            } catch (_: Exception) {
                                otpError = "Invalid OTP or failed to delete."
                            } finally {
                                otpLoading = false
                            }
                        }
                    },
                    onDismiss = {
                        showOtpDialog = false
                        otpLoading = false
                    },
                    errorMessage = otpError,
                    loading = otpLoading
                )
            }
            if (showAddPartnerDialog) {
                DialogAddPartner(
                    role = role,
                    onDismiss = { showAddPartnerDialog = false },
                    onOtpRequested = { patientId ->
                        showAddPartnerDialog = false
                        addPatientIdForOtp = patientId
                    }
                )
            }
            if (addPatientIdForOtp != null && role is PartnerScreenRole.Caregiver) {
                DialogOTP(
                    title = "Enter OTP to add patient",
                    onConfirm = { otp ->
                        addLoading = true
                        addError = null
                        coroutineScope.launch {
                            try {
                                // Call your addPatient API here with addPatientIdForOtp and otp
                                addPatientIdForOtp = null
                            } catch (_: Exception) {
                                addError = "Failed to add patient"
                            } finally {
                                addLoading = false
                            }
                        }
                    },
                    onDismiss = {
                        addPatientIdForOtp = null
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
fun EmptyPartnersState(relationshipTitle: String, singleRelationshipTitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "No $relationshipTitle icon",
                tint = Color(0xFFE0E0E0).copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No $relationshipTitle yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E0E0),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first $singleRelationshipTitle to get started!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE0E0E0).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun PartnerCard(
    partner: PartnerInfo,
    role: PartnerScreenRole,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val cardGradient = Brush.verticalGradient(colors = role.cardGradient)
    val partnerRoleDisplay = role.singleRelationshipTitle
    val cardColor = role.cardColor
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(cardGradient),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Display the role of the person in the card
                Text(
                    text = partnerRoleDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = cardColor,
                    modifier = Modifier.weight(1f)
                )

                if (partner.profilePicture != null) {
                    AsyncImage(
                        model = partner.profilePicture,
                        contentDescription = "Partner profile picture: ${partner.name}",
                        modifier = Modifier
                            .size(getPartnerProfilePictureSize())
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                        // Add placeholder/error drawables
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default partner profile picture",
                        tint = cardColor,
                        modifier = Modifier.size(getPartnerProfilePictureSize())
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Partner options menu",
                            tint = colorResource(R.color.dark_primary)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(colorResource(id = R.color.dark_surface)) // Match theme
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete $partnerRoleDisplay", color = Color(0xFFFF9800)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete, // Use Delete icon
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800)
                                )
                            }
                        )
                    }
                }
            }

            // Separator(optional, can make card look cleaner)
            // Divider(color = colorResource(id = R.color.dark_on_surface).copy(alpha = 0.12f), thickness = 1.dp)

            PartnerInfoRow(
                label = "Name",
                value = partner.name,
                icon = Icons.Default.AccountCircle
            )
            PartnerInfoRow(
                label = "User ID",
                value = partner.id,
                icon = Icons.Default.Badge
            )
            PartnerInfoRow(
                label = "Gender",
                value = formatGenderDisplay(partner.gender), // Use utility function
                icon = Icons.Default.Person
            )
            PartnerInfoRow(
                label = "Joined on",
                value = partner.createdAt, // Consider formatting this date
                icon = Icons.Default.CalendarToday
            )

            if (partner.removeAt != null) {
                PartnerInfoRow(
                    label = "Removed on",
                    value = partner.removeAt, // Consider formatting this date
                    icon = Icons.Default.CalendarToday,
                    valueColor = colorResource(id = R.color.warning_orange) // Highlight removed status
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
    valueColor: Color = colorResource(R.color.dark_on_surface) // Default color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Label text serves as description
            tint = colorResource(R.color.dark_primary),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) { // Allow text to wrap if too long
            Text(
                label,
                style = MaterialTheme.typography.labelMedium, // Slightly larger for better readability
                color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor,
                maxLines = 2, // Prevent very long values from breaking layout
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// Utility function to format gender for display (using string resources is best)
@Composable
fun formatGenderDisplay(genderCode: String): String {
    return when (genderCode.uppercase()) {
        "M" -> "Male"
        "F" -> "Female"
        "O", "OTHER" -> "Other"
        else -> genderCode.ifBlank { "Not specified" }
    }
}


@Composable
private fun getPartnerProfilePictureSize(): Dp {
    return 48.dp
}

// Remove the inline DialogAddConfirm composable at the bottom of this file, as it is now imported from its own file

