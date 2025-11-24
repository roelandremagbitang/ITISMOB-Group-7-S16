package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private val expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        // UPDATED: Now points to 'activity_item_expense' instead of 'item_expense'
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_expense, parent, false)
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
        holder.tvAmount.text = format.format(expense.amount)
    }

    override fun getItemCount() = expenseList.size
}