package com.abtahiapp.dontworry.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Mood
import com.abtahiapp.dontworry.adapter.MoodAdapter
import com.abtahiapp.dontworry.R
import com.google.firebase.database.FirebaseDatabase

class MyActivity : AppCompatActivity() {

    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter
    private val moodList = mutableListOf<Mood>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        moodRecyclerView = findViewById(R.id.mood_recycler_view)
        moodRecyclerView.layoutManager = LinearLayoutManager(this)
        moodAdapter = MoodAdapter(moodList)
        moodRecyclerView.adapter = moodAdapter

        val userId = intent.getStringExtra("userId") ?: return
        val database = FirebaseDatabase.getInstance().getReference("user_information/$userId/mood_history")

        database.get().addOnSuccessListener { dataSnapshot ->
            for (snapshot in dataSnapshot.children) {
                val dateTime = snapshot.child("dateTime").value as String
                val moodName = snapshot.child("mood").value as String
                val details = snapshot.child("details").value as String
                val moodImage = getMoodImageResource(moodName)

                val mood = Mood(moodImage, dateTime, details)
                moodList.add(mood)
            }
            moodAdapter.notifyDataSetChanged()
        }
    }

    private fun getMoodImageResource(moodName: String): Int {
        return when (moodName) {
            "Angry" -> R.drawable.angry
            "Very Sad" -> R.drawable.very_sad
            "Sad" -> R.drawable.sad
            "Fine" -> R.drawable.fine
            "Very Fine" -> R.drawable.very_fine
            else -> R.drawable.fine
        }
    }
}
