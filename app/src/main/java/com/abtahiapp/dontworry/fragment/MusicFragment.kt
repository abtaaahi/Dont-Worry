package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.VideoResponse
import com.abtahiapp.dontworry.adapter.AudioAdapter
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MusicFragment : Fragment() {

    private lateinit var audioAdapter: AudioAdapter
    private lateinit var account: GoogleSignInAccount
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music, container, false)
        val audioRecyclerView: RecyclerView = view.findViewById(R.id.audio_recycler_view)

        audioRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        audioAdapter = AudioAdapter(requireContext(), mutableListOf())
        audioRecyclerView.adapter = audioAdapter

        account = activity?.intent?.getParcelableExtra("account") ?: return view

        database = FirebaseDatabase.getInstance().getReference("user_information")

        fetchLastMoodAndAudios()

        return view
    }

    private fun fetchLastMoodAndAudios() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val moodHistoryRef = database.child(account.id!!).child("mood_history")

        moodHistoryRef.orderByChild("date").equalTo(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val lastMood = if (dataSnapshot.exists()) {
                        dataSnapshot.children.last().child("mood").getValue(String::class.java)
                    } else {
                        null
                    }
                    fetchAudios(lastMood)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to check mood history", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchAudios(mood: String?) {
        val apiKey = BuildConfig.GOOGLE_API_KEY
        val query = when (mood) {
            "Angry" -> "relaxing music"
            "Very Sad", "Sad" -> "soothing music"
            "Fine", "Very Fine" -> "cheerful music"
            else -> "calmness music"
        }

        RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
            .enqueue(object : Callback<VideoResponse> {
                override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                    if (response.isSuccessful) {
                        val videos = response.body()?.items ?: emptyList()
                        audioAdapter.updateAudios(videos)
                    }
                }

                override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to fetch musics", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
