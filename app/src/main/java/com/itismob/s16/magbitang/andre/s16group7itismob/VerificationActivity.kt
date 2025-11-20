package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class VerificationActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        val btnVerify = findViewById<Button>(R.id.btnVerify)

        // 1. Initialize the Executor (Runs the callback on the main thread)
        executor = ContextCompat.getMainExecutor(this)

        // 2. Define the Callback (What happens after the scan)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // SUCCESS: The user is verified!
                    Toast.makeText(applicationContext, "Identity Verified!", Toast.LENGTH_SHORT).show()

                    // Navigate to Dashboard
                    startActivity(Intent(this@VerificationActivity, DashboardActivity::class.java))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Hard failure (e.g., wrong face/fingerprint)
                    Toast.makeText(applicationContext, "Not recognized. Please try again.", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle errors (like user pressing "Back" or hardware issues)
                    // We ignore "User Canceled" to avoid spamming Toasts when they just close the dialog
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(applicationContext, "Authentication Error: $errString", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        // 3. Configure the Prompt UI
        // IMPORTANT: We use BIOMETRIC_WEAK to allow standard Face Unlock
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Facial Verification")
            .setSubtitle("Confirm your identity to access your finances")
            // Allow Face (Weak/Strong) OR PIN/Pattern (Device Credential)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            // NOTE: We DO NOT set a Negative Button (Cancel) because DEVICE_CREDENTIAL is enabled.
            .build()

        // 4. Button Click Listener
        btnVerify.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        // Optional: Trigger the scan immediately when the activity opens
        // biometricPrompt.authenticate(promptInfo)
    }
}