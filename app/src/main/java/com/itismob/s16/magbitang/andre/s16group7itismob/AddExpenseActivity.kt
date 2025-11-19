package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AddExpenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val btnScan = findViewById<Button>(R.id.btnScanReceipt)
        val btnSave = findViewById<Button>(R.id.btnSaveExpense)

        btnScan.setOnClickListener {
            // open camera scanner (UI only)
            startActivity(Intent(this, ScanActivity::class.java))
        }

        btnSave.setOnClickListener {
            // TODO: collect field values and save to DB (Phase 3)
            finish()
        }
    }
}
