package com.example.frontend.screens.components

import androidx.compose.runtime.Composable

@Composable
fun CaregiverRegisterDialog(
    name: String,
    email: String,
    dob: String,
    gender: String,
    profilePictureUrl: String?,
    onDobChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    RegisterDialog(
        title = "Caregiver Registration",
        name = name,
        email = email,
        dob = dob,
        gender = gender,
        profilePictureUrl = profilePictureUrl,
        additionalFields = {
            // No additional fields for caregiver
        },
        infoMessage = "Complete your caregiver profile by providing your date of birth and gender information.",
        onDobChange = onDobChange,
        onGenderChange = onGenderChange,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}