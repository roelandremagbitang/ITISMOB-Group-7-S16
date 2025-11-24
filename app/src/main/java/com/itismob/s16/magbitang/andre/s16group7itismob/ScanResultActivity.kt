package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ScanResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val btnConfirm = findViewById<Button>(R.id.btnConfirmScan)
        btnConfirm.setOnClickListener {
            // TODO: return data to AddExpenseActivity (Phase 3)
            finish()
        }
    }
}
