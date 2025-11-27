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

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)

        val btnGraphs = findViewById<ImageButton>(R.id.btnGraphs)
        val btnBudget = findViewById<ImageButton>(R.id.btnTool)
        val btnViewAll = findViewById<Button>(R.id.btnViewAll)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddExpense)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(expenseList) { selectedExpense ->
            val intent = Intent(this, EditTransactionActivity::class.java)

            intent.putExtra("EXPENSE_ID", selectedExpense.expenseId)
            intent.putExtra("AMOUNT", selectedExpense.amount)
            intent.putExtra("CATEGORY", selectedExpense.category)
            intent.putExtra("NOTES", selectedExpense.notes)
            intent.putExtra("DATE", selectedExpense.date)
            intent.putExtra("TYPE", selectedExpense.type)

            startActivity(intent)
        }
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

        btnGraphs.setOnClickListener { startActivity(Intent(this, SummaryActivity::class.java)) }
        btnBudget.setOnClickListener { startActivity(Intent(this, ToolActivity::class.java)) }
        fabAdd.setOnClickListener { showAddOptionsDialog() }
        ivProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        btnViewAll.setOnClickListener { startActivity(Intent(this, TransactionListActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }
    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        // Temporary list to hold merged data
        val tempTransactionList = mutableListOf<Expense>()
        var totalIncome = 0.0
        var totalExpense = 0.0

        // Fetch Expenses
        db.collection("expenses").whereEqualTo("userId", userId).get().addOnSuccessListener { expenseDocs ->
            for (doc in expenseDocs) {
                val item = doc.toObject(Expense::class.java)
                item.expenseId = doc.id

                val fixedItem = item.copy(type = "expense")
                tempTransactionList.add(fixedItem)
                totalExpense += fixedItem.amount
            }

            // Fetch Income
            db.collection("income").whereEqualTo("userId", userId).get().addOnSuccessListener { incomeDocs ->
                for (doc in incomeDocs) {
                    val source = doc.getString("source") ?: "Income"
                    val item = doc.toObject(Expense::class.java)

                    item.expenseId = doc.id

                    val fixedItem = item.copy(type = "income", category = source)
                    tempTransactionList.add(fixedItem)
                    totalIncome += fixedItem.amount
                }

                // Sort by date descending (newest first)
                tempTransactionList.sortByDescending { it.date }

                // Update the Main List used by the Adapter
                expenseList.clear()
                expenseList.addAll(tempTransactionList.take(5))
                adapter.notifyDataSetChanged()

                // Update Total Balance UI (Income - Expense)
                val balance = totalIncome - totalExpense
                val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                tvTotalExpense.text = format.format(balance)
            }
        }.addOnFailureListener {
            tvTotalExpense.text = "Error"
            tvTotalExpense.text = "Error"
        }
    }

    // Function for the small prompt
    private fun showAddOptionsDialog() {
        val options = arrayOf("Add Expense", "Add Income")

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("What do you want to add?")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // User clicked "Add Expense"
                    startActivity(Intent(this, AddExpenseActivity::class.java))
                }
                1 -> {
                    // User clicked "Add Income"
                    startActivity(Intent(this, AddIncomeActivity::class.java))
                }
            }
        }
        builder.show()
    }
}