package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import com.google.firebase.firestore.PropertyName

data class Expense(
    // We store the Firestore Document ID here for easy reference later (e.g., for editing/deleting)
    @get:PropertyName("expenseId") @set:PropertyName("expenseId")
    var expenseId: String = "",

    val userId: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val notes: String = "",

    // We store the date as a simple Long (milliseconds) for easy sorting
    val date: Long = System.currentTimeMillis()
)