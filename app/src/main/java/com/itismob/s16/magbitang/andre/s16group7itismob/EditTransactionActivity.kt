package com.itismob.s16.magbitang.andre.s16group7itismob

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class EditTransactionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnDiscard = findViewById<Button>(R.id.btnDiscard)
        val btnSave = findViewById<Button>(R.id.btnSaveEdit)

        btnDelete.setOnClickListener {
            // TODO: show confirm dialog (UI)
        }
        btnDiscard.setOnClickListener {
            finish()
        }
        btnSave.setOnClickListener {
            // TODO: update DB
            finish()
        }
    }
}
