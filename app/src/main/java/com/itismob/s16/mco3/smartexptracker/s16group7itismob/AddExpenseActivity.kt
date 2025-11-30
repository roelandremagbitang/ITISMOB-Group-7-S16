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

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDate: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

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

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        etDate.setText(sdf.format(Date(selectedDateTimestamp)))

        etDate.setOnClickListener { showDatePicker() }

        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        // --- CHECK FOR SCANNER OR BUDGET DATA ---
        val categoryFromBudget = intent.getStringExtra("CATEGORY_FROM_BUDGET")
        if (categoryFromBudget != null) {
            if (!categoryList.any { it.equals(categoryFromBudget, ignoreCase = true) }) {
                categoryList.add(0, categoryFromBudget) // Add to the top of the list
                categoryAdapter.notifyDataSetChanged()
            }
            val position = categoryList.indexOfFirst { it.equals(categoryFromBudget, ignoreCase = true) }
            if (position >= 0) {
                spinnerCategory.setSelection(position)
            }
        } else {
            if (intent.hasExtra("AMOUNT_FROM_SCAN")) {
                val scannedAmount = intent.getDoubleExtra("AMOUNT_FROM_SCAN", 0.0)
                if (scannedAmount > 0) etAmount.setText(scannedAmount.toString())
            }
            if (intent.hasExtra("NOTES_FROM_SCAN")) {
                val scannedNotes = intent.getStringExtra("NOTES_FROM_SCAN")
                etNotes.setText(scannedNotes)
            }
            if (intent.hasExtra("CATEGORY_FROM_SCAN")) {
                val scannedCat = intent.getStringExtra("CATEGORY_FROM_SCAN")
                val position = categoryList.indexOfFirst { it.equals(scannedCat, ignoreCase = true) }
                if (position >= 0) {
                    spinnerCategory.setSelection(position)
                }
            }
        }
        // -------------------------------------

        btnSave.setOnClickListener { saveExpense() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            selectedDateTimestamp = calendar.timeInMillis
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            etDate.setText(sdf.format(calendar.time))
        }

        DatePickerDialog(
            this, dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveExpense() {
        val amountStr = etAmount.text.toString().trim()
        val notes = etNotes.text.toString().trim()
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

        db.collection("expenses").add(expenseData).addOnSuccessListener { documentReference ->
            documentReference.update("expenseId", documentReference.id)
            Toast.makeText(this, "Expense Saved!", Toast.LENGTH_LONG).show()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
