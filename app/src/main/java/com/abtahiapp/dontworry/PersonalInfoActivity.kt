package com.abtahiapp.dontworry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.makeText
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var account: GoogleSignInAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        account = intent.getParcelableExtra("account")!!

        val submitButton: Button = findViewById(R.id.button2)
        submitButton.setOnClickListener {
            savePersonalInfo()
        }
    }

    private fun savePersonalInfo() {

        val musicTextInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout)
        val bookMovieTextInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout2)
        val likesTextInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout4)
        val dislikesTextInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout3)
        val trustTextInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout5)
        val waterTextInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout6)
        val choicesTextInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout7)
        val sleepTextInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout8)

        val musicEditText = musicTextInputLayout.editText as? TextInputEditText
        val bookMovieEditText =bookMovieTextInputLayout.editText as? TextInputEditText
        val likesEditText = likesTextInputLayout.editText as? TextInputEditText
        val dislikesEditText = dislikesTextInputLayout.editText as? TextInputEditText
        val trustEditText = trustTextInputLayout.editText as? TextInputEditText
        val waterEditText = waterTextInputLayout.editText as? TextInputEditText
        val choicesEditText = choicesTextInputLayout.editText as? TextInputEditText
        val sleepEditText = sleepTextInputLayout.editText as? TextInputEditText

        val userInfo = mapOf(
            "music" to musicEditText?.text.toString(),
            "bookMovie" to bookMovieEditText?.text.toString(),
            "likes" to likesEditText?.text.toString(),
            "dislikes" to dislikesEditText?.text.toString(),
            "trust" to trustEditText?.text.toString(),
            "water" to waterEditText?.text.toString(),
            "choices" to choicesEditText?.text.toString(),
            "sleep" to sleepEditText?.text.toString()
        )

        val databaseReference = FirebaseDatabase.getInstance().getReference("user_information").child(account.id!!)
        databaseReference.updateChildren(userInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("account", account)
                startActivity(intent)
                finish()
            } else {
                makeText(this, "Failed to save information: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
