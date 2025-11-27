package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddSavingsChallengeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerFreq: Spinner
    private lateinit var etDate: EditText
    private lateinit var tvSummary: TextView
    private lateinit var btnSave: Button

    private var selectedDateTimestamp: Long = 0L
    private var calculatedAmountPerFreq: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_savings_challenge)

        etName = findViewById(R.id.etChallengeName)
        etAmount = findViewById(R.id.etTargetAmount)
        spinnerFreq = findViewById(R.id.spinnerFrequency)
        etDate = findViewById(R.id.etTargetDate)
        tvSummary = findViewById(R.id.tvCalculatedSummary)
        btnSave = findViewById(R.id.btnSaveChallenge)

        // Setup Spinner
        val freqs = listOf("Daily", "Weekly", "Monthly")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, freqs)
        spinnerFreq.adapter = adapter

        // Setup Date Picker
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                selectedDateTimestamp = calendar.timeInMillis
                etDate.setText(sdf.format(calendar.time))
                calculateProjection()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Add Listeners for real-time calculation
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = calculateProjection()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spinnerFreq.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) = calculateProjection()
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btnSave.setOnClickListener { saveChallenge() }
    }

    private fun calculateProjection() {
        val amountStr = etAmount.text.toString()
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val freq = spinnerFreq.selectedItem?.toString() ?: "Monthly"

        if (amount <= 0 || selectedDateTimestamp <= System.currentTimeMillis()) {
            tvSummary.text = "Please enter valid amount and future date."
            return
        }

        val today = System.currentTimeMillis()
        val diffInMillis = selectedDateTimestamp - today
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis).toDouble()

        // Avoid division by zero
        val safeDays = if (days < 1) 1.0 else days

        val intervals = when (freq) {
            "Daily" -> safeDays
            "Weekly" -> safeDays / 7.0
            "Monthly" -> safeDays / 30.44 // Average days in month
            else -> 1.0
        }

        // Determine cost
        val safeIntervals = if (intervals < 1.0) 1.0 else intervals
        calculatedAmountPerFreq = amount / safeIntervals

        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        tvSummary.text = "You need to save ${format.format(calculatedAmountPerFreq)} $freq to reach your goal by the target date."
    }

    private fun saveChallenge() {
        val name = etName.text.toString().trim()
        val amount = etAmount.text.toString().toDoubleOrNull()

        if (name.isEmpty() || amount == null || selectedDateTimestamp == 0L) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        val challenge = SavingsChallenge(
            userId = userId,
            name = name,
            currentAmount = 0.0,
            goalAmount = amount,
            frequency = spinnerFreq.selectedItem?.toString() ?: "Monthly",
            targetDate = selectedDateTimestamp,
            amountPerFrequency = calculatedAmountPerFreq
        )

        db.collection("savings_challenges").add(challenge).addOnSuccessListener { doc ->
            doc.update("savingsId", doc.id)

            Toast.makeText(this, "Challenge Started!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}