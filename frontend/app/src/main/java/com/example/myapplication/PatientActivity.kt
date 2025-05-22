package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PatientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient)

        // Find views by their IDs
        val userIdInput: EditText = findViewById(R.id.userIdInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val loginButton: Button = findViewById(R.id.loginButton)

        // Login Button logic
        loginButton.setOnClickListener {
            val userId = userIdInput.text.toString()
            val password = passwordInput.text.toString()

            // Simple validation (replace with your own logic for login validation)
            if (userId.isNotEmpty() && password.isNotEmpty()) {
                // Here you can add your logic to authenticate the user,
                // such as checking credentials from a database, etc.

                // Show success message
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // After successful login, navigate to another activity
                val intent = Intent(this, PatientDashboardActivity::class.java)
                startActivity(intent)
            } else {
                // Show error message if fields are empty
                Toast.makeText(this, "Please enter both User ID and Password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
