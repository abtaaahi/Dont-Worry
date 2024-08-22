package com.abtahiapp.dontworry

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("account", account)
            startActivity(intent)
        } else {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        finish()
        }, SPLASH_TIME_OUT)
    }
}