package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class HomePageActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var caregiverCheckBox: CheckBox
    private lateinit var patientCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        // Initialize the views here after setContentView()
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
        caregiverCheckBox = findViewById(R.id.caregiverCheckBox)
        patientCheckBox = findViewById(R.id.patientCheckBox)

        // Login Button logic
        loginButton.setOnClickListener {
            // Display role selection and checkboxes
            caregiverCheckBox.visibility = View.VISIBLE
            patientCheckBox.visibility = View.VISIBLE

            // Make the sign-up button less visible
            signupButton.alpha = 0.5f  // Reduces opacity of the sign-up button
            loginButton.alpha = 1f  // Reset opacity of the login button

            // Clear any previous selections
            caregiverCheckBox.isChecked = false
            patientCheckBox.isChecked = false

            // Handle user selection for login
            caregiverCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Show confirmation dialog for Caregiver login
                    showRoleSelectionDialog("Login", "Caregiver")
                }
            }

            patientCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Show confirmation dialog for Patient login
                    showRoleSelectionDialog("Login", "Patient")
                }
            }
        }

        // Sign Up Button logic
        signupButton.setOnClickListener {
            // Display role selection and checkboxes
            caregiverCheckBox.visibility = View.VISIBLE
            patientCheckBox.visibility = View.VISIBLE

            // Make the login button less visible
            loginButton.alpha = 0.5f  // Reduces opacity of the login button
            signupButton.alpha = 1f  // Reset opacity of the sign-up button

            // Clear any previous selections
            caregiverCheckBox.isChecked = false
            patientCheckBox.isChecked = false

            // Handle user selection for sign up
            caregiverCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Show confirmation dialog for Caregiver sign up
                    showRoleSelectionDialog("Sign Up", "Caregiver")
                }
            }

            patientCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Show confirmation dialog for Patient sign up
                    showRoleSelectionDialog("Sign Up", "Patient")
                }
            }
        }

        val testApiButton : Button = findViewById(R.id.test_api_button)
        testApiButton.setOnClickListener {
            println("API Hit will go here")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = URL("http://192.168.43.169:8080/v1/public/health")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val inputStream = connection.inputStream
                    val response = inputStream.bufferedReader().use { it.readText() }

                    // Switch to main thread to update UI
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomePageActivity, response, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e("API_ERROR", e.toString())
                }
            }

        }


    }

    // Function to show the Alert Dialog for Login or Sign Up confirmation
    private fun showRoleSelectionDialog(action: String, role: String) {
        val message = "Are you sure you want to $action as $role?"
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Confirm") { dialog, _ ->
                // Proceed based on Login or Sign Up action and role
                if (action == "Login") {
                    if (role == "Caregiver") {
                        val intent = Intent(this, LoginCaregiverActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, LoginPatientActivity::class.java)
                        startActivity(intent)
                    }
                } else if (action == "Sign Up") {
                    if (role == "Caregiver") {
                        val intent = Intent(this, SignupCaregiverActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, SignupPatientActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Reset everything to the home page state
                caregiverCheckBox.visibility = View.GONE
                patientCheckBox.visibility = View.GONE

                // Reset button opacity
                signupButton.alpha = 1f  // Reset opacity of the sign-up button
                loginButton.alpha = 1f  // Reset opacity of the login button

                // Show a message
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()

                // Dismiss the dialog, but do NOT finish the activity
                dialog.dismiss()
            }

        // Create and show the AlertDialog
        val alert = builder.create()
        alert.show()
    }
}
