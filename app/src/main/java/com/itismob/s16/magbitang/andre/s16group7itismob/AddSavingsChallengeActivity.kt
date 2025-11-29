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

    private lateinit var tvTitle: TextView
    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerFreq: Spinner
    private lateinit var etDate: EditText
    private lateinit var tvSummary: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var selectedDateTimestamp: Long = 0L
    private var calculatedAmountPerFreq: Double = 0.0

    private var isEditMode = false
    private var savingsId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_savings_challenge)

        tvTitle = findViewById(R.id.tvAddTitle)
        etName = findViewById(R.id.etChallengeName)
        etAmount = findViewById(R.id.etTargetAmount)
        spinnerFreq = findViewById(R.id.spinnerFrequency)
        etDate = findViewById(R.id.etTargetDate)
        tvSummary = findViewById(R.id.tvCalculatedSummary)
        btnSave = findViewById(R.id.btnSaveChallenge)
        btnCancel = findViewById(R.id.btnCancelSavings)

        setupSpinner()
        setupDatePicker()
        checkForEditIntent() // Check if we are editing or adding
        setupListeners()

        btnSave.setOnClickListener { saveChallenge() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun setupSpinner() {
        val freqs = listOf("Daily", "Weekly", "Monthly")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, freqs)
        spinnerFreq.adapter = adapter
    }

    private fun setupDatePicker() {
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (selectedDateTimestamp != 0L) {
                calendar.timeInMillis = selectedDateTimestamp
            }

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    selectedDateTimestamp = calendar.timeInMillis
                    etDate.setText(sdf.format(calendar.time))
                    calculateProjection()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }
    }

    private fun checkForEditIntent() {
        if (intent.hasExtra("SAVINGS_ID")) {
            isEditMode = true
            savingsId = intent.getStringExtra("SAVINGS_ID")

            // Update UI Labels
            tvTitle.text = "Edit Savings Goal"
            btnSave.text = "Update Challenge"

            // Populate Fields
            etName.setText(intent.getStringExtra("NAME"))

            val goalAmount = intent.getDoubleExtra("GOAL_AMOUNT", 0.0)
            etAmount.setText(goalAmount.toString())

            selectedDateTimestamp = intent.getLongExtra("TARGET_DATE", 0L)
            if (selectedDateTimestamp != 0L) {
                val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                etDate.setText(sdf.format(Date(selectedDateTimestamp)))
            }

            // Set Spinner Selection
            val freq = intent.getStringExtra("FREQUENCY") ?: "Monthly"
            val adapter = spinnerFreq.adapter as ArrayAdapter<String>
            val position = adapter.getPosition(freq)
            if (position >= 0) {
                spinnerFreq.setSelection(position)
            }

            // Force calculation update
            calculateProjection()
        }
    }

    private fun setupListeners() {
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = calculateProjection()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spinnerFreq.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) = calculateProjection()
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun calculateProjection() {
        val amountStr = etAmount.text.toString()
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val freq = spinnerFreq.selectedItem?.toString() ?: "Monthly"

        if (amount <= 0 || selectedDateTimestamp <= System.currentTimeMillis()) {
            if (amountStr.isNotEmpty()) {
                tvSummary.text = "Please enter valid amount and future date."
            }
            return
        }

        val today = System.currentTimeMillis()
        val diffInMillis = selectedDateTimestamp - today
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis).toDouble()

        val safeDays = if (days < 1) 1.0 else days
        val intervals = when (freq) {
            "Daily" -> safeDays
            "Weekly" -> safeDays / 7.0
            "Monthly" -> safeDays / 30.44
            else -> 1.0
        }

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
        val frequency = spinnerFreq.selectedItem?.toString() ?: "Monthly"

        if (isEditMode && savingsId != null) {
            val updates = mapOf(
                "name" to name,
                "goalAmount" to amount,
                "frequency" to frequency,
                "targetDate" to selectedDateTimestamp,
                "amountPerFrequency" to calculatedAmountPerFreq
            )

            db.collection("savings_challenges").document(savingsId!!).update(updates).addOnSuccessListener {
                Toast.makeText(this, "Challenge Updated!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        } else {
            val challenge = SavingsChallenge(
                userId = userId,
                name = name,
                currentAmount = 0.0,
                goalAmount = amount,
                frequency = frequency,
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
}