package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ToolActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tool)

        val btnSavings = findViewById<android.widget.Button>(R.id.btnSavingsTool)

        btnSavings.setOnClickListener {
            startActivity(android.content.Intent(this, SavingsChallengeActivity::class.java))
        }
    }
}