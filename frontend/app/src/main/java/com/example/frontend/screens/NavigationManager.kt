package com.example.frontend.screens

import android.annotation.SuppressLint
import androidx.navigation.NavHostController

object NavigationManager {
    @SuppressLint("StaticFieldLeak")
    private var navController: NavHostController? = null

    fun setNavController(navController: NavHostController) {
        this.navController = navController
    }

    fun getNavController(): NavHostController {
        return navController ?: throw IllegalStateException("NavController not initialized")
    }
}