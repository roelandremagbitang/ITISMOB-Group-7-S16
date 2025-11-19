package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = UserDatabaseHelper(this)

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

            if (name.isEmpty() || birthday.isEmpty() || email.isEmpty() ||
                pass.isEmpty() || confirm.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.emailExists(email)) {
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inserted = db.insertUser(name, birthday, email, pass)

            if (inserted) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to register user", Toast.LENGTH_SHORT).show()
            }
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
