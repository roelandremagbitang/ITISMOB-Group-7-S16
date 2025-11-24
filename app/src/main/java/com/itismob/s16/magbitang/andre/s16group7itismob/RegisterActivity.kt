package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var editBirthday: EditText // Retained for date display

    // Variable to store the selected birthday (as a String for display)
    private var selectedBirthday: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        db = Firebase.firestore

        val editFullname = findViewById<EditText>(R.id.editFullname)
        editBirthday = findViewById<EditText>(R.id.editBirthday) // Initialize here
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val editConfirm = findViewById<EditText>(R.id.editConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginRedirect = findViewById<TextView>(R.id.tvLoginRedirect)

        // --- NEW: Date Picker Setup ---
        // 1. Make the EditText non-editable and set up the click listener
        editBirthday.isFocusable = false
        editBirthday.isCursorVisible = false
        editBirthday.setOnClickListener {
            showDatePicker()
        }
        // --- END NEW ---

        btnRegister.setOnClickListener {
            val name = editFullname.text.toString().trim()
            val birthday = editBirthday.text.toString().trim() // Now retrieves the formatted date
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val confirm = editConfirm.text.toString().trim()

            // Basic Validation
            if (name.isEmpty() || birthday.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Create User in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        // Data to save to Firestore
                        val userMap = hashMapOf(
                            "fullName" to name,
                            "birthday" to birthday, // Storing the formatted date string (e.g., 20/09/1999)
                            "email" to email
                        )

                        // 2. Save Data to Firestore
                        if (userId != null) {
                            db.collection("users").document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registration Successful! Logging in...", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // --- NEW: Date Picker Function ---
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Check if a date has already been picked and set the calendar to that date
        if (selectedBirthday.isNotEmpty()) {
            try {
                // Use the same date format as the output to parse the current date
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                calendar.time = dateFormat.parse(selectedBirthday) ?: Date()
            } catch (e: Exception) {
                // Ignore parsing errors and default to today
            }
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Format the date to display (e.g., 20/09/1999)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            selectedBirthday = dateFormat.format(calendar.time)

            // Update the EditText field
            editBirthday.setText(selectedBirthday)
        }

        // Create the DatePickerDialog
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}