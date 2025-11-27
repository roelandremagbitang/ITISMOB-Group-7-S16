package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import com.google.firebase.firestore.PropertyName

data class SavingsChallenge(
    @get:PropertyName("savingsId") @set:PropertyName("savingsId")
    var savingsId: String = "",
    val userId: String = "",
    val name: String = "",
    val currentAmount: Double = 0.0,
    val goalAmount: Double = 0.0,
    val frequency: String = "Daily",
    val targetDate: Long = 0L,
    val amountPerFrequency: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)