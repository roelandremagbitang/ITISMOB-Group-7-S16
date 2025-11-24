package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        val btnUse = findViewById<Button>(R.id.btnUseScan)
        btnUse.setOnClickListener {
            // For Phase 2: go to ScanResultActivity with dummy text
            val i = Intent(this, ScanResultActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}
