package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay for 2 seconds to show branding, then check session
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if a user is already signed in with Firebase
            val user = Firebase.auth.currentUser
            if (user != null) {
                // User is logged in, navigate to Dashboard directly
                // (Or VerificationActivity if you want to force 2FA on every app launch)
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                // No user session, navigate to Login screen
                startActivity(Intent(this, LoginActivity::class.java))
            }
            // Close SplashActivity so user can't go back to it
            finish()
        }, 2000)
    }
}