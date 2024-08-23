package com.abtahiapp.dontworry

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                val intent = Intent(this, MyProfile::class.java)
                intent.putExtra("userId", account.id)
                intent.putExtra("name", account.displayName)
                intent.putExtra("email", account.email)
                intent.putExtra("photoUrl", account.photoUrl.toString())

                startActivity(intent)
            }

//            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//
//            val database = FirebaseDatabase.getInstance().getReference("user_information")
//            val moodHistoryRef = database.child(account.id!!).child("mood_history")
//
//            moodHistoryRef.orderByChild("dateTime").startAt(currentDate).endAt(currentDate + "\uf8ff")
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        if (!dataSnapshot.exists()) {
//                            showMoodDialog(account.id!!)
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {
//                        Toast.makeText(this@MainActivity, "Failed to check mood history", Toast.LENGTH_SHORT).show()
//                    }
//                })
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val database = FirebaseDatabase.getInstance().getReference("user_information")
            val moodHistoryRef = database.child(account.id!!).child("mood_history")

            moodHistoryRef.orderByChild("date").equalTo(currentDate)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            showMoodDialog(account.id!!)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Failed to check mood history", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
    private fun showMoodDialog(userId: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.how_was_the_day_dialog)

        val moodImages = mapOf(
            R.id.mood_1 to "Angry",
            R.id.mood_2 to "Very Sad",
            R.id.mood_3 to "Sad",
            R.id.mood_4 to "Fine",
            R.id.mood_5 to "Very Fine"
        )

        val etDetails: EditText = dialog.findViewById(R.id.et_details)

        var selectedMood: String? = null

        moodImages.forEach { (imageViewId, moodName) ->
            val imageView: ImageView = dialog.findViewById(imageViewId)
            imageView.setOnClickListener {
                if (selectedMood == moodName) {
                    selectedMood = null
                    moodImages.keys.forEach { id ->
                        dialog.findViewById<ImageView>(id).visibility = View.VISIBLE
                    }
                } else {
                    selectedMood = moodName
                    moodImages.keys.forEach { id ->
                        dialog.findViewById<ImageView>(id).visibility = if (id == imageViewId) View.VISIBLE else View.INVISIBLE
                    }
                }
            }
        }

        val button: Button = dialog.findViewById(R.id.submit)
        button.setOnClickListener {
            selectedMood?.let {
                storeMoodInDatabase(userId, it, etDetails.text.toString())
                dialog.dismiss()
            } ?: Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

//    private fun storeMoodInDatabase(userId: String, moodName: String, details: String) {
//        val database = FirebaseDatabase.getInstance().getReference("user_information")
//
//        val currentDateTime = SimpleDateFormat("hh:mm a dd MMM", Locale.getDefault()).format(Date())
//
//        val moodData = mapOf(
//            "dateTime" to currentDateTime,
//            "mood" to moodName,
//            "details" to details
//        )
//
//        //database.child(userId).child("mood_history").child(currentDateTime).setValue(moodData)
//        database.child(userId).child("mood_history").push().setValue(moodData).addOnSuccessListener {
//        }.addOnFailureListener {
//            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
//        }
//    }

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

}