package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This activity uses your activity_dashboard.xml layout
        setContentView(R.layout.activity_dashboard)
    } //remove once done testing
} //remove once done testing
/*
        // TODO: Implement the dashboard logic here:
        // 1. Fetch and display the user's name (tvGreeting).
        // 2. Load and display total expenses (tvTotalExpense).
        // 3. Set up the RecyclerView for transactions (rvTransactions).
        // 4. Implement click listeners for the Bottom Navigation and the FAB.

        val btnGraphs = findViewById<ImageButton>(R.id.btnGraphs)
        val btnBudget = findViewById<ImageButton>(R.id.btnBudget)
        val fabAdd = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddExpense)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        btnGraphs.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }

        btnBudget.setOnClickListener {
            startActivity(Intent(this, BudgetBuilderActivity::class.java))
        }

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }
}

*/