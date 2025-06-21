package com.example.frontend.screens.models

data class PatientFormData(
    val name: String = "",
    val email: String = "",
    val dob: String = "",
    val gender: String = "",
    val primaryContact: String = "",
    val otp: String = ""
)
