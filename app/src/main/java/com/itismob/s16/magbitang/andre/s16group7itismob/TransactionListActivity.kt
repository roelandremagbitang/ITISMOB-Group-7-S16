package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val masterList = mutableListOf<Expense>() // Holds EVERYTHING
    private val displayedList = mutableListOf<Expense>() // Holds filtered data for RecyclerView
    private lateinit var adapter: ExpenseAdapter

    private lateinit var spinnerFilter: Spinner
    private lateinit var tvDateRange: TextView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    // Filters
    private var currentTabPosition = 0 // 0=All, 1=Income, 2=Expense
    private var currentSearchQuery = ""

    private var filterMode = "Daily" // Daily, Weekly, Monthly, Yearly, All Time
    private val selectedDate = Calendar.getInstance() // Holds the current anchor date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)

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

        // Initializes Date Controls
        spinnerFilter = findViewById(R.id.spinnerTimeFilter)
        tvDateRange = findViewById(R.id.tvDateRange)
        btnPrev = findViewById(R.id.btnPrevDate)
        btnNext = findViewById(R.id.btnNextDate)

        setupDateSpinner()
        setupDateNavigation()

        // Setup Tabs
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

        // Setup Search
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

        loadAllTransactions()
    }

    private fun loadAllTransactions() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("expenses").whereEqualTo("userId", userId).get().addOnSuccessListener { expenseDocs ->
            masterList.clear()
            for (doc in expenseDocs) {
                val item = doc.toObject(Expense::class.java)
                item.expenseId = doc.id
                masterList.add(item.copy(type = "expense"))
            }

            db.collection("income").whereEqualTo("userId", userId).get().addOnSuccessListener { incomeDocs ->
                for (doc in incomeDocs) {
                    val source = doc.getString("source") ?: "Income"
                    val item = doc.toObject(Expense::class.java)
                    item.expenseId = doc.id
                    masterList.add(item.copy(type = "income", category = source))
                }
                masterList.sortByDescending { it.date }

                updateDateLabel()
                filterData()
            }
        }
    }

    private fun filterData() {
        displayedList.clear()

        // Calculate Start and End timestamps based on selection
        val (startTime, endTime) = getStartAndEndTimes()

        for (item in masterList) {
            val matchesDate = if (filterMode == "All Time") {
                true
            } else {
                item.date >= startTime && item.date <= endTime
            }

            // Tab Check (Income vs Expense)
            val matchesTab = when (currentTabPosition) {
                0 -> true
                1 -> item.type == "income"
                2 -> item.type == "expense"
                else -> true
            }

            // Search Check
            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                item.category.contains(currentSearchQuery, ignoreCase = true) ||
                        item.notes.contains(currentSearchQuery, ignoreCase = true)
            }

            if (matchesDate && matchesTab && matchesSearch) {
                displayedList.add(item)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun setupDateSpinner() {
        val options = listOf("Daily", "Weekly", "Monthly", "Yearly", "All Time")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinnerFilter.adapter = adapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterMode = options[position]

                // Reset to today whenever mode changes
                selectedDate.timeInMillis = System.currentTimeMillis()

                updateDateLabel()
                filterData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDateNavigation() {
        btnPrev.setOnClickListener {
            changeDate(-1)
        }
        btnNext.setOnClickListener {
            changeDate(1)
        }
    }

    private fun changeDate(amount: Int) {
        when (filterMode) {
            "Daily" -> selectedDate.add(Calendar.DAY_OF_YEAR, amount)
            "Weekly" -> selectedDate.add(Calendar.WEEK_OF_YEAR, amount)
            "Monthly" -> selectedDate.add(Calendar.MONTH, amount)
            "Yearly" -> selectedDate.add(Calendar.YEAR, amount)
        }
        updateDateLabel()
        filterData()
    }

    private fun updateDateLabel() {
        val sdfDay = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.US)
        val sdfYear = SimpleDateFormat("yyyy", Locale.US)

        tvDateRange.text = when (filterMode) {
            "Daily" -> sdfDay.format(selectedDate.time)
            "Monthly" -> sdfMonth.format(selectedDate.time)
            "Yearly" -> sdfYear.format(selectedDate.time)
            "Weekly" -> {
                // Calculate start and end of week
                val startOfWeek = selectedDate.clone() as Calendar
                startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)

                val endOfWeek = startOfWeek.clone() as Calendar
                endOfWeek.add(Calendar.DAY_OF_WEEK, 6)

                val shortFormat = SimpleDateFormat("MMM dd", Locale.US)
                "${shortFormat.format(startOfWeek.time)} - ${shortFormat.format(endOfWeek.time)}"
            }
            else -> "All Transactions"
        }
    }

    private fun getStartAndEndTimes(): Pair<Long, Long> {
        val startCal = selectedDate.clone() as Calendar
        val endCal = selectedDate.clone() as Calendar

        when (filterMode) {
            "Daily" -> {
                setStartOfDay(startCal)
                setEndOfDay(endCal)
            }
            "Weekly" -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                setStartOfDay(startCal)

                endCal.set(Calendar.DAY_OF_WEEK, endCal.firstDayOfWeek)
                endCal.add(Calendar.DAY_OF_WEEK, 6)
                setEndOfDay(endCal)
            }
            "Monthly" -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                setStartOfDay(startCal)

                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                setEndOfDay(endCal)
            }
            "Yearly" -> {
                startCal.set(Calendar.DAY_OF_YEAR, 1)
                setStartOfDay(startCal)

                endCal.set(Calendar.MONTH, 11)
                endCal.set(Calendar.DAY_OF_MONTH, 31)
                setEndOfDay(endCal)
            }
        }
        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }

    private fun setStartOfDay(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }

    private fun setEndOfDay(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
    }
}