package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalExpense: TextView
    private lateinit var rvRecentTransactions: RecyclerView
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val expenseList = mutableListOf<Expense>()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Views
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)

        val btnGraphs = findViewById<ImageButton>(R.id.btnGraphs)
        val btnBudget = findViewById<ImageButton>(R.id.btnBudget)
        val btnViewAll = findViewById<Button>(R.id.btnViewAll)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddExpense)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        // Setup RecyclerView
        rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(expenseList)
        rvRecentTransactions.adapter = adapter

        // Load User Name
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("fullName")
                        tvGreeting.text = "Hello, ${name ?: "User"}!"
                    }
                }
        }

        // Navigation
        btnGraphs.setOnClickListener { startActivity(Intent(this, SummaryActivity::class.java)) }
        btnBudget.setOnClickListener { startActivity(Intent(this, BudgetBuilderActivity::class.java)) }
        fabAdd.setOnClickListener { startActivity(Intent(this, AddExpenseActivity::class.java)) }
        ivProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        btnViewAll.setOnClickListener { startActivity(Intent(this, ExpenseListActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        // 1. Calculate Total & Fetch Recent 3 Items
        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                var total = 0.0
                expenseList.clear()

                for ((index, doc) in documents.withIndex()) {
                    // Calculate Total
                    val amount = doc.getDouble("amount") ?: 0.0
                    total += amount

                    // Add only the first 3 items to the list
                    if (index < 3) {
                        val expense = doc.toObject(Expense::class.java)
                        expenseList.add(expense)
                    }
                }

                // Update Total UI
                val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                tvTotalExpense.text = format.format(total)

                // Update List UI
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                tvTotalExpense.text = "₱0.00"
            }
    }
}