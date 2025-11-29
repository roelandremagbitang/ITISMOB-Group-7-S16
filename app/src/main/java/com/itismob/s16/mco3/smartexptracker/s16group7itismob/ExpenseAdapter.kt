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
import androidx.core.graphics.toColorInt

class ExpenseAdapter(private val expenseList: List<Expense>, private val onItemClick: (Expense) -> Unit) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]

        holder.tvCategory.text = expense.category

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