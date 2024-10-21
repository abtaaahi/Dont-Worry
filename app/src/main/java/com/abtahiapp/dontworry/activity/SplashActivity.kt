package com.abtahiapp.dontworry.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.apiservice.TextBlobApiService
import com.abtahiapp.dontworry.utils.NetworkUtil
import com.abtahiapp.dontworry.utils.RetrofitClient
import com.abtahiapp.dontworry.utils.SentimentRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (NetworkUtil.isOnline(this)) {
            wakeUpServer()
        }

        Handler(Looper.getMainLooper()).postDelayed({
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("account", account)
            startActivity(intent)
            overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
        } else {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim)
        }
        finish()
        }, SPLASH_TIME_OUT)
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