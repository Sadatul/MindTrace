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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance // Ensure this is the correct import for your API instance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.models.PartnerInfo
import kotlinx.coroutines.launch // For the coroutine scope inside LaunchedEffect

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
    val relationshipTitle = if (isPatientView) stringResource(R.string.caregivers_title) else stringResource(R.string.patients_title)
    val singleRelationshipTitle = if (isPatientView) stringResource(R.string.caregiver_singular) else stringResource(R.string.patient_singular)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var otpLoading by remember { mutableStateOf(false) }
    var selectedPartner by remember { mutableStateOf<PartnerInfo?>(null) }
    val coroutineScope = rememberCoroutineScope() // Get coroutine scope

    Scaffold(
        containerColor = colorResource(R.color.dark_background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        // Use string resource with placeholder if possible
                        text = stringResource(R.string.partners_screen_title, currentUser.name, relationshipTitle),
                        color = colorResource(R.color.dark_on_surface),
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
                            contentDescription = stringResource(R.string.back_to_dashboard_description),
                            tint = colorResource(R.color.white),
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
                                contentDescription = stringResource(R.string.current_user_profile_picture_description),
                                modifier = Modifier
                                    .size(getTopBarProfilePictureSize())
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = stringResource(R.string.default_profile_picture_description),
                                tint = colorResource(R.color.dark_primary),
                                modifier = Modifier.size(getTopBarProfilePictureSize())
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isPatientView)
                        colorResource(R.color.gradient_patient_start)
                    else
                        colorResource(R.color.gradient_caregiver_start),
                    titleContentColor = colorResource(R.color.white),
                    navigationIconContentColor = colorResource(R.color.white),
                    actionIconContentColor = colorResource(R.color.white)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPartner,
                modifier = Modifier.padding(16.dp),
                containerColor = if (isPatientView)
                    colorResource(R.color.card_patient)
                else
                    colorResource(R.color.card_caregiver),
                contentColor = colorResource(R.color.white),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_partner_description),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.add_relationship_button, singleRelationshipTitle),
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
                            colorResource(R.color.gradient_patient_start),
                            colorResource(R.color.gradient_patient_end),
                            colorResource(R.color.dark_background)
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
                                isPatientView = isPatientView, // This is current user's role context
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
                val partnerRole = if (isPatientView) stringResource(R.string.caregiver_singular_lowercase) else stringResource(R.string.patient_singular_lowercase)
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
                                        otpError = stringResource(R.string.delete_failed_error)
                                    }
                                } catch (e: Exception) {
                                    otpError = stringResource(R.string.delete_failed_error) + ": ${'$'}{e.message}"
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
                    title = stringResource(R.string.otp_dialog_title_delete, partnerName),
                    onConfirm = { otp ->
                        otpLoading = true
                        otpError = null
                        coroutineScope.launch {
                            try {
                                val api = RetrofitInstance.dementiaAPI
                                val success = api.deleteCaregiver(selectedPartner!!.id, otp)
                                if (success) {
                                    showOtpDialog = false
                                    onPartnerDeleted?.invoke()
                                } else {
                                    otpError = stringResource(R.string.otp_invalid_or_delete_failed)
                                }
                            } catch (e: Exception) {
                                otpError = stringResource(R.string.otp_invalid_or_delete_failed) + ": ${'$'}{e.message}"
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
                imageVector = Icons.Default.Person, // Consider a more specific icon like Groups or People
                contentDescription = stringResource(R.string.no_partners_icon_description, relationshipTitle),
                tint = colorResource(R.color.dark_on_surface).copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_partners_yet_title, relationshipTitle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.dark_on_surface),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.add_first_partner_prompt, singleRelationshipTitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun PartnerCard(
    partner: PartnerInfo,
    isPatientView: Boolean, // True if the current user is a Patient (viewing Caregivers)
    // False if the current user is a Caregiver (viewing Patients)
    currentRoleIsPatient: Boolean, // Explicitly pass the current user's role for clarity
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Determine the role of the partner being displayed in this card
    val partnerRoleDisplay = if (currentRoleIsPatient) {
        stringResource(R.string.caregiver_singular) // If I'm a patient, the partner is a caregiver
    } else {
        stringResource(R.string.patient_singular)   // If I'm a caregiver, the partner is a patient
    }
    val cardColor = if (currentRoleIsPatient) { // Color depends on who the partner is
        colorResource(R.color.card_patient) // If I'm a patient, this card represents a caregiver (use patient theme color for their partners)
    } else {
        colorResource(R.color.card_caregiver) // If I'm a caregiver, this card represents a patient (use caregiver theme color for their partners)
    }


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
                        contentDescription = stringResource(R.string.partner_profile_picture_description, partner.name),
                        modifier = Modifier
                            .size(getPartnerProfilePictureSize())
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                        // Add placeholder/error drawables
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.default_partner_profile_picture_description),
                        tint = cardColor, // Color based on partner's role relative to viewer
                        modifier = Modifier.size(getPartnerProfilePictureSize())
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.partner_options_menu_description),
                            tint = colorResource(R.color.dark_primary)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(colorResource(id = R.color.dark_surface)) // Match theme
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_relationship_action, partnerRoleDisplay), color = colorResource(id = R.color.warning_orange)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete, // Use Delete icon
                                    contentDescription = null,
                                    tint = colorResource(id = R.color.warning_orange)
                                )
                            }
                        )
                    }
                }
            }

            // Separator(optional, can make card look cleaner)
            // Divider(color = colorResource(id = R.color.dark_on_surface).copy(alpha = 0.12f), thickness = 1.dp)

            PartnerInfoRow(
                label = stringResource(R.string.label_name),
                value = partner.name,
                icon = Icons.Default.AccountCircle
            )
            PartnerInfoRow(
                label = stringResource(R.string.label_id), // Or "User ID"
                value = partner.id,
                icon = Icons.Default.Badge
            )
            PartnerInfoRow(
                label = stringResource(R.string.label_gender),
                value = formatGenderDisplay(partner.gender), // Use utility function
                icon = Icons.Default.Person
            )
            PartnerInfoRow(
                label = stringResource(R.string.label_joined_on),
                value = partner.createdAt, // Consider formatting this date
                icon = Icons.Default.CalendarToday
            )

            if (partner.removeAt != null) {
                PartnerInfoRow(
                    label = stringResource(R.string.label_removed_on),
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
        "M" -> stringResource(R.string.gender_male)
        "F" -> stringResource(R.string.gender_female)
        "O", "OTHER" -> stringResource(R.string.gender_other)
        else -> genderCode.ifBlank { stringResource(R.string.gender_not_specified) }
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

// Dummy DialogDeleteConfirm and DialogOTP for compilation - replace with your actual implementations
@Composable
fun DialogDeleteConfirm(partnerName: String, partnerRole: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $partnerRole?") },
        text = { Text("Are you sure you want to delete $partnerName as your $partnerRole?") },
        confirmButton = { Button(onClick = onConfirm) { Text("Confirm") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DialogOTP(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String?,
    loading: Boolean
) {
    var otpValue by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = otpValue,
                    onValueChange = { otpValue = it },
                    label = { Text("Enter OTP") },
                    isError = errorMessage != null,
                    singleLine = true
                )
                if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
                if (loading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(otpValue) }, enabled = !loading) { Text("Submit") } },
        dismissButton = { Button(onClick = onDismiss, enabled = !loading) { Text("Cancel") } }
    )
}