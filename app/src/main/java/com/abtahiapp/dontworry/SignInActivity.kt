package com.abtahiapp.dontworry

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
            checkUserInformationExists(account)
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

                checkUserInformationExists(account)
            }
        } catch (e: ApiException) {
            makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUserInformationExists(account: GoogleSignInAccount) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("user_information")

        databaseReference.child(account.id!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.putExtra("account", account)
                    startActivity(intent)
                    finish()
                } else {
                    storeUserInfoInDatabase(account)
                    val intent = Intent(this@SignInActivity, PersonalInfoActivity::class.java)
                    intent.putExtra("account", account)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@SignInActivity, "Failed to check user information", Toast.LENGTH_SHORT).show()
            }
        })
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