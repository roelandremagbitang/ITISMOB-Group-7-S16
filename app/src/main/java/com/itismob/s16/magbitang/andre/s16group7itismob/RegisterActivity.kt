package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: UserDatabaseHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Helpers
        db = UserDatabaseHelper(this)
        auth = Firebase.auth

        // Bind Views (IDs from your activity_register.xml)
        val editFullname = findViewById<EditText>(R.id.editFullname)
        val editBirthday = findViewById<EditText>(R.id.editBirthday)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val editConfirm = findViewById<EditText>(R.id.editConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginRedirect = findViewById<TextView>(R.id.tvLoginRedirect)

        btnRegister.setOnClickListener {
            val name = editFullname.text.toString().trim()
            val birthday = editBirthday.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val pass = editPassword.text.toString().trim()
            val confirm = editConfirm.text.toString().trim()

            // Basic Validation
            if (name.isEmpty() || birthday.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Create User in Firebase Authentication (Cloud)
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 2. Save additional user details in SQLite (Local)
                        // We insert locally to keep your 'UserDatabaseHelper' logic relevant for offline data
                        val isInserted = db.insertUser(name, birthday, email, pass)

                        if (isInserted) {
                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to Login
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Account created, but local save failed.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Firebase registration failed
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}