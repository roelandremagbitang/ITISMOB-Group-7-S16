package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import androidx.core.graphics.toColorInt

class SummaryActivity : AppCompatActivity() {

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Data
    private val masterList = mutableListOf<Expense>()

    // Summary Data for Adapter
    data class CategorySummary(val name: String, val amount: Double, val type: String)
    private val summaryList = mutableListOf<CategorySummary>()
    private lateinit var categoryAdapter: CategorySummaryAdapter

    // UI
    private lateinit var pieChart: PieChart
    private lateinit var tvDateRange: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var spinnerPeriod: Spinner
    private lateinit var tabType: TabLayout

    // Filter Logic
    private var filterMode = 0 // 0=Weekly, 1=Monthly, 2=Yearly
    private var selectedDate = Calendar.getInstance()
    private var isExpenseView = true // Default to Expense View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // 1. Initialize Views
        pieChart = findViewById(R.id.pieChart)
        tvDateRange = findViewById(R.id.tvCurrentDateRange)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        spinnerPeriod = findViewById(R.id.spinnerPeriodType)
        tabType = findViewById(R.id.tabType)

        val btnPrev = findViewById<ImageButton>(R.id.btnPrevPeriod)
        val btnNext = findViewById<ImageButton>(R.id.btnNextPeriod)
        val rv = findViewById<RecyclerView>(R.id.rvCategorySummary)

        // 2. Setup Tabs (Expense / Income)
        tabType.addTab(tabType.newTab().setText("Expenses"))
        tabType.addTab(tabType.newTab().setText("Income"))

        tabType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // If tab index is 0, it's Expenses. If 1, it's Income.
                isExpenseView = (tab?.position == 0)
                updateUI()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 3. Setup Spinner (Weekly/Monthly/Yearly)
        val options = listOf("Weekly", "Monthly", "Yearly")
        spinnerPeriod.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterMode = position
                updateUI()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 4. Setup RecyclerView
        rv.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategorySummaryAdapter(summaryList)
        rv.adapter = categoryAdapter

        // 5. Setup Buttons
        btnPrev.setOnClickListener { changeDate(-1) }
        btnNext.setOnClickListener { changeDate(1) }

