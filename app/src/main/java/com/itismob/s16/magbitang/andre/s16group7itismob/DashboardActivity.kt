package com.itismob.s16.magbitang.andre.s16group7itismob

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This activity uses your activity_dashboard.xml layout
        setContentView(R.layout.activity_dashboard)

        // TODO: Implement the dashboard logic here:
        // 1. Fetch and display the user's name (tvGreeting).
        // 2. Load and display total expenses (tvTotalExpense).
        // 3. Set up the RecyclerView for transactions (rvTransactions).
        // 4. Implement click listeners for the Bottom Navigation and the FAB.
    }
}