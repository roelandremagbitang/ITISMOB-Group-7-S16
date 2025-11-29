package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import com.google.firebase.firestore.PropertyName

data class SavingsTransaction(
    @get:PropertyName("transactionId") @set:PropertyName("transactionId")
    var transactionId: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val type: String = "deposit" // "deposit" or "withdrawal"
)