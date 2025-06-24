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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.UserInfo
import com.example.frontend.api.models.PartnerInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMyPartners(
    currentUser: UserInfo,
    partners: List<PartnerInfo>,
    isPatientView: Boolean, // true if viewing caregivers, false if viewing patients
    onNavigateBack: () -> Unit,
    onAddPartner: () -> Unit = {},
    isLoading: Boolean = false
) {
    val relationshipTitle = if (isPatientView) "Caregivers" else "Patients"
    val singleRelationshipTitle = if (isPatientView) "Caregiver" else "Patient"
    
    Scaffold(
        containerColor = colorResource(R.color.dark_background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${currentUser.name}'s $relationshipTitle",
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
                            contentDescription = "Back to Dashboard",
                            tint = colorResource(R.color.white),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    // Current user profile picture in top bar
                    Box(
                        modifier = Modifier.padding(end = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser.profilePicture != null) {
                            AsyncImage(
                                model = currentUser.profilePicture,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(getTopBarProfilePictureSize())
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Default Profile",
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
                    contentDescription = "Add",
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
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "No $relationshipTitle",
                                tint = colorResource(R.color.dark_on_surface).copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No $relationshipTitle Yet",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.dark_on_surface),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your first $singleRelationshipTitle to get started",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Partners list
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(partners) { partner ->
                            PartnerCard(
                                partner = partner,
                                isPatientView = isPatientView
                            )
                        }
                        // Add spacing at bottom for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerCard(
    partner: PartnerInfo,
    isPatientView: Boolean
) {
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
                    text = if (isPatientView) "Caregiver" else "Patient",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPatientView) 
                        colorResource(R.color.card_patient) 
                    else 
                        colorResource(R.color.card_caregiver),
                    modifier = Modifier.weight(1f)
                )
                
                // Partner profile picture
                if (partner.profilePicture != null) {
                    AsyncImage(
                        model = partner.profilePicture,
                        contentDescription = "Partner Profile Picture",
                        modifier = Modifier
                            .size(getPartnerProfilePictureSize())
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Profile",
                        tint = if (isPatientView) 
                            colorResource(R.color.card_patient) 
                        else 
                            colorResource(R.color.card_caregiver),
                        modifier = Modifier.size(getPartnerProfilePictureSize())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Partner information
            PartnerInfoRow(
                label = "Name", 
                value = partner.name, 
                icon = Icons.Default.AccountCircle
            )
            PartnerInfoRow(
                label = "ID", 
                value = partner.id, 
                icon = Icons.Default.Badge
            )
            PartnerInfoRow(
                label = "Gender",
                value = when (partner.gender.uppercase()) {
                    "M" -> "Male"
                    "F" -> "Female"
                    "O", "OTHER" -> "Other"
                    else -> partner.gender.ifBlank { "Not specified" }
                },
                icon = Icons.Default.Person
            )
            PartnerInfoRow(
                label = "Joined", 
                value = partner.createdAt, 
                icon = Icons.Default.CalendarToday
            )
            
            // Show if deleted
            if (partner.removeAt != null) {
                PartnerInfoRow(
                    label = "Removed", 
                    value = partner.removeAt, 
                    icon = Icons.Default.CalendarToday
                )
            }
        }
    }
}

@Composable
private fun PartnerInfoRow(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label Icon",
            tint = colorResource(R.color.dark_primary),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.dark_on_surface)
            )
        }
    }
}

@Composable
private fun getTopBarProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    return when {
        screenWidth >= 400.dp -> 40.dp
        screenWidth >= 360.dp -> 36.dp
        else -> 32.dp
    }
}

@Composable
private fun getPartnerProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    return when {
        screenWidth >= 400.dp -> 56.dp
        screenWidth >= 360.dp -> 48.dp
        else -> 40.dp
    }
}
