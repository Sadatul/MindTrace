package com.example.frontend.api

object SelfUserInfoCache {
    private var userInfo: UserInfo? = null

    fun getUserInfo(): UserInfo? {
        return userInfo
    }

    fun setUserInfo(userInfo: UserInfo) {
        this.userInfo = userInfo
    }

    fun signOutUser() {
        userInfo = null
    }
}Use