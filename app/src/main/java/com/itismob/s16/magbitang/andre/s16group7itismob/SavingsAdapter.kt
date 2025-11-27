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

class SavingsAdapter(private val list: List<SavingsChallenge>) :
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

        // Formats Currency (Current / Goal)
        val currency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        // Remove decimals if necessary, or keep default
        currency.maximumFractionDigits = 0

        val currentStr = currency.format(item.currentAmount)
        val goalStr = currency.format(item.goalAmount)

        holder.tvAmountProgress.text = "$currentStr / $goalStr"

        // Sets Date
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        holder.tvDate.text = "Target: ${sdf.format(Date(item.targetDate))}"

        // Sets Progress Bar
        val progress = if (item.goalAmount > 0) {
            ((item.currentAmount / item.goalAmount) * 100).toInt()
        } else {
            0
        }
        holder.progressBar.progress = progress
        holder.tvPercent.text = "$progress%"
    }

    override fun getItemCount() = list.size
}