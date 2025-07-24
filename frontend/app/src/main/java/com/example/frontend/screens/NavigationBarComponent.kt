package com.example.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.ui.unit.sp
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.SelfUserInfoCache
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken

val NavigationBarSelectedColor = Color(0xFFB9F6CA)
val NavigationBarQRColor = Color(0xFFB3E5FC)

data class NavigationBarComponent(
    val onPatientLogs: () -> Unit,
    val onReminders: () -> Unit,
    val onPatientProfile: (isPatient: Boolean) -> Unit,
    val onChatScreen: () -> Unit,
    val onCaregiverProfile: (isCaregiver: Boolean) -> Unit
) {
    @Composable
    fun PatientNavigationBar(selectedScreen: Screen) {

        var userInfo: UserInfo? by remember { mutableStateOf(null) }
        var showQrDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            userInfo = SelfUserInfoCache.getUserInfo()
        }

        if (showQrDialog && userInfo != null) {
            AlertDialog(
                onDismissRequest = {
                    showQrDialog = false
                    // No qrData for patient, but for consistency, you could reset any related state here if needed
                },
                title = {
                    Text("Your QR Code", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        com.example.frontend.screens.components.QRCode(
                            data = userInfo!!.id,
                            modifier = Modifier.size(240.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("QR Code to share your ID", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showQrDialog = false
                        // No qrData for patient, but for consistency, you could reset any related state here if needed
                    }) {
                        Text("CLOSE")
                    }
                }
            )
        }

        NavigationBar(
            windowInsets = WindowInsets(0.dp)
        ) {
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "My Logs",
                        modifier = Modifier.size(32.dp),
                    )
                },
                label = {
                    Text(
                        "My Logs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                selected = selectedScreen is Screen.PatientLogs,
                onClick = onPatientLogs,
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = if (selectedScreen is Screen.PatientLogs) NavigationBarSelectedColor else Color.Transparent
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "My Reminders",
                        modifier = Modifier.size(32.dp),
                    )
                },
                label = {
                    Text(
                        "Reminders",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                selected = false,
                onClick = onReminders
            )
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NavigationBarQRColor, shape = CircleShape)
                            .border(width = 2.dp, color = Color(0xFF26A69A), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Show QR Code to Share Your ID",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                label = {
                    Text(
                        "My QR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                selected = false,
                onClick = { showQrDialog = true }
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(32.dp),
                    )
                },
                label = {
                    Text(
                        "Profile",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                selected = selectedScreen is Screen.DashBoardPatient,
                onClick = {
                    onPatientProfile(true)
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = if (selectedScreen is Screen.DashBoardPatient) NavigationBarSelectedColor else Color.Transparent
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Ask AI",
                        modifier = Modifier.size(32.dp),
                    )
                },
                label = {
                    Text(
                        "Ask AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                selected = selectedScreen is Screen.Chat,
                onClick = onChatScreen,
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = if (selectedScreen is Screen.Chat) NavigationBarSelectedColor else Color.Transparent
                )
            )
        }
    }
    @Composable
    fun CaregiverNavigationBar(selectedScreen: Screen) {

        var userInfo: UserInfo? by remember { mutableStateOf(null) }
        var showQrDialog by remember { mutableStateOf(false) }
        var qrData by remember { mutableStateOf("") }

        // Always fetch userInfo once on composition
        LaunchedEffect(Unit) {
            userInfo = SelfUserInfoCache.getUserInfo()
        }

        // Only fetch OTP and QR data when dialog is shown and userInfo is available
        LaunchedEffect(showQrDialog, userInfo) {
            if (showQrDialog && userInfo != null) {
                val caregiverAuthToken = RetrofitInstance.dementiaAPI.getIdToken()
                val response = RetrofitInstance.dementiaAPI.getOtp("Bearer $caregiverAuthToken")
                val userId = userInfo?.id ?: ""
                val otp = response.body()?.otp ?: ""
                qrData = "$userId|$otp"
            }
        }

        if (showQrDialog && userInfo != null) {
            AlertDialog(
                onDismissRequest = {
                    showQrDialog = false
                    qrData = ""
                },
                title = {
                    Text("Your QR Code", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        com.example.frontend.screens.components.QRCode(
                            data = qrData,
                            modifier = Modifier.size(240.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("QR code for new patient register", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showQrDialog = false
                        qrData = ""
                    }) {
                        Text("CLOSE")
                    }
                }
            )
        }

        NavigationBar(
            windowInsets = WindowInsets(0.dp)
        ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp),
                )
            },
            label = {
                Text(
                    "Profile",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            selected = selectedScreen is Screen.DashboardCareGiver,
            onClick = {
                onCaregiverProfile(true)
            },
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                indicatorColor = if (selectedScreen is Screen.DashboardCareGiver) NavigationBarSelectedColor else Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NavigationBarQRColor, shape = CircleShape)
                        .border(width = 2.dp, color = Color(0xFF26A69A), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Show QR Code for Patient Registration",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            label = {
                Text(
                    "QR Code",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            selected = false,
            onClick = { showQrDialog = true }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Ask AI",
                    modifier = Modifier.size(32.dp),
                )
            },
            label = {
                Text(
                    "Ask AI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            selected = selectedScreen is Screen.Chat,
            onClick = onChatScreen,
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                indicatorColor = if (selectedScreen is Screen.Chat) NavigationBarSelectedColor else Color.Transparent
            )
        )
        }
    }
}