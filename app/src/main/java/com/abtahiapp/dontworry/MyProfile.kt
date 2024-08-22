package com.abtahiapp.dontworry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import de.hdodenhof.circleimageview.CircleImageView


class MyProfile : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        val userNameTextView: TextView = findViewById(R.id.user_name)
        val userEmailTextView: TextView = findViewById(R.id.user_email)
        val profileImageView: CircleImageView = findViewById(R.id.profile_image)
        val logoutButton: Button = findViewById(R.id.logout_button)

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val photoUrl = intent.getStringExtra("photoUrl")

        userNameTextView.text = name
        userEmailTextView.text = email

        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .into(profileImageView)
        }

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
    }
}