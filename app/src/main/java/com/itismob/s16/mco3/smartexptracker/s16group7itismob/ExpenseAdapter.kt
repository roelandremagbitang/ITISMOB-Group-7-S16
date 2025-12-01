package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.toColorInt

class ExpenseAdapter(private val expenseList: List<Expense>, private val onItemClick: (Expense) -> Unit) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    // Define a map to store category colors
    private val categoryColors = mutableMapOf<String, Int>()
    // Define a list of predefined colors
    private val colors = listOf("#FFC107", "#FF5722", "#4CAF50", "#2196F3", "#9C27B0", "#F44336", "#00BCD4", "#FF9800")

    private fun getCategoryColor(category: String): Int {
        return categoryColors.getOrPut(category) {
            // Assign a new color if the category is not in the map
            colors[categoryColors.size % colors.size].toColorInt()
        }
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val dot: View = itemView.findViewById(R.id.dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]

        holder.tvCategory.text = expense.category

        // Set the dot color
        val color = getCategoryColor(expense.category)
        (holder.dot.background as GradientDrawable).setColor(color)

        // Format Date
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        holder.tvDate.text = sdf.format(Date(expense.date))

        // Format Amount (Philippines Peso)
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        val amountString = format.format(expense.amount)

        // Changes color based on type
        if (expense.type == "income") {
            holder.tvAmount.text = "+ $amountString"
            holder.tvAmount.setTextColor("#4CAF50".toColorInt())
        } else {
            holder.tvAmount.text = "- $amountString"
            holder.tvAmount.setTextColor("#FF6B6B".toColorInt())
        }

        holder.itemView.setOnClickListener {
            onItemClick(expense)
        }
    }

    override fun getItemCount() = expenseList.size
}
