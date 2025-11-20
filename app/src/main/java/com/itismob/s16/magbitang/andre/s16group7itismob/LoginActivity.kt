package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Bind Views (IDs from your activity_login.xml)
        val editEmail = findViewById<EditText>(R.id.inputEmail)
        val editPassword = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val linkRegister = findViewById<TextView>(R.id.linkRegister)

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Verify Password with Firebase (Factor 1)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 2. Password Correct -> Proceed to Facial Recognition (2FA)
                        Toast.makeText(this, "Password Verified. Starting 2FA...", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, VerificationActivity::class.java)
                        intent.putExtra("USER_EMAIL", email) // Pass email if needed for context
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Authentication Failed: Invalid email or password.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        linkRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}