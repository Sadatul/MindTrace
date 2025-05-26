package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DashboardActivity : AppCompatActivity() {
    private lateinit var userNameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        userNameTextView = findViewById(R.id.userNameTextView)
        profileImageView = findViewById(R.id.profileImageView)
        auth = Firebase.auth

        // Get current user
        val user = auth.currentUser
        user?.let { firebaseUser ->
            // Set user name
            userNameTextView.text = firebaseUser.displayName ?: "Anonymous"

            // Load profile picture
            firebaseUser.photoUrl?.let { photoUrl ->
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile)
                    .into(profileImageView)
            } ?: run {
                // Set default profile picture if no photo URL exists
                profileImageView.setImageResource(R.drawable.default_profile)
            }
        }
    }
} 