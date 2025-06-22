package com.example.frontend.screens

import androidx.compose.runtime.*
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getPartners
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.models.PartnerInfo
import com.example.frontend.screens.components.PartnerViewDialog
import com.example.frontend.screens.components.ScreenMyPartners
import kotlinx.coroutines.launch

@Composable
fun ScreenMyPatients(
    onNavigateBack: () -> Unit,
    onAddPatient: () -> Unit = {}
) {
    var currentUser: UserInfo? by remember { mutableStateOf(null) }
    var patients by remember { mutableStateOf(listOf<PartnerInfo>()) }
    var isLoading by remember { mutableStateOf(false) }
    var showViewDialog by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        currentUser = RetrofitInstance.dementiaAPI.getSelfUserInfo()
    }

    fun loadPatients(includeDeleted: Boolean) {
        scope.launch {
            isLoading = true
            try {
                patients = RetrofitInstance.dementiaAPI.getPartners(includeDeleted)
            } catch (_: Exception) {
                // Handle error - could show snackbar or error state
            } finally {
                isLoading = false
            }
        }
    }

    // Only load patients after user is loaded and dialog is handled
    LaunchedEffect(currentUser, showViewDialog) {
        if (currentUser != null && !showViewDialog) {
            loadPatients(includeDeleted = false)
        }
    }

    currentUser?.let { user ->
        ScreenMyPartners(
            currentUser = user,
            partners = patients,
            role = PartnerScreenRole.Caregiver, // Caregiver viewing patients
            onNavigateBack = onNavigateBack,
            onAddPartner = onAddPatient,
            isLoading = isLoading
        )
    }

    // Show dialog for choosing view type
    if (showViewDialog) {
        PartnerViewDialog(
            onDismiss = {
                showViewDialog = false
                onNavigateBack() // Go back if dismissed
            },
            onViewActiveOnly = {
                showViewDialog = false
                loadPatients(includeDeleted = false)
            },
            onViewIncludingDeleted = {
                showViewDialog = false
                loadPatients(includeDeleted = true)
            },
            partnerType = "Patients"
        )
    }
}
