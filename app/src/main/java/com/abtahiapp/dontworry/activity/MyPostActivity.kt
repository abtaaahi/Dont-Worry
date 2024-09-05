package com.abtahiapp.dontworry.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Post
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.adapter.PostAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyPostActivity : AppCompatActivity() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post)

        recyclerViewPosts = findViewById(R.id.recycler_view_posts)
        recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(mutableListOf())
        recyclerViewPosts.adapter = postAdapter

        val account = GoogleSignIn.getLastSignedInAccount(this)
        currentUserId = account?.id ?: "unknown"

        databaseReference = FirebaseDatabase.getInstance().getReference("social_posts")
        loadPostsFromDatabase()

        postAdapter.setOnItemClickListener { post ->
            showOptionsDialog(post)
        }

        postAdapter.setOnPostLongClickListener { post ->
            showOptionsDialog(post)
        }
    }

    private fun loadPostsFromDatabase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null && post.userId == currentUserId) {
                        post.id = postSnapshot.key ?: ""
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

    private fun showOptionsDialog(post: Post) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_post_options, null)
        val editTextPostContent = dialogView.findViewById<EditText>(R.id.edit_text_post_content)

        editTextPostContent.setText(post.content)

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Options")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedContent = editTextPostContent.text.toString().trim()
                if (updatedContent.isNotEmpty()) {
                    updatePost(post, updatedContent)
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                deletePost(post)
            }
            .setNeutralButton("Cancel", null)

        val dialog = dialogBuilder.create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.textColor))
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.red))
            neutralButton.setTextColor(ContextCompat.getColor(this, R.color.textColor))
        }

        dialog.show()
    }

    private fun updatePost(post: Post, updatedContent: String) {
        val postId = post.id
        databaseReference.child(postId).child("content").setValue(updatedContent)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update post", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deletePost(post: Post) {
        val postId = post.id
        databaseReference.child(postId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
                }
            }
    }
}