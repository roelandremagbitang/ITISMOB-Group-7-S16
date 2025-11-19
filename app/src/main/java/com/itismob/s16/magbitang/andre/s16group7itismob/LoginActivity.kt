package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = UserDatabaseHelper(this)

        // CORRECTED IDs to match activity_login.xml
        val editEmail = findViewById<EditText>(R.id.inputEmail)
        val editPassword = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val registerText = findViewById<TextView>(R.id.linkRegister) // Corrected ID

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Phase 1: Email and Password Authentication
            if (db.validateLogin(email, password)) {

                // Login successful! Now enforce Facial Recognition (2FA)[cite: 42].
                Toast.makeText(this, "Password correct. Proceeding to 2FA...", Toast.LENGTH_SHORT).show()

                // CORRECTED NAVIGATION: Go to VerificationActivity for 2FA
                val intent = Intent(this, VerificationActivity::class.java).apply {
                    // Pass the user's email to the next activity for context
                    putExtra("USER_EMAIL", email)
                }
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}