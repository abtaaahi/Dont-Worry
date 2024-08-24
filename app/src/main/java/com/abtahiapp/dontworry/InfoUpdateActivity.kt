package com.abtahiapp.dontworry

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class InfoUpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_update)

        val userID = intent.getStringExtra("userID") ?: ""

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

        val databaseReference = FirebaseDatabase.getInstance().getReference("user_information").child(userID)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.getValue<Map<String, Any>>()
                    userData?.let {
                        musicEditText?.setText(it["music"].toString())
                        bookMovieEditText?.setText(it["bookMovie"].toString())
                        likesEditText?.setText(it["likes"].toString())
                        dislikesEditText?.setText(it["dislikes"].toString())
                        trustEditText?.setText(it["trust"].toString())
                        waterEditText?.setText(it["water"].toString())
                        choicesEditText?.setText(it["choices"].toString())
                        sleepEditText?.setText(it["sleep"].toString())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("InfoUpdateActivity", "Database error: ${databaseError.message}")
            }
        })

        val updateButton: Button = findViewById(R.id.button2)
        updateButton.setOnClickListener {
            val music = musicEditText?.text.toString()
            val bookMovie = bookMovieEditText?.text.toString()
            val likes = likesEditText?.text.toString()
            val dislikes = dislikesEditText?.text.toString()
            val trust = trustEditText?.text.toString()
            val water = waterEditText?.text.toString()
            val choices = choicesEditText?.text.toString()
            val sleep = sleepEditText?.text.toString()

            val updatedData = mapOf(
                "music" to music,
                "bookMovie" to bookMovie,
                "likes" to likes,
                "dislikes" to dislikes,
                "trust" to trust,
                "water" to water,
                "choices" to choices,
                "sleep" to sleep
            )

            databaseReference.updateChildren(updatedData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Information updated successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update information.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
