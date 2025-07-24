package com.example.frontend.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.frontend.api.SelfUserInfoCache
import com.example.frontend.api.UserInfo

data class NavigationBarComponent(
    val onPatientLogs: () -> Unit,
    val onReminders: () -> Unit,
    val onPatientProfile: (isPatient: Boolean) -> Unit,
    val onChatScreen: () -> Unit,
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
                onDismissRequest = { showQrDialog = false },
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
                    TextButton(onClick = { showQrDialog = false }) {
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
                        contentDescription = "My Logs"
                    )
                },
                label = { Text("Logs", fontSize = 10.sp) },
                selected = selectedScreen is Screen.PatientLogs,
                onClick = onPatientLogs
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "My Reminders"
                    )
                },
                label = { Text("Reminders", fontSize = 10.sp) },
                selected = false,
                onClick = onReminders
            )
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = { Text("QR", fontSize = 10.sp) },
                selected = false,
                onClick = { showQrDialog = true },
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile"
                    )
                },
                label = { Text("Profile", fontSize = 10.sp) },
                selected = selectedScreen is Screen.DashBoardPatient,
                onClick = {
                    onPatientProfile(true)
                }
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Ask AI"
                    )
                },
                label = { Text("AI", fontSize = 10.sp) },
                selected = selectedScreen is Screen.Chat,
                onClick = onChatScreen
            )
        }
    }
}