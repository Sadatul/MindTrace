package com.example.frontend.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.signOutUser


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPatient(
    errorMsg: String? = null,
    onNavigateToChat: () -> Unit = {}
) {

    var userInfo: UserInfo? by remember { mutableStateOf(null) }

    LaunchedEffect(null) {
        userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
    }    Scaffold(
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(R.color.dark_surface),
                    titleContentColor = colorResource(R.color.dark_on_surface)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { 
                    Text(
                        "Get Help",
                        color = colorResource(R.color.dark_on_primary),
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = { 
                    Icon(
                        Icons.Filled.HelpOutline, 
                        contentDescription = "Help",
                        tint = colorResource(R.color.dark_on_primary)
                    )
                },
                onClick = onNavigateToChat,
                modifier = Modifier.padding(16.dp),
                containerColor = colorResource(R.color.card_patient),
                contentColor = colorResource(R.color.dark_on_primary),
                shape = RoundedCornerShape(16.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
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
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {            if (userInfo != null) {
                PatientInfoCard(
                    name = userInfo!!.name,
                    email = userInfo!!.email,
                    gender = userInfo!!.gender,
                    dob = userInfo!!.dob
                )
            }

            if (!errorMsg.isNullOrBlank()) {
                ErrorDisplay(errorMsg)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    RetrofitInstance.dementiaAPI.signOutUser()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.warning_orange),
                    contentColor = colorResource(R.color.dark_on_primary)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Filled.ExitToApp, 
                    contentDescription = "Sign Out",
                    tint = colorResource(R.color.dark_on_primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign Out", 
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_on_primary)
                )
            }

        }
    }
}

@Composable
fun PatientInfoCard(name: String, email: String, gender: String, dob: String) {
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
            Text(
                text = "Patient Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.dark_primary),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Row for Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Name Icon",
                    tint = colorResource(R.color.dark_primary),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Name",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f)
                    )
                    Text(
                        name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.dark_on_surface)
                    )
                }
            }

            // Row for Email
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Email Icon",
                    tint = colorResource(R.color.dark_primary),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Email",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f)
                    )
                    Text(
                        email,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.dark_on_surface)
                    )
                }
            }

            // Row for Date of Birth
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "Date of Birth Icon",
                    tint = colorResource(R.color.dark_primary),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Date of Birth",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f)
                    )
                    Text(
                        dob,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.dark_on_surface)
                    )
                }
            }

            // Row for Gender with inlined formatting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Gender Icon",
                    tint = colorResource(R.color.dark_primary),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Gender",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f)
                    )
                    Text(
                        // Inlined formatGender logic
                        text = when (gender.uppercase()) {
                            "M" -> "Male"
                            "F" -> "Female"
                            "O", "OTHER" -> "Other"
                            else -> gender // Fallback
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.dark_on_surface)
                    )
                }
            }
        }
    }
}

// InfoRow composable can now be removed if it's not used anywhere else
// @Composable
// fun InfoRow(label: String, icon: ImageVector, value: String) { ... }

@Composable
fun ErrorDisplay(errorMsg: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ReportProblem,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}