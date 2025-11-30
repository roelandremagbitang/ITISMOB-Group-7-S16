package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

class SavingsChallengeDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var tvName: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvPercent: TextView

    private lateinit var rvTransactions: RecyclerView
    private lateinit var transactionAdapter: SavingsTransactionAdapter
    private val transactionList = mutableListOf<SavingsTransaction>()

    private var savingsId: String = ""
    private var currentAmount: Double = 0.0
    private var goalAmount: Double = 0.0
    private var name: String = ""
    private var frequency: String = "Monthly"
    private var targetDate: Long = 0L

    private lateinit var btnDeposit: Button
    private lateinit var btnWithdraw: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private var isChallengeCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings_challenge_detail)

        tvName = findViewById(R.id.tvDetailName)
        tvProgressText = findViewById(R.id.tvDetailProgress)
        progressBar = findViewById(R.id.pbDetail)
        tvPercent = findViewById(R.id.tvDetailPercent)
        rvTransactions = findViewById(R.id.rvSavingsTransactions)

        btnDeposit = findViewById(R.id.btnDeposit)
        btnWithdraw = findViewById(R.id.btnWithdraw)
        btnEdit = findViewById(R.id.btnEditChallenge)
        btnDelete = findViewById(R.id.btnDeleteChallenge)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        loadDataFromIntent()
        updateUI() // this function handles button-locking
        setupRecyclerView()
        loadTransactionHistory()

        btnBack.setOnClickListener { finish() }

        btnDeposit.setOnClickListener { showTransactionDialog(isDeposit = true, existingTransaction = null) }
        btnWithdraw.setOnClickListener { showTransactionDialog(isDeposit = false, existingTransaction = null) }

        btnEdit.setOnClickListener {
            val intent = Intent(this, AddSavingsChallengeActivity::class.java)
            intent.putExtra("SAVINGS_ID", savingsId)
            intent.putExtra("NAME", name)
            intent.putExtra("GOAL_AMOUNT", goalAmount)
            intent.putExtra("TARGET_DATE", targetDate)
            intent.putExtra("FREQUENCY", frequency)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Challenge")
                .setMessage("Are you sure? This will delete the goal and its history.")
                .setPositiveButton("Delete") { _, _ -> deleteChallenge() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupRecyclerView() {
        rvTransactions.layoutManager = LinearLayoutManager(this)
        transactionAdapter = SavingsTransactionAdapter(transactionList) { selectedTransaction ->
            if (!isChallengeCompleted) {
                val isDeposit = selectedTransaction.type == "deposit"
                showTransactionDialog(isDeposit, selectedTransaction)
            } else {
                Toast.makeText(this, "Challenge complete. History is locked.", Toast.LENGTH_SHORT).show()
            }
        }
        rvTransactions.adapter = transactionAdapter
    }

    private fun loadTransactionHistory() {
        if (savingsId.isEmpty()) return
        db.collection("savings_challenges").document(savingsId)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    transactionList.clear()
                    for (doc in snapshots) {
                        val item = doc.toObject(SavingsTransaction::class.java)
                        item.transactionId = doc.id
                        transactionList.add(item)
                    }
                    transactionAdapter.notifyDataSetChanged()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if (savingsId.isNotEmpty()) {
            db.collection("savings_challenges").document(savingsId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    name = doc.getString("name") ?: name
                    currentAmount = doc.getDouble("currentAmount") ?: currentAmount
                    goalAmount = doc.getDouble("goalAmount") ?: goalAmount
                    frequency = doc.getString("frequency") ?: "Monthly"
                    targetDate = doc.getLong("targetDate") ?: targetDate
                    updateUI()
                }
            }
        }
    }

    private fun loadDataFromIntent() {
        savingsId = intent.getStringExtra("SAVINGS_ID") ?: ""
        name = intent.getStringExtra("NAME") ?: ""
        currentAmount = intent.getDoubleExtra("CURRENT_AMOUNT", 0.0)
        goalAmount = intent.getDoubleExtra("GOAL_AMOUNT", 0.0)
        targetDate = intent.getLongExtra("TARGET_DATE", 0L)
    }

    private fun updateUI() {
        tvName.text = name
        val currency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        currency.maximumFractionDigits = 0
        tvProgressText.text = "${currency.format(currentAmount)} / ${currency.format(goalAmount)}"

        val percentage = if (goalAmount > 0) {
            ((currentAmount / goalAmount) * 100).toInt()
        } else {
            0
        }

        progressBar.progress = percentage.coerceIn(0, 100)
        tvPercent.text = "$percentage%"

        isChallengeCompleted = currentAmount >= goalAmount
        if (isChallengeCompleted) {
            tvPercent.setTextColor("#4CAF50".toColorInt())

            // Disable Action Buttons
            btnDeposit.isEnabled = false
            btnWithdraw.isEnabled = false
            btnEdit.isEnabled = false

            btnDeposit.alpha = 0.5f
            btnWithdraw.alpha = 0.5f
            btnEdit.alpha = 0.5f

        } else {
            tvPercent.setTextColor("#6C63FF".toColorInt())

            // Enable Action Buttons
            btnDeposit.isEnabled = true
            btnWithdraw.isEnabled = true
            btnEdit.isEnabled = true

            btnDeposit.alpha = 1.0f
            btnWithdraw.alpha = 1.0f
            btnEdit.alpha = 1.0f
        }
        btnDelete.isEnabled = true
    }

    private fun showTransactionDialog(isDeposit: Boolean, existingTransaction: SavingsTransaction?) {
        val isEditMode = existingTransaction != null
        val typeLabel = if (isDeposit) "Deposit" else "Withdrawal"
        val title = if (isEditMode) "Edit $typeLabel" else "New $typeLabel"

        val dialogView = layoutInflater.inflate(R.layout.dialog_savings_transaction, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etTransAmount)
        val etNotes = dialogView.findViewById<EditText>(R.id.etTransNotes)
        val etDate = dialogView.findViewById<EditText>(R.id.etTransDate)

        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        var selectedDateTimestamp = System.currentTimeMillis()

        if (isEditMode && existingTransaction != null) {
            etAmount.setText(existingTransaction.amount.toString())
            etNotes.setText(existingTransaction.notes)
            selectedDateTimestamp = existingTransaction.date
            etDate.setText(sdf.format(Date(selectedDateTimestamp)))
        } else {
            etDate.setText(sdf.format(Date()))
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateTimestamp
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

        val builder = AlertDialog.Builder(this).setTitle(title).setView(dialogView).setPositiveButton(if (isEditMode) "Update" else "Confirm") { _, _ ->
            val amountStr = etAmount.text.toString()
            val notes = etNotes.text.toString()
            val amount = amountStr.toDoubleOrNull()

            if (amount != null && amount > 0) {
                if (isEditMode && existingTransaction != null) {
                    updateExistingTransaction(
                        existingTransaction,
                        amount,
                        selectedDateTimestamp,
                        notes
                    )
                } else {
                    createNewTransaction(amount, isDeposit, selectedDateTimestamp, notes)
                }
            } else {
                Toast.makeText(this, "Invalid Amount", Toast.LENGTH_SHORT).show()
            }
        }.setNegativeButton("Cancel", null)

        if (isEditMode && existingTransaction != null) {
            builder.setNeutralButton("Delete") { _, _ ->
                deleteTransaction(existingTransaction)
            }
        }

        builder.show()
    }

    private fun createNewTransaction(amount: Double, isDeposit: Boolean, date: Long, notes: String) {
        val newBalance = if (isDeposit) currentAmount + amount else currentAmount - amount

        if (newBalance < 0) {
            Toast.makeText(this, "Insufficient funds.", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = SavingsTransaction(
            transactionId = "",
            amount = amount,
            date = date,
            notes = notes,
            type = if (isDeposit) "deposit" else "withdrawal"
        )

        val batch = db.batch()
        val savingsRef = db.collection("savings_challenges").document(savingsId)
        val historyRef = savingsRef.collection("transactions").document()

        transaction.transactionId = historyRef.id

        batch.update(savingsRef, "currentAmount", newBalance)
        batch.set(historyRef, transaction)

        batch.commit().addOnSuccessListener {
            currentAmount = newBalance
            updateUI()
            Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error processing transaction", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateExistingTransaction(oldTrans: SavingsTransaction, newAmount: Double, newDate: Long, newNotes: String) {
        val amountAfterRevert = if (oldTrans.type == "deposit") {
            currentAmount - oldTrans.amount
        } else {
            currentAmount + oldTrans.amount
        }

        val finalAmount = if (oldTrans.type == "deposit") {
            amountAfterRevert + newAmount
        } else {
            amountAfterRevert - newAmount
        }

        if (finalAmount < 0) {
            Toast.makeText(this, "Resulting balance cannot be negative.", Toast.LENGTH_SHORT).show()
            return
        }

        val batch = db.batch()
        val savingsRef = db.collection("savings_challenges").document(savingsId)
        val transRef = savingsRef.collection("transactions").document(oldTrans.transactionId)

        batch.update(savingsRef, "currentAmount", finalAmount)
        batch.update(transRef, mapOf(
            "amount" to newAmount,
            "date" to newDate,
            "notes" to newNotes
        ))

        batch.commit().addOnSuccessListener {
            currentAmount = finalAmount
            updateUI()
            Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTransaction(transaction: SavingsTransaction) {
        val newBalance = if (transaction.type == "deposit") {
            currentAmount - transaction.amount // Remove deposit -> Balance goes down
        } else {
            currentAmount + transaction.amount // Remove withdrawal -> Balance goes up
        }

        val batch = db.batch()
        val savingsRef = db.collection("savings_challenges").document(savingsId)
        val transRef = savingsRef.collection("transactions").document(transaction.transactionId)

        batch.update(savingsRef, "currentAmount", newBalance)
        batch.delete(transRef)

        batch.commit().addOnSuccessListener {
            currentAmount = newBalance
            updateUI()
            Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteChallenge() {
        db.collection("savings_challenges").document(savingsId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Challenge deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}