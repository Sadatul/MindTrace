package com.example.frontend.api

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object SelfUserInfoCache {
    private var userInfo: UserInfo? = null

    fun getUserInfo(): UserInfo? {
        return userInfo
    }

    fun setUserInfo(userInfo: UserInfo) {
        // If the backend UserInfo has no profile picture, use the Google profile picture from Firebase
        val enhancedUserInfo = if (userInfo.profilePicture.isNullOrEmpty()) {
            val firebaseUser = Firebase.auth.currentUser
            val googleProfilePicture = firebaseUser?.photoUrl?.toString()
            
            if (!googleProfilePicture.isNullOrEmpty()) {
                userInfo.copy(profilePicture = googleProfilePicture)
            } else {
                userInfo
            }
        } else {
            userInfo
        }
        
        this.userInfo = enhancedUserInfo
    }

    fun signOutUser() {
        userInfo = null
    }
}