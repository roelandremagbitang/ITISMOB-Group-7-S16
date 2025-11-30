package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

data class BudgetWithProgress(val budget: Budget, val spent: Double)

class BudgetAdapter(private val budgetsWithProgress: MutableList<BudgetWithProgress>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budgetWithProgress = budgetsWithProgress[position]
        holder.bind(budgetWithProgress, this)
    }

    override fun getItemCount() = budgetsWithProgress.size

    fun removeAt(position: Int) {
        budgetsWithProgress.removeAt(position)
        notifyItemRemoved(position)
    }

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvLimit: TextView = itemView.findViewById(R.id.tvLimit)
        private val tvPeriod: TextView = itemView.findViewById(R.id.tvPeriod)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tvSpent: TextView = itemView.findViewById(R.id.tvSpent)
        private val tvRemaining: TextView = itemView.findViewById(R.id.tvRemaining)
        private val btnAddExpense: Button = itemView.findViewById(R.id.btnAddExpense)
        private val btnDeleteBudget: Button = itemView.findViewById(R.id.btnDeleteBudget)
        private val defaultTextColor: ColorStateList = tvRemaining.textColors
        private val db = FirebaseFirestore.getInstance()

        fun bind(budgetWithProgress: BudgetWithProgress, adapter: BudgetAdapter) {
            val budget = budgetWithProgress.budget
            val spent = budgetWithProgress.spent
            val remaining = budget.limit - spent
            val progress = if (budget.limit > 0) ((spent / budget.limit) * 100).toInt() else 0

            tvCategory.text = budget.category
            tvLimit.text = String.format("Limit: %.2f", budget.limit)
            tvPeriod.text = budget.period
            progressBar.progress = progress
            tvSpent.text = String.format("Spent: %.2f", spent)
            tvRemaining.text = String.format("Remaining: %.2f", remaining)

            if (spent > budget.limit) {
                val redColor = ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
                tvSpent.setTextColor(redColor)
                tvRemaining.setTextColor(redColor)
                progressBar.progressTintList = ColorStateList.valueOf(redColor)
            } else {
                tvSpent.setTextColor(defaultTextColor)
                tvRemaining.setTextColor(defaultTextColor)
                progressBar.progressTintList = null // Or set to a default color
            }

            btnAddExpense.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, AddExpenseActivity::class.java)
                intent.putExtra("CATEGORY_FROM_BUDGET", budget.category)
                context.startActivity(intent)
            }

            btnDeleteBudget.setOnClickListener {
                db.collection("budgets").document(budget.id).delete()
                    .addOnSuccessListener {
                        adapter.removeAt(adapterPosition)
                    }
            }
        }
    }
}