        setupChart()
        loadData()
    }

    private fun changeDate(amount: Int) {
        when (filterMode) {
            0 -> selectedDate.add(Calendar.WEEK_OF_YEAR, amount)
            1 -> selectedDate.add(Calendar.MONTH, amount)
            2 -> selectedDate.add(Calendar.YEAR, amount)
        }
        updateUI()
    }

    private fun setupChart() {
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(10f)
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.animateY(800)
    }

    private fun loadData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("expenses").whereEqualTo("userId", userId).get().addOnSuccessListener { expDocs ->
            masterList.clear()
            for (doc in expDocs) {
                val item = doc.toObject(Expense::class.java)
                masterList.add(item.copy(type = "expense"))
            }

            db.collection("income").whereEqualTo("userId", userId).get().addOnSuccessListener { incDocs ->
                for (doc in incDocs) {
                    val source = doc.getString("source") ?: "Income"
                    val item = doc.toObject(Expense::class.java)
                    masterList.add(item.copy(type = "income", category = source))
                }
                updateUI()
            }
        }
    }

    private fun updateUI() {
        // --- 1. SET DATE LABELS (Same as before) ---
        val startCal = selectedDate.clone() as Calendar
        val endCal = selectedDate.clone() as Calendar
        val sdfWeek = SimpleDateFormat("MMM dd", Locale.US)
        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.US)
        val sdfYear = SimpleDateFormat("yyyy", Locale.US)

        when (filterMode) {
            0 -> { // Weekly
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                setStartOfDay(startCal)
                endCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                endCal.add(Calendar.DAY_OF_WEEK, 6)
                setEndOfDay(endCal)
                tvDateRange.text = "${sdfWeek.format(startCal.time)} - ${sdfWeek.format(endCal.time)}"
            }
            1 -> { // Monthly
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                setStartOfDay(startCal)
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                setEndOfDay(endCal)
                tvDateRange.text = sdfMonth.format(startCal.time)
            }
            2 -> { // Yearly
                startCal.set(Calendar.DAY_OF_YEAR, 1)
                setStartOfDay(startCal)
                endCal.set(Calendar.MONTH, 11)
                endCal.set(Calendar.DAY_OF_MONTH, 31)
                setEndOfDay(endCal)
                tvDateRange.text = sdfYear.format(startCal.time)
            }
        }

        // --- 2. FILTER DATA ---
        val startTime = startCal.timeInMillis
        val endTime = endCal.timeInMillis

        var totalInc = 0.0
        var totalExp = 0.0

        // This map will store data only for the CURRENTLY selected Tab
        val activeMap = HashMap<String, Double>()

        for (item in masterList) {
            if (item.date in startTime..endTime) {
                // Calculate global totals for the top headers
                if (item.type == "income") totalInc += item.amount
                else totalExp += item.amount

                // Filter logic for Graph & List
                if (isExpenseView && item.type == "expense") {
                    val current = activeMap[item.category] ?: 0.0
                    activeMap[item.category] = current + item.amount
                }
                else if (!isExpenseView && item.type == "income") {
                    val current = activeMap[item.category] ?: 0.0
                    activeMap[item.category] = current + item.amount
                }
            }
        }

        // --- 3. UPDATE TOTALS ---
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        tvTotalIncome.text = "Inc: ${format.format(totalInc)}"
        tvTotalExpense.text = "Exp: ${format.format(totalExp)}"

        // Highlight the active total
        if(isExpenseView) {
            tvTotalExpense.alpha = 1.0f
            tvTotalIncome.alpha = 0.5f
        } else {
            tvTotalExpense.alpha = 0.5f
            tvTotalIncome.alpha = 1.0f
        }

        // --- 4. UPDATE GRAPH (Active Map Only) ---
        updatePieChart(activeMap)

        // --- 5. UPDATE LIST (Active Map Only) ---
        summaryList.clear()
        val typeTag = if(isExpenseView) "expense" else "income"

        for ((name, amount) in activeMap) {
            summaryList.add(CategorySummary(name, amount, typeTag))
        }

        summaryList.sortByDescending { it.amount }
        categoryAdapter.notifyDataSetChanged()
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
    }

    private fun updatePieChart(dataMap: Map<String, Double>) {
        val entries = ArrayList<PieEntry>()
        for ((key, value) in dataMap) {
            entries.add(PieEntry(value.toFloat(), key))
        }

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = "No Data"
            pieChart.invalidate()
            return
        } else {
            pieChart.centerText = ""
        }

        val dataSet = PieDataSet(entries, "")
        val colors = ArrayList<Int>()

        // FIX: Combine multiple templates to ensure enough unique colors
        if (isExpenseView) {
            // For Expenses: Mix Material, Joyful, and Colorful
            for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)
            for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
            for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
            for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        } else {
            // For Income: Mix Pastel and Vordiplom (lighter/softer themes)
            for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
            for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
            // Reuse others if really needed to prevent looping
            for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        }

        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }

    // --- REUSE PREVIOUS ADAPTER ---
    inner class CategorySummaryAdapter(private val list: List<CategorySummary>) :
        RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
            val tvTotal: TextView = itemView.findViewById(R.id.tvCategoryTotal)
            val indicator: View = itemView.findViewById(R.id.viewIndicator)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_summary, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name

            val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
            holder.tvTotal.text = format.format(item.amount)

            if (item.type == "income") {
                holder.indicator.setBackgroundColor("#4CAF50".toColorInt())
                holder.tvTotal.setTextColor("#4CAF50".toColorInt())
            } else {
                holder.indicator.setBackgroundColor("#FF6B6B".toColorInt())
                holder.tvTotal.setTextColor("#FF6B6B".toColorInt())
            }
        }

        override fun getItemCount() = list.size
    }
}