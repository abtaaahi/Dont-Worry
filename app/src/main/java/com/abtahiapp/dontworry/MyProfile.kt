package com.abtahiapp.dontworry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import de.hdodenhof.circleimageview.CircleImageView

class MyProfile : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        val userNameTextView: TextView = findViewById(R.id.user_name)
        val userEmailTextView: TextView = findViewById(R.id.user_email)
        val profileImageView: CircleImageView = findViewById(R.id.profile_image)
        val logoutButton: Button = findViewById(R.id.logout_button)
        val editDetailsButton: ImageButton = findViewById(R.id.editDetails)
        val activityButton: Button = findViewById(R.id.activity_button)

        userId = intent.getStringExtra("userId") ?: ""
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val photoUrl = intent.getStringExtra("photoUrl")

        activityButton.setOnClickListener {
            val intent = Intent(this, MyActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        userNameTextView.text = name
        userEmailTextView.text = email

        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .fallback(R.drawable.person)
            .into(profileImageView)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        logoutButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        editDetailsButton.setOnClickListener {
            val intent = Intent(this, InfoUpdateActivity::class.java)
            intent.putExtra("userID", userId)
            startActivity(intent)
        }

        if (userId != null) {
            loadProfileData(userId)
        } else {
            Toast.makeText(this, "Failed to retrieve email address.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData(userId)
    }

    private fun loadProfileData(userId: String) {
        val musicTextView: TextView = findViewById(R.id.music)
        val bookMovieTextView: TextView = findViewById(R.id.book_movie)
        val likesTextView: TextView = findViewById(R.id.likes)
        val dislikesTextView: TextView = findViewById(R.id.dislikes)
        val trustTextView: TextView = findViewById(R.id.trust)
        val waterTextView: TextView = findViewById(R.id.water)
        val choicesTextView: TextView = findViewById(R.id.choices)
        val sleepTextView: TextView = findViewById(R.id.sleep)

        val databaseReference = FirebaseDatabase.getInstance().getReference("user_information").child(userId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.getValue<Map<String, Any>>()
                    userData?.let {
                        musicTextView.text = "Favorite Music Type: " + it["music"].toString()
                        bookMovieTextView.text = "Favorite Book/Movie: " + it["bookMovie"].toString()
                        likesTextView.text = "Likes: " + it["likes"].toString()
                        dislikesTextView.text = "Dislikes: " + it["dislikes"].toString()
                        trustTextView.text = "Trustworthy Person: " + it["trust"].toString()
                        waterTextView.text = "Water Intake: " + it["water"].toString() + " Glass"
                        choicesTextView.text = "Choices: " + it["choices"].toString()
                        sleepTextView.text = "Sleep: " + it["sleep"].toString() + " Hours"
                    }
                } else {
                    Toast.makeText(this@MyProfile, "No data found for this user.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MyProfile", "Database error: ${databaseError.message}")
            }
        })
    }
}