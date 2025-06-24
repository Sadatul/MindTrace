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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMyPartners(
    currentUser: UserInfo,
    partners: List<PartnerInfo>,
    isPatientView: Boolean, // true if patient is viewing their caregivers, false if caregiver is viewing their patients
    onNavigateBack: () -> Unit,
    onAddPartner: () -> Unit = {},
    isLoading: Boolean = false,
    onPartnerDeleted: (() -> Unit)? = null // callback to refresh after delete
) {
    val relationshipTitle = if (isPatientView) "Caregivers" else "Patients"
    val singleRelationshipTitle = if (isPatientView) "Caregiver" else "Patient"

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var otpLoading by remember { mutableStateOf(false) }
    var selectedPartner by remember { mutableStateOf<PartnerInfo?>(null) }

    // Add dialog state for Add Partner
    var showAddDialog by remember { mutableStateOf(false) }
    var addLoading by remember { mutableStateOf(false) }
    var addError by remember { mutableStateOf<String?>(null) }
    var newCaregiverName by remember { mutableStateOf("") }
    var newCaregiverId by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${currentUser.name}'s $relationshipTitle",
                        color = Color(0xFFE0E0E0),
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
                        if (currentUser.profilePicture != null) {
                            AsyncImage(
                                model = currentUser.profilePicture,
                                contentDescription = "Current user profile picture",
                                modifier = Modifier
                                    .size(getTopBarProfilePictureSize())
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Default profile picture",
                                tint = Color(0xFF90CAF9),
                                modifier = Modifier.size(getTopBarProfilePictureSize())
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isPatientView)
                        Color(0xFF1976D2)
                    else
                        Color(0xFF388E3C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(16.dp),
                containerColor = if (isPatientView)
                    Color(0xFF90CAF9)
                else
                    Color(0xFF43A047), // More vibrant green for caregivers
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add $singleRelationshipTitle",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add $singleRelationshipTitle",
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
                        colors = if (isPatientView) listOf(
                            Color(0xFF43A047), // Green start
                            Color(0xFF66BB6A), // Green end
                            Color(0xFF121212)
                        ) else listOf(
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
                    EmptyPartnersState(relationshipTitle, singleRelationshipTitle)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(partners) { partner ->
                            PartnerCard(
                                partner = partner,
                                currentRoleIsPatient = isPatientView, // Pass explicitly for clarity in card
                                onDelete = {
                                    selectedPartner = partner
                                    showDeleteDialog = true
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Spacing for FAB
                        }
                    }
                }
            }

            if (showDeleteDialog && selectedPartner != null) {
                val partnerName = selectedPartner!!.name
                val partnerRole = if (isPatientView) "caregiver" else "patient"
                DialogDeleteConfirm(
                    partnerName = partnerName,
                    partnerRole = partnerRole,
                    onConfirm = {
                        showDeleteDialog = false
                        otpError = null
                        if (isPatientView) {
                            // Patient deleting caregiver: show OTP dialog
                            showOtpDialog = true
                        } else {
                            // Caregiver deleting patient: call API directly
                            otpLoading = true
                            coroutineScope.launch {
                                try {
                                    val api = RetrofitInstance.dementiaAPI
                                    val success = api.removePatient(selectedPartner!!.id)
                                    if (success) {
                                        showOtpDialog = false
                                        onPartnerDeleted?.invoke()
                                    } else {
                                        otpError = "Failed to delete partner."
                                    }
                                } catch (_: Exception) {
                                    otpError = "Failed to delete partner: ${'$'}{e.message}"
                                } finally {
                                    otpLoading = false
                                }
                            }
                        }
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }
            if (showOtpDialog && selectedPartner != null) {
                val partnerName = selectedPartner!!.name
                DialogOTP(
                    title = "Confirm deletion of $partnerName",
                    onConfirm = { otp ->
                        otpLoading = true
                        otpError = null
                        coroutineScope.launch {
                            try {
                                val api = RetrofitInstance.dementiaAPI
                                val success = if (isPatientView) {
                                    api.deleteCaregiver(selectedPartner!!.id, otp)
                                } else {
                                    api.removePatient(selectedPartner!!.id)
                                }
                                if (success) {
                                    showOtpDialog = false
                                    onPartnerDeleted?.invoke()
                                } else {
                                    otpError = "Invalid OTP or failed to delete."
                                }
                            } catch (_: Exception) {
                                otpError = "Invalid OTP or failed to delete: ${'$'}{e.message}"
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
            if (showAddDialog) {
                val addTitle = if (isPatientView) "Add Caregiver" else "Add Patient"
                val addButtonText = if (isPatientView) "Add Caregiver" else "Add Patient"
                val nameLabel = if (isPatientView) "Caregiver Name" else "Patient Name"
                val idLabel = if (isPatientView) "Caregiver ID" else "Patient ID"
                DialogAddConfirm(
                    title = addTitle,
                    name = newCaregiverName,
                    onNameChange = { newCaregiverName = it },
                    id = newCaregiverId,
                    onIdChange = { newCaregiverId = it },
                    loading = addLoading,
                    errorMessage = addError,
                    confirmButtonText = addButtonText,
                    nameLabel = nameLabel,
                    idLabel = idLabel,
                    onConfirm = {
                        addLoading = true
                        addError = null
                        coroutineScope.launch {
                            try {
                                val api = RetrofitInstance.dementiaAPI
                                val success = if (isPatientView) {
                                    api.addCaregiver(newCaregiverId, newCaregiverName)
                                } else {
                                    api.addPatient(newCaregiverId, newCaregiverName)
                                }
                                if (success) {
                                    showAddDialog = false
                                    newCaregiverName = ""
                                    newCaregiverId = ""
                                    onPartnerDeleted?.invoke() // Refresh list
                                } else {
                                    addError = "Failed to add ${if (isPatientView) "caregiver" else "patient"}."
                                }
                            } catch (e: Exception) {
                                addError = "Failed to add ${if (isPatientView) "caregiver" else "patient"}: ${'$'}{e.message}"
                            } finally {
                                addLoading = false
                            }
                        }
                    },
                    onDismiss = {
                        showAddDialog = false
                        addLoading = false
                        addError = null
                    }
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
    currentRoleIsPatient: Boolean, // Explicitly pass the current user's role for clarity
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val cardGradient = Brush.verticalGradient(
        colors = if (currentRoleIsPatient) listOf(
            Color(0xFF43A047), Color(0xFF388E3C), Color(0xFF212121)
        ) else listOf(
            colorResource(R.color.card_caregiver), colorResource(R.color.dark_surface_variant)
        )
    )


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
                .padding(horizontal = 20.dp, vertical = 16.dp) // Adjusted padding
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp) // Adjusted spacing
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
                    color = cardColor, // Color based on partner's role relative to viewer
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
                        tint = cardColor, // Color based on partner's role relative to viewer
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
private fun getTopBarProfilePictureSize(): Dp {
    // Simplified, or use your existing LocalConfiguration logic if preferred
    return 36.dp
}

@Composable
private fun getPartnerProfilePictureSize(): Dp {
    // Simplified, or use your existing LocalConfiguration logic if preferred
    return 48.dp
}

// Remove the inline DialogAddConfirm composable at the bottom of this file, as it is now imported from its own file