package com.example.frontend.screens.models

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val showRegisterPrompt: Boolean = false,
    val showRoleDialog: Boolean = false,
    val showCaregiverForm: Boolean = false,
    val showPatientForm: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val selectedRole: String? = null,
    val caregiverFormData: CaregiverFormData = CaregiverFormData(),
    val patientFormData: PatientFormData = PatientFormData(),
    val firebaseCredentials: FirebaseCredentials? = null,
    val isRegistered: Boolean = false
)
