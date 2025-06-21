package com.example.frontend.screens.models

data class FirebaseCredentials(
    val userUID: String,
    val displayName: String?,
    val email: String?,
    val idToken: String,
    val photoUrl: String?
)
