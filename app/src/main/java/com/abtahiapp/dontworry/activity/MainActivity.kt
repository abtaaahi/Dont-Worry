package com.abtahiapp.dontworry.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.adapter.ViewPagerAdapter
import com.abtahiapp.dontworry.apiservice.TextBlobApiService
import com.abtahiapp.dontworry.utils.NetworkUtil
import com.abtahiapp.dontworry.utils.RetrofitClient
import com.abtahiapp.dontworry.utils.SentimentRequest
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (NetworkUtil.isOnline(this)) {
            wakeUpServer()
        }

        val profileImageView: CircleImageView = findViewById(R.id.profile_image)

        val account = intent.getParcelableExtra<GoogleSignInAccount>("account")
        if (account != null) {

            Glide.with(this)
                .load(account.photoUrl)
                .placeholder(R.drawable.person)
                .error(R.drawable.person)
                .fallback(R.drawable.person)
                .into(profileImageView)

            profileImageView.setOnClickListener {
                val intent = Intent(this, MyProfileActivity::class.java)
                intent.putExtra("userId", account.id)
                intent.putExtra("name", account.displayName)
                intent.putExtra("email", account.email)
                intent.putExtra("photoUrl", account.photoUrl.toString())

                startActivity(intent)
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
            }

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val database = FirebaseDatabase.getInstance().getReference("user_information")
            val moodHistoryRef = database.child(account.id!!).child("mood_history")

            moodHistoryRef.orderByChild("date").equalTo(currentDate)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            showMoodDialog(account.id!!)
                        } else {
                            val lastMood = dataSnapshot.children.last().child("mood").getValue(String::class.java)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Failed to check mood history", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.home)
                1 -> tab.setIcon(R.drawable.movie)
                2 -> tab.setIcon(R.drawable.music)
                3 -> tab.setIcon(R.drawable.video)
                4 -> tab.setIcon(R.drawable.article)
                5 -> tab.setIcon(R.drawable.weatherquote)
            }
        }.attach()
    }
    private fun showMoodDialog(userId: String) {
        val dialogView = layoutInflater.inflate(R.layout.how_was_the_day_dialog, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val moodImages = mapOf(
            R.id.mood_1 to "Angry",
            R.id.mood_2 to "Very Sad",
            R.id.mood_3 to "Sad",
            R.id.mood_4 to "Fine",
            R.id.mood_5 to "Very Fine"
        )

        val etDetails: EditText = dialogView.findViewById(R.id.et_details)

        var selectedMood: String? = null

        moodImages.forEach { (imageViewId, moodName) ->
            val imageView: ImageView = dialogView.findViewById(imageViewId)
            imageView.setOnClickListener {
                if (selectedMood == moodName) {
                    selectedMood = null
                    moodImages.keys.forEach { id ->
                        dialogView.findViewById<ImageView>(id).visibility = View.VISIBLE
                    }
                } else {
                    selectedMood = moodName
                    moodImages.keys.forEach { id ->
                        dialogView.findViewById<ImageView>(id).visibility = if (id == imageViewId) View.VISIBLE else View.INVISIBLE
                    }
                }
            }
        }

        val button: Button = dialogView.findViewById(R.id.submit)
        button.setOnClickListener {
            selectedMood?.let {
                storeMoodInDatabase(userId, it, etDetails.text.toString())
                dialog.dismiss()
            } ?: Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeMoodInDatabase(userId: String, moodName: String, details: String) {
        val database = FirebaseDatabase.getInstance().getReference("user_information")

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentDateTime = SimpleDateFormat("hh:mm a dd MMM", Locale.getDefault()).format(Date())

        val moodData = mapOf(
            "date" to currentDate,
            "dateTime" to currentDateTime,
            "mood" to moodName,
            "details" to details
        )

        database.child(userId).child("mood_history").push().setValue(moodData).addOnSuccessListener {
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save mood data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun wakeUpServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val service = RetrofitClient.create(TextBlobApiService::class.java)
                service.analyzeSentiment(SentimentRequest("Test"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}