package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R.*

class LoginPatientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.login_patient)

        val userEmailInput: EditText = findViewById(id.username_email)
        val passwordInput: EditText = findViewById(id.password)
        val logInButton: Button = findViewById(id.login_button)
        val rememberMeCheckbox: CheckBox = findViewById(id.remember_me_checkbox)
        val forgotPassword: TextView = findViewById(id.forgot_password)
        val signUp: TextView = findViewById(id.sign_up)

        logInButton.setOnClickListener {
            val userEmail = userEmailInput.text.toString()
            val password = passwordInput.text.toString()

            if (userEmail.isNotEmpty() && password.isNotEmpty()) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (password.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (userEmail == "test@patient.com" && password == "password123") {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DashboardPatientActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid credentials, please try again", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        forgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }

        signUp.setOnClickListener {
            val intent = Intent(this, SignupPatientActivity::class.java)
            startActivity(intent)
        }
    }
}