package com.example.frontend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.frontend.api.AuthSession
import com.example.frontend.api.PatientRegisterRequest
import com.example.frontend.api.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun ScreenPatient(onNavigateToChat: () -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    var patientInfo by remember { mutableStateOf<PatientRegisterRequest?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val token = AuthSession.token

    // Fetch patient info from backend if not already loaded
    LaunchedEffect(token) {
        if (token != null && patientInfo == null) {
            coroutineScope.launch {
                isLoading = true
                try {
                    // You should have a getPatientDetails API, but as a placeholder, show only AuthSession details
                    // val response = RetrofitInstance.dementiaAPI.getPatientDetails("Bearer $token")
                    // if (response.isSuccessful) patientInfo = response.body()
                    // else errorMsg = "Failed to load patient details"
                } catch (e: Exception) {
                    errorMsg = "Failed to load patient info: ${e.localizedMessage}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Patient Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Token: ${token ?: "<none>"}", style = MaterialTheme.typography.bodyMedium)
                Text("User Type: ${AuthSession.userType ?: "<none>"}", style = MaterialTheme.typography.bodyMedium)
                // Add more fields here if you fetch patientInfo from backend
                // Example:
                // Text("Name: ${patientInfo?.name ?: "<unknown>"}")
                // Text("DOB: ${patientInfo?.dob ?: "<unknown>"}")
                // Text("Gender: ${patientInfo?.gender ?: "<unknown>"}")
                // Text("Caregiver Contact: ${patientInfo?.caregiverPrimaryContact ?: "<unknown>"}")
                // Text("Caregiver OTP: ${patientInfo?.caregiverOTP ?: "<unknown>"}")
            }
        }
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNavigateToChat, modifier = Modifier.fillMaxWidth()) {
            Text("Help")
        }
    }
}