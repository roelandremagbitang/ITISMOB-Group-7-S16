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

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val masterList = mutableListOf<Expense>()

    // Summary Data for Adapter
    data class CategorySummary(val name: String, val amount: Double, val type: String, val color: Int)
    private val summaryList = mutableListOf<CategorySummary>()
    private lateinit var categoryAdapter: CategorySummaryAdapter

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

        pieChart = findViewById(R.id.pieChart)
        tvDateRange = findViewById(R.id.tvCurrentDateRange)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        spinnerPeriod = findViewById(R.id.spinnerPeriodType)
        tabType = findViewById(R.id.tabType)

        val btnPrev = findViewById<ImageButton>(R.id.btnPrevPeriod)
        val btnNext = findViewById<ImageButton>(R.id.btnNextPeriod)
        val rv = findViewById<RecyclerView>(R.id.rvCategorySummary)

        // Setup Tabs (Expense / Income)
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

        // Setup Spinner (Weekly/Monthly/Yearly)
        val options = listOf("Weekly", "Monthly", "Yearly")
        spinnerPeriod.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterMode = position
                updateUI()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        rv.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategorySummaryAdapter(summaryList)
        rv.adapter = categoryAdapter

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
        pieChart.setDrawEntryLabels(false)
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.holeRadius = 35f
        pieChart.transparentCircleRadius = 35f
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

        val startTime = startCal.timeInMillis
        val endTime = endCal.timeInMillis

        var totalInc = 0.0
        var totalExp = 0.0

        // This map will store data only for the CURRENTLY selected Tab
        val activeMap = HashMap<String, Double>()

        for (item in masterList) {
            if (item.date in startTime..endTime) {
                // Calculates global totals for the top headers
                if (item.type == "income") totalInc += item.amount
                else totalExp += item.amount

                // Filters logic for Graph & List
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

        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        tvTotalIncome.text = "Inc: ${format.format(totalInc)}"
        tvTotalExpense.text = "Exp: ${format.format(totalExp)}"

        if(isExpenseView) {
            tvTotalExpense.alpha = 1.0f
            tvTotalIncome.alpha = 0.5f
        } else {
            tvTotalExpense.alpha = 0.5f
            tvTotalIncome.alpha = 1.0f
        }

        // Convert map to list and sorts first (Highest amount first)
        val sortedList = activeMap.toList().sortedByDescending { (_, value) -> value }
        val colorPool = getColors()

        // Prepares lists for Chart and Adapter
        val pieEntries = ArrayList<PieEntry>()
        val pieColors = ArrayList<Int>()
        summaryList.clear()

        val typeTag = if(isExpenseView) "expense" else "income"

        // Loops through sorted items and assign colors
        sortedList.forEachIndexed { index, (category, amount) ->
            val color = colorPool[index % colorPool.size]

            // Add to Chart Data
            pieEntries.add(PieEntry(amount.toFloat(), category))
            pieColors.add(color)

            summaryList.add(CategorySummary(category, amount, typeTag, color))
        }

        categoryAdapter.notifyDataSetChanged()

        // Updates Chart
        if (pieEntries.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = "No Data"
            pieChart.invalidate()
        } else {
            pieChart.centerText = ""

            val dataSet = PieDataSet(pieEntries, "")
            dataSet.colors = pieColors // Assign the synced colors

            dataSet.setDrawValues(false)

            val data = PieData(dataSet)
            pieChart.data = data
            pieChart.invalidate()
        }
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

        // Combine multiple templates to ensure enough unique colors
        if (isExpenseView) {
            // For Expenses: Mix Material, Joyful, and Colorful
            for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)
            for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
            for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
            for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        } else {
            // For Income: Mix Pastel and Vordiplom
            for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
            for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
            for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        }

        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }

    inner class CategorySummaryAdapter(private val list: List<CategorySummary>) :
        RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
            val tvTotal: TextView = itemView.findViewById(R.id.tvCategoryTotal)
            val indicator: View = itemView.findViewById(R.id.viewIndicator)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_summary, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name

            val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
            holder.tvTotal.text = format.format(item.amount)

            // Sets the specific color from the Chart to the indicator bar
            holder.indicator.setBackgroundColor(item.color)

            // Keeps the text color logical
            if (item.type == "income") {
                holder.tvTotal.setTextColor("#4CAF50".toColorInt())
            } else {
                holder.tvTotal.setTextColor("#FF6B6B".toColorInt())
            }
        }
        override fun getItemCount() = list.size
    }

    private fun getColors(): List<Int> {
        val colors = ArrayList<Int>()
        if (isExpenseView) {
            for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)
            for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
            for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
            for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        } else {
            for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
            for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
            for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        }
        return colors
    }
}