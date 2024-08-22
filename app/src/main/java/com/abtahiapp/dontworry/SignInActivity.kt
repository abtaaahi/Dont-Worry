package com.abtahiapp.dontworry

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toast.makeText
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var isShowingMan = true
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        imageView = findViewById(R.id.imageView3)

        startImageSlider()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("account", account)
            startActivity(intent)
            finish()
        }

        val signInButton: Button = findViewById(R.id.sign_in_button)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {

                storeUserInfoInDatabase(account)

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("account", account)
                startActivity(intent)
                finish()
            }
        } catch (e: ApiException) {
            makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeUserInfoInDatabase(account: GoogleSignInAccount) {
        val database = FirebaseDatabase.getInstance()
        val databaseReference = database.getReference("user_information")

        val userInfo = mapOf(
            "name" to account.displayName,
            "email" to account.email
        )

        databaseReference.child(account.id!!).setValue(userInfo)
    }

    private fun startImageSlider() {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 500

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 500

        val runnable = object : Runnable {
            override fun run() {
                imageView.startAnimation(fadeOut)
                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        if (isShowingMan) {
                            imageView.setImageResource(R.drawable.woman)
                        } else {
                            imageView.setImageResource(R.drawable.man)
                        }

                        isShowingMan = !isShowingMan

                        imageView.startAnimation(fadeIn)
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })

                handler.postDelayed(this, 2500)
            }
        }

        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}