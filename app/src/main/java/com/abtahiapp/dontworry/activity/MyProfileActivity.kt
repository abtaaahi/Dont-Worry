package com.abtahiapp.dontworry.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.room.UserProfileDao
import com.abtahiapp.dontworry.room.UserProfileDatabase
import com.abtahiapp.dontworry.room.UserProfileEntity
import com.abtahiapp.dontworry.utils.NetworkUtil.isOnline
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.getValue
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class MyProfileActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userId: String
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var database: UserProfileDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        val userNameTextView: TextView = findViewById(R.id.user_name)
        val userEmailTextView: TextView = findViewById(R.id.user_email)
        val profileImageView: CircleImageView = findViewById(R.id.profile_image)
        val logoutButton: Button = findViewById(R.id.logout_button)
        val editDetailsButton: ImageButton = findViewById(R.id.editDetails)
        val activityButton: Button = findViewById(R.id.activity_button)
        val myPostButton: Button = findViewById(R.id.my_post_button)
        val personalSpaceButton: Button = findViewById(R.id.personal_space)


        database = UserProfileDatabase.getDatabase(this)
        userProfileDao = database.userProfileDao()

        userId = intent.getStringExtra("userId") ?: ""
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val photoUrl = intent.getStringExtra("photoUrl")

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
            if(isOnline(this)){
                googleSignInClient.signOut().addOnCompleteListener {
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
                    finish()
                }
            } else{
                Toast.makeText(this, "You are offline!", Toast.LENGTH_SHORT).show()
            }
        }

        editDetailsButton.setOnClickListener {
            if(isOnline(this)){
                val intent = Intent(this, InfoUpdateActivity::class.java)
                intent.putExtra("userID", userId)
                startActivity(intent)
            } else{
                Toast.makeText(this, "You are offline!", Toast.LENGTH_SHORT).show()
            }
        }

        activityButton.setOnClickListener {
            val intent = Intent(this, MyActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
        }

        personalSpaceButton.setOnClickListener {
            val intent = Intent(this, PersonalSpaceActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("name", name)
            startActivity(intent)
            overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
        }

        myPostButton.setOnClickListener {
            val intent = Intent(this, MyPostActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
        }

        if (isOnline(this)) {
            loadProfileDataFromFirebase(userId)
        } else {
            loadProfileDataFromLocal(userId)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isOnline(this)) {
            loadProfileDataFromFirebase(userId)
        } else {
            loadProfileDataFromLocal(userId)
        }
    }

    private fun loadProfileDataFromFirebase(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("user_information").child(userId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.getValue<Map<String, Any>>()

                    userData?.let {
                        Log.d("MyProfile", "$userId Data fetched from Firebase: $userData")

                        val music = it["music"]?.toString() ?: ""
                        val bookMovie = it["bookMovie"]?.toString() ?: ""
                        val likes = it["likes"]?.toString() ?: ""
                        val dislikes = it["dislikes"]?.toString() ?: ""
                        val trust = it["trust"]?.toString() ?: ""
                        val water = it["water"]?.toString() ?: ""
                        val choices = it["choices"]?.toString() ?: ""
                        val sleep = it["sleep"]?.toString() ?: ""

                        updateUI(music, bookMovie, likes, dislikes, trust, water, choices, sleep)

                        val userProfile = UserProfileEntity(
                            music = music,
                            bookMovie = bookMovie,
                            likes = likes,
                            dislikes = dislikes,
                            trust = trust,
                            water = water,
                            choices = choices,
                            sleep = sleep,
                            userId = userId
                        )
                        saveProfileDataToLocal(userProfile)
                    }
                } else {
                    Log.d("MyProfile", "No data found in Firebase for user: $userId")
                    Toast.makeText(this@MyProfileActivity, "No data found for this user.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyProfile", "Database error: ${error.message}")
            }
        })
    }

    private fun updateUI(music: String, bookMovie: String, likes: String,
                         dislikes: String, trust: String, water: String, choices: String, sleep: String) {
        val musicTextView: TextView = findViewById(R.id.music)
        val bookMovieTextView: TextView = findViewById(R.id.book_movie)
        val likesTextView: TextView = findViewById(R.id.likes)
        val dislikesTextView: TextView = findViewById(R.id.dislikes)
        val trustTextView: TextView = findViewById(R.id.trust)
        val waterTextView: TextView = findViewById(R.id.water)
        val choicesTextView: TextView = findViewById(R.id.choices)
        val sleepTextView: TextView = findViewById(R.id.sleep)

        musicTextView.text = "Favorite Music Type: $music"
        bookMovieTextView.text = "Favorite Book/Movie: $bookMovie"
        likesTextView.text = "Likes: $likes"
        dislikesTextView.text = "Dislikes: $dislikes"
        trustTextView.text = "Trustworthy Person: $trust"
        waterTextView.text = "Water Intake: $water Glass"
        choicesTextView.text = "Choices: $choices"
        sleepTextView.text = "Sleep: $sleep Hours"
    }

    private fun saveProfileDataToLocal(userProfile: UserProfileEntity) {
        lifecycleScope.launch {
            try {
                userProfileDao.insertUserProfile(userProfile)
                Log.d("MyProfile", "Data successfully saved locally")
            } catch (e: Exception) {
                Log.e("MyProfile", "Error saving data: ${e.message}")
            }
        }
    }

    private fun loadProfileDataFromLocal(userId: String) {
        lifecycleScope.launch {
            val userProfile = userProfileDao.getUserProfile(userId)
            Log.d("MyProfile", "Retrieved from local: $userProfile")
            userProfile?.let {
                Log.d("MyProfile", "Data retrieved from local database: $it")
                updateUI(it.music, it.bookMovie, it.likes, it.dislikes, it.trust, it.water, it.choices, it.sleep)
            } ?: run {
                Log.d("MyProfile", "No offline data found in local database.")
                Toast.makeText(this@MyProfileActivity, "No offline data found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}