package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

class SavingsTransactionAdapter(
    private val list: List<SavingsTransaction>,
    private val onItemClick: (SavingsTransaction) -> Unit
) : RecyclerView.Adapter<SavingsTransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvTransDate)
        val tvNotes: TextView = itemView.findViewById(R.id.tvTransNotes)
        val tvAmount: TextView = itemView.findViewById(R.id.tvTransAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_savings_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        holder.tvDate.text = sdf.format(Date(item.date))
        holder.tvNotes.text = item.notes.ifEmpty { "No notes" }

        val currency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        val amountStr = currency.format(item.amount)

        if (item.type == "deposit") {
            holder.tvAmount.text = "+ $amountStr"
            holder.tvAmount.setTextColor("#4CAF50".toColorInt())
        } else {
            holder.tvAmount.text = "- $amountStr"
            holder.tvAmount.setTextColor("#FF6B6B".toColorInt())
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = list.size
}