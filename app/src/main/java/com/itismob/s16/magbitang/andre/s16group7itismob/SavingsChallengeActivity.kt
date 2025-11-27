package com.itismob.s16.mco3.smartexptracker.s16group7itismob

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavingsChallengeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val list = mutableListOf<SavingsChallenge>()
    private lateinit var adapter: SavingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings_challenge)

        val rv = findViewById<RecyclerView>(R.id.rvSavings)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddChallenge)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = SavingsAdapter(list)
        rv.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(this, AddSavingsChallengeActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadChallenges()
    }

    private fun loadChallenges() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("savings_challenges").whereEqualTo("userId", userId).get().addOnSuccessListener { result ->
            list.clear()
            for (doc in result) {
                val item = doc.toObject(SavingsChallenge::class.java)
                list.add(item)
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}