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
fun ScreenMyCaregivers(
    onNavigateBack: () -> Unit,
    onAddCaregiver: () -> Unit = {}
) {
    var currentUser: UserInfo? by remember { mutableStateOf(null) }
    var caregivers by remember { mutableStateOf(listOf<PartnerInfo>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                currentUser = RetrofitInstance.dementiaAPI.getSelfUserInfo()
                // TODO: Load caregivers from API
                // For now, using mock data
                caregivers = loadMockCaregivers()
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
            partners = caregivers,
            isPatientView = true, // Patient viewing caregivers
            onNavigateBack = onNavigateBack,
            onAddPartner = onAddCaregiver,
            isLoading = isLoading
        )
    }
}

// Mock data - replace with actual API call
private fun loadMockCaregivers(): List<PartnerInfo> {
    return listOf(
        PartnerInfo(
            id = "CG001",
            name = "Dr. Sarah Johnson",
            email = "sarah.johnson@hospital.com",
            gender = "F",
            profilePicture = null,
            role = "CAREGIVER",
            joinedDate = "2024-01-15"
        ),
        PartnerInfo(
            id = "CG002",
            name = "Mike Thompson",
            email = "mike.thompson@care.com",
            gender = "M",
            profilePicture = null,
            role = "CAREGIVER",
            joinedDate = "2024-02-20"
        )
    )
}
