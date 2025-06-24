package com.example.frontend.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.screens.components.PartnerInfo
import com.example.frontend.screens.components.ScreenMyPartners
import kotlinx.coroutines.launch

@Composable
fun ScreenMyPatients(
    onNavigateBack: () -> Unit,
    onAddPatient: () -> Unit = {}
) {
    var currentUser: UserInfo? by remember { mutableStateOf(null) }
    var patients by remember { mutableStateOf(listOf<PartnerInfo>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                currentUser = RetrofitInstance.dementiaAPI.getSelfUserInfo()
                // TODO: Load patients from API
                // For now, using mock data
                patients = loadMockPatients()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    currentUser?.let { user ->
        ScreenMyPartners(
            currentUser = user,
            partners = patients,
            isPatientView = false, // Caregiver viewing patients
            onNavigateBack = onNavigateBack,
            onAddPartner = onAddPatient,
            isLoading = isLoading
        )
    }
}

// Mock data - replace with actual API call
private fun loadMockPatients(): List<PartnerInfo> {
    return listOf(
        PartnerInfo(
            id = "PT001",
            name = "John Williams",
            email = "john.williams@email.com",
            gender = "M",
            profilePicture = null,
            role = "PATIENT",
            joinedDate = "2024-01-10"
        ),
        PartnerInfo(
            id = "PT002",
            name = "Mary Davis",
            email = "mary.davis@email.com",
            gender = "F",
            profilePicture = null,
            role = "PATIENT",
            joinedDate = "2024-03-05"
        )
    )
}
