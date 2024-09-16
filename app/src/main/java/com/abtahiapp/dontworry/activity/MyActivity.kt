package com.abtahiapp.dontworry.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Mood
import com.abtahiapp.dontworry.adapter.MoodAdapter
import com.abtahiapp.dontworry.R
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import com.abtahiapp.dontworry.room.MoodDao
import com.abtahiapp.dontworry.room.MoodDatabase
import com.abtahiapp.dontworry.room.MoodEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyActivity : AppCompatActivity() {

    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter
    private val moodList = mutableListOf<Mood>()
    private lateinit var moodDao: MoodDao
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        moodRecyclerView = findViewById(R.id.mood_recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        moodRecyclerView.layoutManager = LinearLayoutManager(this)
        moodAdapter = MoodAdapter(moodList)
        moodRecyclerView.adapter = moodAdapter

        val database = MoodDatabase.getDatabase(this)
        moodDao = database.moodDao()

        if (isOnline(this)) {
            fetchMoodsFromFirebase()
        } else {
            loadMoodsFromLocal()
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

    private fun parseDateTime(dateTime: String): Date? {
        val sdf = SimpleDateFormat("hh:mm a dd MMM", Locale.getDefault())
        return sdf.parse(dateTime)
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun fetchMoodsFromFirebase() {
        progressBar.visibility = View.VISIBLE
        val userId = intent.getStringExtra("userId") ?: return
        val database = FirebaseDatabase.getInstance().getReference("user_information/$userId/mood_history")

        database.get().addOnSuccessListener { dataSnapshot ->
            val moods = mutableListOf<MoodEntity>()
            moodList.clear()
            for (snapshot in dataSnapshot.children) {
                val moodName = snapshot.child("mood").value as? String ?: ""
                val moodImage = getMoodImageResource(moodName)

                val dateTime = snapshot.child("dateTime").value as? String ?: ""
                val details = snapshot.child("details").value as? String ?: ""

                val mood = Mood(moodImage, dateTime, details)
                moodList.add(mood)
                moods.add(MoodEntity(moodImage, dateTime, details))
            }

            moodList.sortByDescending { parseDateTime(it.dateTime) }
            moodAdapter.notifyDataSetChanged()

            lifecycleScope.launch {
                moodDao.clearMoods()
                moodDao.insertAll(moods)
            }

            progressBar.visibility = View.GONE
        }
    }

    private fun loadMoodsFromLocal() {
        lifecycleScope.launch(Dispatchers.IO) {
            val moods = moodDao.getAllMoods()
            withContext(Dispatchers.Main) {
                moodList.clear()
                for (moodEntity in moods) {
                    moodList.add(Mood(moodEntity.moodImage, moodEntity.dateTime, moodEntity.details))
                }
                moodList.sortByDescending { parseDateTime(it.dateTime) }
                moodAdapter.notifyDataSetChanged()
            }
        }
    }
}