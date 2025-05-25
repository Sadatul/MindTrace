package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignupPatientActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_patient)

        val fullNameField = findViewById<EditText>(R.id.fullName)
        val emailField = findViewById<EditText>(R.id.userIdInput)
        val phoneField = findViewById<EditText>(R.id.phoneNumber)
        val nidField = findViewById<EditText>(R.id.nidNumber)
        val passwordField = findViewById<EditText>(R.id.passwordInput)
        val reenterPasswordField = findViewById<EditText>(R.id.reenterPasswordInput)
        val termsCheckbox = findViewById<CheckBox>(R.id.terms_checkbox)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val loginButton = findViewById<TextView>(R.id.login_button)

        signupButton.setOnClickListener {
            if (validateInputs(fullNameField, emailField, phoneField, nidField, passwordField, reenterPasswordField, termsCheckbox)) {
                Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()
                // Proceed with signup logic
            }
        }

        loginButton.setOnClickListener {
            // Navigate to patient login page
            val intent = Intent(this, LoginPatientActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInputs(
        fullNameField: EditText,
        emailField: EditText,
        phoneField: EditText,
        nidField: EditText,
        passwordField: EditText,
        reenterPasswordField: EditText,
        termsCheckbox: CheckBox
    ): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(fullNameField.text.toString().trim())) {
            fullNameField.error = "Full Name is required"
            isValid = false
        }

        if (TextUtils.isEmpty(emailField.text.toString().trim()) ||
            !Patterns.EMAIL_ADDRESS.matcher(emailField.text.toString().trim()).matches()
        ) {
            emailField.error = "Enter a valid email address"
            isValid = false
        }

        if (TextUtils.isEmpty(phoneField.text.toString().trim()) ||
            !Patterns.PHONE.matcher(phoneField.text.toString().trim()).matches()
        ) {
            phoneField.error = "Enter a valid phone number"
            isValid = false
        }

        if (TextUtils.isEmpty(nidField.text.toString().trim())) {
            nidField.error = "NID Number is required"
            isValid = false
        }

        val password = passwordField.text.toString()
        val rePassword = reenterPasswordField.text.toString()
        if (password.length < 6) {
            passwordField.error = "Password must be at least 6 characters"
            isValid = false
        }
        if (password != rePassword) {
            reenterPasswordField.error = "Passwords do not match"
            isValid = false
        }

        if (!termsCheckbox.isChecked) {
            termsCheckbox.error = "You must agree to the Terms and Conditions"
            isValid = false
        } else {
            termsCheckbox.error = null
        }

        return isValid
    }
}