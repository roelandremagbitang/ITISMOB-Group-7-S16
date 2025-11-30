package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import java.util.Calendar
import java.util.Date

class BudgetBuilderActivity : AppCompatActivity() {

    private lateinit var etCategory: EditText
    private lateinit var etLimit: EditText
    private lateinit var spinnerPeriod: Spinner
    private lateinit var btnSave: Button
    private lateinit var rvBudgets: RecyclerView
    private lateinit var btnBack: ImageButton

    private lateinit var budgetAdapter: BudgetAdapter
    private val budgetListWithProgress = mutableListOf<BudgetWithProgress>()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val NOTIFICATION_CHANNEL_ID = "budget_alerts"
    private val notifiedBudgets = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_builder)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        etCategory = findViewById(R.id.etCategory)
        etLimit = findViewById(R.id.etLimit)
        spinnerPeriod = findViewById(R.id.spinnerPeriod)
        btnSave = findViewById(R.id.btnSave)
        rvBudgets = findViewById(R.id.rvBudgets)
        btnBack = findViewById(R.id.btnBack)

        // Setup Spinner
        val periodOptions = resources.getStringArray(R.array.period_options).filter { it == "Weekly" || it == "Monthly" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeriod.adapter = adapter

        // Setup RecyclerView
        rvBudgets.layoutManager = LinearLayoutManager(this)
        budgetAdapter = BudgetAdapter(budgetListWithProgress)
        rvBudgets.adapter = budgetAdapter

        // Set Listeners
        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveBudget() }

        // Create notification channel
        createNotificationChannel()

        // Load data
        loadBudgetsAndExpenses()
    }

    private fun saveBudget() {
        val category = etCategory.text.toString().trim()
        val limitStr = etLimit.text.toString().trim()
        val period = spinnerPeriod.selectedItem.toString()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty() || limitStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val limit = limitStr.toDoubleOrNull()
        if (limit == null || limit <= 0) {
            Toast.makeText(this, "Please enter a valid limit.", Toast.LENGTH_SHORT).show()
            return
        }

        val budget = Budget(
            userId = userId,
            category = category,
            limit = limit,
            period = period
        )

        db.collection("budgets")
            .add(budget)
            .addOnSuccessListener {
                Toast.makeText(this, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
                etCategory.text.clear()
                etLimit.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving budget: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBudgetsAndExpenses() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("budgets").whereEqualTo("userId", userId).addSnapshotListener { budgetSnapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (budgetSnapshots != null) {
                val budgets = budgetSnapshots.toObjects<Budget>()

                db.collection("expenses").whereEqualTo("userId", userId).addSnapshotListener { expenseSnapshots, e2 ->
                    if (e2 != null) {
                        return@addSnapshotListener
                    }

                    if (expenseSnapshots != null) {
                        val expenses = expenseSnapshots.toObjects<Expense>()
                        updateBudgetProgress(budgets, expenses)
                    }
                }
            }
        }
    }

    private fun updateBudgetProgress(budgets: List<Budget>, expenses: List<Expense>) {
        val newBudgetList = mutableListOf<BudgetWithProgress>()
        val now = Calendar.getInstance()

        for (budget in budgets) {
            val (start, end) = getPeriodDateRange(budget.period, now)
            val totalSpent = expenses.filter { exp ->
                val expenseDate = Calendar.getInstance().apply { timeInMillis = exp.date }
                exp.category == budget.category && expenseDate.after(start) && expenseDate.before(end)
            }.sumOf { it.amount }

            newBudgetList.add(BudgetWithProgress(budget, totalSpent))

            if (totalSpent > budget.limit) {
                 if (!notifiedBudgets.contains(budget.id)) {
                    sendOverspendingNotification(budget)
                    notifiedBudgets.add(budget.id)
                }
            } else {
                notifiedBudgets.remove(budget.id)
            }
        }

        budgetListWithProgress.clear()
        budgetListWithProgress.addAll(newBudgetList)
        budgetAdapter.notifyDataSetChanged()
    }

    private fun getPeriodDateRange(period: String, now: Calendar): Pair<Calendar, Calendar> {
        val start = now.clone() as Calendar
        val end = now.clone() as Calendar

        when (period) {
            "Weekly" -> {
                start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
                // If today is before the first day of the week (e.g., Sunday), rewind one week.
                if (now.get(Calendar.DAY_OF_WEEK) < start.firstDayOfWeek) {
                    start.add(Calendar.WEEK_OF_YEAR, -1)
                }
                end.time = start.time
                end.add(Calendar.DAY_OF_WEEK, 6)
            }
            "Monthly" -> {
                start.set(Calendar.DAY_OF_MONTH, 1)
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
        }

        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0)
        end.set(Calendar.HOUR_OF_DAY, 23); end.set(Calendar.MINUTE, 59); end.set(Calendar.SECOND, 59)

        return Pair(start, end)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val descriptionText = "Notifications for when you overspend on a budget."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendOverspendingNotification(budget: Budget) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = budget.id.hashCode()

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Budget Alert: Overspending")
            .setContentText("You have overspent on your '${budget.category}' budget.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(notificationId, builder.build())
    }
}