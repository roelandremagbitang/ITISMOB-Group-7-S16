package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditTransactionActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    // UI Elements
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var btnDiscard: Button

    // Data variables
    private var expenseId: String = ""
    private var transactionType: String = "expense" // Default
    private var currentCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        // 1. Initialize Views
        etAmount = findViewById(R.id.etEditAmount)
        spinnerCategory = findViewById(R.id.spinnerEditCategory)
        etNotes = findViewById(R.id.etEditNotes)
        btnSave = findViewById(R.id.btnSaveEdit)
        btnDelete = findViewById(R.id.btnDelete)
        btnDiscard = findViewById(R.id.btnDiscard)

        // 2. Get Data from Intent
        expenseId = intent.getStringExtra("EXPENSE_ID") ?: ""
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)
        currentCategory = intent.getStringExtra("CATEGORY") ?: ""
        val notes = intent.getStringExtra("NOTES") ?: ""
        transactionType = intent.getStringExtra("TYPE") ?: "expense"

        // 3. Populate UI
        etAmount.setText(amount.toString())
        etNotes.setText(notes)
        setupSpinner()

        // 4. Button Listeners
        btnDiscard.setOnClickListener { finish() }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        btnSave.setOnClickListener {
            updateTransaction()
        }
    }

    private fun setupSpinner() {
        // Different categories based on Income vs Expense
        val list = if (transactionType == "income") {
            listOf("Allowance", "Cash Savings", "Extra Income", "Fund Transfer", "Government Aid", "Salary", "Others", "Uncategorized", currentCategory)
        } else {
            listOf("Food", "Transport", "Utilities", "Entertainment", "Other", currentCategory)
        }
        // Use a Set to remove duplicates
        val uniqueList = list.toSet().toList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, uniqueList)
        spinnerCategory.adapter = adapter

        // Set the selected item to the one passed from the list
        val spinnerPosition = adapter.getPosition(currentCategory)
        spinnerCategory.setSelection(spinnerPosition)
    }

    private fun updateTransaction() {
        val newAmount = etAmount.text.toString().toDoubleOrNull()
        val newNotes = etNotes.text.toString()
        val newCategory = spinnerCategory.selectedItem.toString()

        if (newAmount == null || expenseId.isEmpty()) return

        // Determine collection based on type
        val collectionName = if (transactionType == "income") "income" else "expenses"

        // For Income, the field is "source", for Expense it is "category"
        val categoryField = if (transactionType == "income") "source" else "category"

        val updates = mapOf(
            "amount" to newAmount,
            "notes" to newNotes,
            categoryField to newCategory
        )

        db.collection(collectionName).document(expenseId).update(updates).addOnSuccessListener {
                Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this).setTitle("Delete Transaction").setMessage("Are you sure you want to delete this?").setPositiveButton("Yes") { _, _ ->
                val collectionName = if (transactionType == "income") "income" else "expenses"

                db.collection(collectionName).document(expenseId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }.setNegativeButton("No", null).show()
    }
}