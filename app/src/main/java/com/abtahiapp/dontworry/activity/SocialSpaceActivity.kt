package com.abtahiapp.dontworry.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Post
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.adapter.PostAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class SocialSpaceActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var editTextPost: EditText
    private lateinit var postButton: Button
    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_space)

        profileImageView = findViewById(R.id.profile_image)
        editTextPost = findViewById(R.id.edit_text_post)
        postButton = findViewById(R.id.post_button)
        recyclerViewPosts = findViewById(R.id.recycler_view_posts)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Glide.with(this)
                .load(account.photoUrl)
                .placeholder(R.drawable.person)
                .into(profileImageView)
        }

        postButton.visibility = Button.GONE

        editTextPost.setOnTouchListener { _, event ->
            editTextPost.isCursorVisible = true
            false
        }

        editTextPost.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                editTextPost.isCursorVisible = true
            } else {
                editTextPost.isCursorVisible = false
            }
        }

        editTextPost.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                postButton.visibility = if (s.isNullOrEmpty()) Button.GONE else Button.VISIBLE
            }
        })

        databaseReference = FirebaseDatabase.getInstance().getReference("social_posts")

        recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(mutableListOf())
        recyclerViewPosts.adapter = postAdapter

        loadPostsFromDatabase()

        postButton.setOnClickListener {
            postContentToDatabase(account?.displayName, account?.photoUrl.toString())
        }
    }

    private fun postContentToDatabase(userName: String?, userPhotoUrl: String?) {
        val postContent = editTextPost.text.toString().trim()
        if (postContent.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            val postTime = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

            val post = Post(userName ?: "Anonymous", userPhotoUrl ?: "", postContent, postTime)
            val postId = databaseReference.push().key ?: return

            databaseReference.child(postId).setValue(post).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    editTextPost.text.clear()
                    postButton.visibility = Button.GONE
                }
            }
        }
    }

    private fun loadPostsFromDatabase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        posts.add(post)
                    }
                }
                posts.sortByDescending { it.postTime }
                postAdapter.updatePosts(posts)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}