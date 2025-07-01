package com.example.frontend.screens

import androidx.compose.runtime.*
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getPartners
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.models.PartnerInfo
import com.example.frontend.screens.components.ScreenMyPartners
import kotlinx.coroutines.launch

@Composable
fun ScreenMyCaregivers(
    onNavigateBack: () -> Unit
) {
    var currentUser: UserInfo? by remember { mutableStateOf(null) }
    var caregivers by remember { mutableStateOf(listOf<PartnerInfo>()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeletedPartners by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        currentUser = RetrofitInstance.dementiaAPI.getSelfUserInfo()
    }

    fun loadCaregivers(includeDeleted: Boolean) {
        scope.launch {
            isLoading = true
            try {
                caregivers = RetrofitInstance.dementiaAPI.getPartners(includeDeleted)
            } catch (_: Exception) {
                // Handle error - could show snackbar or error state
            } finally {
                isLoading = false
            }
        }
    }

    // Load caregivers when user is loaded or toggle changes
    LaunchedEffect(currentUser, showDeletedPartners) {
        if (currentUser != null) {
            loadCaregivers(includeDeleted = showDeletedPartners)
        }
    }

    currentUser?.let { user ->
        ScreenMyPartners(
            currentUser = user,
            partners = caregivers,
            role = "PATIENT", // Pass as string for patient viewing caregivers
            onNavigateBack = onNavigateBack,
            isLoading = isLoading,
            showDeletedPartners = showDeletedPartners,
            onToggleDeleted = { showDeletedPartners = !showDeletedPartners }
        )
    }
}
