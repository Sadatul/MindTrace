package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find views by their IDs
        val loginButton: Button = findViewById(R.id.loginButton)
        val signupButton: Button = findViewById(R.id.signupButton)
        val caregiverCheckBox: CheckBox = findViewById(R.id.caregiverCheckBox)
        val patientCheckBox: CheckBox = findViewById(R.id.patientCheckBox)

        // Login Button logic
        loginButton.setOnClickListener {
            // Show the CheckBoxes when Login button is clicked
            caregiverCheckBox.visibility = View.VISIBLE
            patientCheckBox.visibility = View.VISIBLE
        }

        // Caregiver CheckBox logic
        caregiverCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Navigate to the Caregiver Activity when Caregiver is selected
                val intent = Intent(this, CaregiverActivity::class.java)
                startActivity(intent)
            }
        }

        // Patient CheckBox logic
        patientCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Navigate to the Patient Activity when Patient is selected
                val intent = Intent(this, PatientActivity::class.java)
                startActivity(intent)
            }
        }

        // Sign Up Button logic
        signupButton.setOnClickListener {
            // Perform sign-up logic here
            Toast.makeText(this, "Sign Up clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
