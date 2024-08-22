package com.abtahiapp.dontworry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val profileImageView: CircleImageView = findViewById(R.id.profile_image)

        val account = intent.getParcelableExtra<GoogleSignInAccount>("account")
        if (account != null) {

            Glide.with(this)
                .load(account.photoUrl)
                .into(profileImageView)

            profileImageView.setOnClickListener {
                val intent = Intent(this, MyProfile::class.java)

                intent.putExtra("name", account.displayName)
                intent.putExtra("email", account.email)
                intent.putExtra("photoUrl", account.photoUrl.toString())

                startActivity(intent)
            }
        }
    }
}