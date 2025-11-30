package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

class SavingsAdapter(private val list: List<SavingsChallenge>, private val onItemClick: (SavingsChallenge) -> Unit) :
    RecyclerView.Adapter<SavingsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvChallengeName)
        val tvAmountProgress: TextView = itemView.findViewById(R.id.tvAmountProgress)
        val tvDate: TextView = itemView.findViewById(R.id.tvTargetDate)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarSavings)
        val tvPercent: TextView = itemView.findViewById(R.id.tvPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_savings_tab, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvName.text = item.name

        val currency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        currency.maximumFractionDigits = 0
        val currentStr = currency.format(item.currentAmount)
        val goalStr = currency.format(item.goalAmount)
        holder.tvAmountProgress.text = "$currentStr / $goalStr"

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        holder.tvDate.text = "Target: ${sdf.format(Date(item.targetDate))}"

        val percentage = if (item.goalAmount > 0) {
            ((item.currentAmount / item.goalAmount) * 100).toInt()
        } else {
            0
        }

        holder.progressBar.progress = percentage.coerceIn(0, 100)
        holder.tvPercent.text = "$percentage%"
        if (percentage >= 100) {
            holder.tvPercent.setTextColor("#4CAF50".toColorInt())
        } else {
            holder.tvPercent.setTextColor("#6C63FF".toColorInt())
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}