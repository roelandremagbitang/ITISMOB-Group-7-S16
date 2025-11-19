package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val userEmail = intent.getStringExtra("USER_EMAIL")

        btnVerify.setOnClickListener {
            if (userEmail != null) {
                // Phase 2: Facial Recognition (2FA) Logic
                // NOTE: The actual Facial Recognition implementation (camera access,
                // processing, and API call) must be added here.

                val isFacialScanSuccessful = attemptFacialRecognition(userEmail)

                if (isFacialScanSuccessful) {
                    // Final Login Step Successful! Navigate to Dashboard.
                    Toast.makeText(this, "Verification successful! Welcome.", Toast.LENGTH_SHORT).show()

                    // Navigate to the main application screen (Dashboard)
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish() // Close the login and verification activities
                } else {
                    Toast.makeText(this, "Facial verification failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Error: User context lost. Please log in again.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    /**
     * Placeholder function for Facial Recognition logic.
     * TODO: Replace this with the actual implementation for accessing the camera
     * and communicating with your Facial Recognition API/Service.
     */
    private fun attemptFacialRecognition(email: String): Boolean {
        // For demonstration, we assume verification is always successful.
        // In a real app, this function would handle the complex biometrics logic.

        // This process provides the extra layer of security required by the proposal[cite: 39].
        return true
    }
}