package com.itismob.s16.magbitang.andre.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnEdit = findViewById<Button>(R.id.btnEditAccount)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnRecurring = findViewById<Button>(R.id.btnRecurring)
        val btnCategories = findViewById<Button>(R.id.btnCustomCategories)

        btnEdit.setOnClickListener {
            // TODO: open Edit Account screen
        }
        btnLogout.setOnClickListener {
            // TODO: clear session and go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        btnRecurring.setOnClickListener {
            // TODO: open recurring transactions screen (UI)
        }
        btnCategories.setOnClickListener {
            // TODO: open CategoryActivity (UI)
        }
    }
}
