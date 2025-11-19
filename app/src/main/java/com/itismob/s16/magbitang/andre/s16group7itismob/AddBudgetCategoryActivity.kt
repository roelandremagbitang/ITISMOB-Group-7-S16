package com.itismob.s16.magbitang.andre.s16group7itismob

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AddBudgetCategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget_category)

        val btnSave = findViewById<Button>(R.id.btnSaveBudgetCategory)
        btnSave.setOnClickListener {
            // TODO: save category (UI only now)
            finish()
        }
    }
}
