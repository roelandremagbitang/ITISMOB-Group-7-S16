package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BudgetBuilderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_builder)

        val btnAdd = findViewById<Button>(R.id.btnAddBudgetCategory)
        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddBudgetCategoryActivity::class.java))
        }
    }
}
