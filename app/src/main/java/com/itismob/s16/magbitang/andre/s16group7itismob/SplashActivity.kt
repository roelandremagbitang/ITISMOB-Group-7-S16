package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay the routing for a few seconds to show the splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            // TODO: Step 1: Check for an existing session or stored login token here.
            val isLoggedIn = checkIfUserIsLoggedIn()

            if (isLoggedIn) {
                // If logged in, go straight to the main app dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                // If not logged in, go to the login screen
                startActivity(Intent(this, LoginActivity::class.java))
            }

            // Finish the splash activity so the user cannot press back to it
            finish()

        }, 2000) // 2000 milliseconds (2 seconds) delay
    }

    /**
     * Placeholder for checking if a user has a valid, stored session.
     * TODO: Implement logic to check SharedPreferences or secure storage for a token.
     */
    private fun checkIfUserIsLoggedIn(): Boolean {
        // For development, we assume the user must always log in first.
        return false
    }
}