package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDate: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    // Data
    private var selectedDateTimestamp: Long = System.currentTimeMillis()
    private val categoryList = mutableListOf("Food", "Transport", "Utilities", "Entertainment", "Others")
    private lateinit var categoryAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etDate = findViewById(R.id.etDate)
        etNotes = findViewById(R.id.etNotes)
        btnSave = findViewById(R.id.btnSaveExpense)
        btnCancel = findViewById(R.id.btnCancelExpense)

        // Setup Spinner
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)
        spinnerCategory.adapter = categoryAdapter

        loadCategoriesFromFirebase()

        // Setup Date Picker
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        etDate.setText(sdf.format(Date())) // Set today as default

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDateTimestamp = calendar.timeInMillis
                    etDate.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSave.setOnClickListener { saveExpense() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun loadCategoriesFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("categories").get().addOnSuccessListener { result ->
            if (!result.isEmpty) {
                val customCategories = result.mapNotNull { it.getString("name") }
                if (customCategories.isNotEmpty()) {
                    categoryList.clear()
                    categoryList.addAll(customCategories)
                    // Ensure we always have a fallback if user deleted all categories
                    if (categoryList.isEmpty()) categoryList.add("Uncategorized")
                    categoryAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun saveExpense() {
        // Debug Toast
        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show()

        val amountStr = etAmount.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        // Safe check for spinner selection
        val category = spinnerCategory.selectedItem?.toString() ?: "Uncategorized"

        if (amountStr.isEmpty()) {
            etAmount.error = "Amount is required"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            etAmount.error = "Invalid amount"
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_LONG).show()
            return
        }

        val expenseData = hashMapOf(
            "userId" to userId,
            "amount" to amount,
            "category" to category,
            "date" to selectedDateTimestamp,
            "notes" to notes,
            "expenseId" to ""
        )

        // Save to Firestore
        db.collection("expenses").add(expenseData).addOnSuccessListener { documentReference ->
            documentReference.update("expenseId", documentReference.id)
            Toast.makeText(this, "Expense Saved Successfully!", Toast.LENGTH_LONG).show()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}