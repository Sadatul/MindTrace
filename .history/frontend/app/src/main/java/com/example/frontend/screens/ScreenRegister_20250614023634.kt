package com.example.frontend.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.frontend.api.RetrofitInstance

@Composable
fun ScreenRegister(
    onProceedToOtp: (String) -> Unit,
    onProceedToDashboard: (String, String, String?) -> Unit, // role, token, name
    googleToken: String? // Pass the real Google JWT token here!
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF512DA8),
                        Color(0xFF9575CD),
                        Color(0xFF03DAC6)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Mind Trace",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Dementia Caregiver App",
                fontSize = 20.sp,
                color = Color(0xFFEEEEEE),
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_mindtrace_logo),
                contentDescription = "Mind Trace Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            GoogleSignUpSection(
                onProceedToOtp = onProceedToOtp,
                onProceedToDashboard = onProceedToDashboard,
                googleToken = googleToken
            )
        }
    }
}

@Composable
private fun GoogleSignUpSection(
    onProceedToOtp: (String) -> Unit,
    onProceedToDashboard: (String, String, String?) -> Unit, // role, token, name
    googleToken: String?
) {
    var showRoleDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }
    var userRole by remember { mutableStateOf<String?>(null) }
    var showRegisterPrompt by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isLoading = true
            errorMsg = null

            val receivedToken = googleToken ?: ""
            token = receivedToken

            if (receivedToken.isBlank()) {
                isLoading = false
                errorMsg = "No Google token found. Please sign in with Google first."
                return@Button
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.dementiaAPI.postAuth("Bearer $receivedToken")
                    when (response.code()) {
                        200 -> {
                            // Existing user
                            isLoading = false
                            // If your backend returns role and name, extract them here:
                            // val role = response.body()?.role ?: "patient"
                            // val name = response.body()?.name
                            // For now, using placeholders:
                            val role = "patient" // Replace with actual role if available
                            val name = null      // Replace with actual name if available
                            userRole = role
                            userName = name
                            onProceedToDashboard(role, receivedToken, name)
                        }
                        201 -> {
                            // New user
                            isLoading = false
                            showRegisterPrompt = true
                        }
                        else -> {
                            isLoading = false
                            errorMsg = "Auth failed: ${response.code()}"
                        }
                    }
                } catch (e: Exception) {
                    isLoading = false
                    errorMsg = "Network error: ${e.localizedMessage}"
                }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF121212),
            contentColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = "Google Logo",
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isLoading) "Signing in..." else "Sign up using Google")
    }

    // Show the token and user details if available
    if (token != null) {
        Text(
            text = "Token: $token",
            color = Color.Green,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
    if (userName != null) {
        Text(
            text = "Name: $userName",
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    if (userRole != null) {
        Text(
            text = "Role: $userRole",
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    // Show error message if any
    if (errorMsg != null) {
        Text(
            text = errorMsg!!,
            color = Color.Red,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    // Show register prompt if not a registered user
    if (showRegisterPrompt) {
        AlertDialog(
            onDismissRequest = { showRegisterPrompt = false },
            title = { Text("Not Registered") },
            text = { Text("You are not a registered user. Do you want to register?") },
            confirmButton = {
                Button(onClick = {
                    showRegisterPrompt = false
                    showRoleDialog = true
                }) { Text("Register") }
            },
            dismissButton = {
                Button(onClick = { showRegisterPrompt = false }) { Text("Cancel") }
            }
        )
    }

    // Role selection dialog for new users
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Choose your role") },
            text = { Text("Are you a patient or a caregiver?") },
            confirmButton = {},
            dismissButton = {
                Row {
                    Button(onClick = {
                        showRoleDialog = false
                        onProceedToOtp("patient")
                    }) { Text("Patient") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        showRoleDialog = false
                        onProceedToOtp("caregiver")
                    }) { Text("Caregiver") }
                }
            }
        )
    }
}