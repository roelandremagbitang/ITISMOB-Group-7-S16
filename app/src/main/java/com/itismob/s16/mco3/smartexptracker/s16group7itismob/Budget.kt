package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import com.google.firebase.firestore.DocumentId

data class Budget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "",
    val limit: Double = 0.0,
    val period: String = "" // e.g., "Weekly" or "Monthly"
)