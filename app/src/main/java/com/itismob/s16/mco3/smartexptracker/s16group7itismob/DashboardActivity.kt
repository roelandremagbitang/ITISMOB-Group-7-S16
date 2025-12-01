package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalExpense: TextView
    private lateinit var tvGreeting: TextView // Global variable to update it later
    private lateinit var rvRecentTransactions: RecyclerView

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val expenseList = mutableListOf<Expense>()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvGreeting = findViewById(R.id.tvGreeting)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)

        val btnGraphs = findViewById<ImageButton>(R.id.btnGraphs)
        val btnTool = findViewById<ImageButton>(R.id.btnTool)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddExpense)

        rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(expenseList) { selectedExpense ->
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("EXPENSE_ID", selectedExpense.expenseId)
            intent.putExtra("AMOUNT", selectedExpense.amount)
            intent.putExtra("CATEGORY", selectedExpense.category)
            intent.putExtra("NOTES", selectedExpense.notes)
            intent.putExtra("TYPE", selectedExpense.type)
            startActivity(intent)
        }
        rvRecentTransactions.adapter = adapter

        btnGraphs.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }

        btnTool.setOnClickListener {
            startActivity(Intent(this, ToolActivity::class.java))
        }

        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        fabAdd.setOnClickListener {
            showAddOptionsDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
        loadUserProfile() // <--- NEW: Forces name refresh every time you enter
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            // 1. Force reload from server to get the latest Display Name
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updatedUser = auth.currentUser
                    val name = updatedUser?.displayName

                    if (!name.isNullOrEmpty()) {
                        // Success: Found name in Auth
                        tvGreeting.text = "Hello, $name!"
                    } else {
                        // 2. Fallback: Fetch from Firestore "users" collection
                        db.collection("users").document(updatedUser!!.uid).get()
                            .addOnSuccessListener { document ->
                                val fsName = document.getString("fullname")
                                if (!fsName.isNullOrEmpty()) {
                                    tvGreeting.text = "Hello, $fsName!"
                                } else {
                                    tvGreeting.text = "Hello, $fsName!"
                                }
                            }
                    }
                }
            }
        }
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { expenseResult ->
                val tempTransactionList = mutableListOf<Expense>()
                var totalExpense = 0.0

                for (document in expenseResult) {
                    val expense = document.toObject(Expense::class.java)
                    if (expense.expenseId.isEmpty()) expense.expenseId = document.id

                    tempTransactionList.add(expense)
                    totalExpense += expense.amount
                }

                tempTransactionList.sortByDescending { it.date }

                expenseList.clear()
                expenseList.addAll(tempTransactionList.take(5))
                adapter.notifyDataSetChanged()

                val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                tvTotalExpense.text = format.format(totalExpense)

            }.addOnFailureListener {
                tvTotalExpense.text = "Error"
            }
    }

    private fun showAddOptionsDialog() {
        val options = arrayOf("Add Expense", "Add Income", "Scan QR or Receipt")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("What do you want to add?")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> startActivity(Intent(this, AddExpenseActivity::class.java))
                1 -> startActivity(Intent(this, AddIncomeActivity::class.java))
                2 -> startActivity(Intent(this, ScanActivity::class.java))
            }
        }
        builder.show()
    }
}
