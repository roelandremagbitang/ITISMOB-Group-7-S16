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
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = SavingsAdapter(list) { selectedChallenge ->
            val intent = Intent(this, SavingsChallengeDetailActivity::class.java)
            // Pass the ID and other details needed
            intent.putExtra("SAVINGS_ID", selectedChallenge.savingsId)
            intent.putExtra("NAME", selectedChallenge.name)
            intent.putExtra("CURRENT_AMOUNT", selectedChallenge.currentAmount)
            intent.putExtra("GOAL_AMOUNT", selectedChallenge.goalAmount)
            intent.putExtra("TARGET_DATE", selectedChallenge.targetDate)
            startActivity(intent)
        }
        rv.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(this, AddSavingsChallengeActivity::class.java))
        }
        btnBack.setOnClickListener { finish() }
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
            list.sortBy { it.targetDate }

            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}