package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddIncomeActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var etAmount: EditText
    private lateinit var spinnerSource: Spinner
    private lateinit var etDate: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    // Data
    private var selectedDateTimestamp: Long = System.currentTimeMillis()
    // Default Income Sources
    private val sourceList = listOf("Allowance", "Cash Savings", "Extra Income", "Fund Transfer", "Government Aid",
        "Salary", "Others", "Uncategorized")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        etAmount = findViewById(R.id.etIncomeAmount)
        spinnerSource = findViewById(R.id.spinnerIncomeSource)
        etDate = findViewById(R.id.etIncomeDate)
        etNotes = findViewById(R.id.etIncomeNotes)
        btnSave = findViewById(R.id.btnSaveIncome)
        btnCancel = findViewById(R.id.btnCancelIncome)

        // Setup Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sourceList)
        spinnerSource.adapter = adapter

        // Setup Date Picker
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        etDate.setText(sdf.format(Date())) // Default to today

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

        btnSave.setOnClickListener { saveIncome() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun saveIncome() {
        val amountStr = etAmount.text.toString().trim()
        val notes = etNotes.text.toString().trim()
        val source = spinnerSource.selectedItem?.toString() ?: "Other"

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
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        // Create Income Data Object
        val incomeData = hashMapOf(
            "userId" to userId,
            "amount" to amount,
            "source" to source,
            "date" to selectedDateTimestamp,
            "notes" to notes,
            "type" to "income" // Useful tag if you ever merge collections later
        )

        // SAVE TO FIREBASE "income" COLLECTION
        db.collection("income").add(incomeData).addOnSuccessListener { documentReference ->
                //Update the document to include its own ID immediately
                documentReference.update("expenseId", documentReference.id)

                Toast.makeText(this, "Income Added Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}