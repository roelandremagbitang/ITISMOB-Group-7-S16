package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransactionListActivity : AppCompatActivity() {

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Data Lists
    private val masterList = mutableListOf<Expense>() // Holds EVERYTHING
    private val displayedList = mutableListOf<Expense>() // Holds filtered data for RecyclerView
    private lateinit var adapter: ExpenseAdapter

    // Filters
    private var currentTabPosition = 0 // 0=All, 1=Income, 2=Expense
    private var currentSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)

        // 1. Setup RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvAllExpenses)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(displayedList) { selectedExpense ->
            val intent = Intent(this, EditTransactionActivity::class.java)

            // Pass all data to the next screen
            intent.putExtra("EXPENSE_ID", selectedExpense.expenseId)
            intent.putExtra("AMOUNT", selectedExpense.amount)
            intent.putExtra("CATEGORY", selectedExpense.category)
            intent.putExtra("NOTES", selectedExpense.notes)
            intent.putExtra("DATE", selectedExpense.date)
            intent.putExtra("TYPE", selectedExpense.type) // Crucial for knowing which DB collection to edit

            startActivity(intent)
        }

        rv.adapter = adapter

        // 2. Setup Tabs (All, Income, Expense)
        val tabLayout = findViewById<TabLayout>(R.id.tabCategories)
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Income"))
        tabLayout.addTab(tabLayout.newTab().setText("Expense"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabPosition = tab?.position ?: 0
                filterData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 3. Setup Search
        val searchView = findViewById<SearchView>(R.id.searchExpenses)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query ?: ""
                filterData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                filterData()
                return true
            }
        })

        // 4. Load Data
        loadAllTransactions()
    }

    private fun loadAllTransactions() {
        val userId = auth.currentUser?.uid ?: return

        // Step A: Fetch Expenses
        db.collection("expenses").whereEqualTo("userId", userId).get().addOnSuccessListener { expenseDocs ->
            masterList.clear()

            for (doc in expenseDocs) {
                val item = doc.toObject(Expense::class.java)
                item.expenseId = doc.id // <--- FIX ADDED

                val fixedItem = item.copy(type = "expense")
                masterList.add(fixedItem)
            }

            // Step B: Fetch Income
            db.collection("income").whereEqualTo("userId", userId).get().addOnSuccessListener { incomeDocs ->
                for (doc in incomeDocs) {
                    val source = doc.getString("source") ?: "Income"
                    val item = doc.toObject(Expense::class.java)
                    item.expenseId = doc.id // <--- FIX ADDED

                    val fixedItem = item.copy(type = "income", category = source)
                    masterList.add(fixedItem)
                }

                        // Step C: Sort by Date (Newest first)
                        masterList.sortByDescending { it.date }

                        // Step D: Show initial data
                        filterData()
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterData() {
        displayedList.clear()

        for (item in masterList) {
            // 1. Check Tab Filter
            val matchesTab = when (currentTabPosition) {
                0 -> true // All
                1 -> item.type == "income"
                2 -> item.type == "expense"
                else -> true
            }

            // 2. Check Search Filter (Category or Notes)
            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                item.category.contains(currentSearchQuery, ignoreCase = true) ||
                        item.notes.contains(currentSearchQuery, ignoreCase = true)
            }

            if (matchesTab && matchesSearch) {
                displayedList.add(item)
            }
        }

        adapter.notifyDataSetChanged()
    }
}