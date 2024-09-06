package com.abtahiapp.dontworry.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Post
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.adapter.PostAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class SocialSpaceActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var editTextPost: EditText
    private lateinit var postButton: Button
    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var socket: Socket
    private lateinit var statusIndicator: View
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_space)

        profileImageView = findViewById(R.id.profile_image)
        editTextPost = findViewById(R.id.edit_text_post)
        postButton = findViewById(R.id.post_button)
        recyclerViewPosts = findViewById(R.id.recycler_view_posts)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_user_profile, null)
        statusIndicator = bottomSheetView.findViewById<View>(R.id.status_indicator)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        var userId: String? = null
        if (account != null) {
            Glide.with(this)
                .load(account.photoUrl)
                .placeholder(R.drawable.person)
                .into(profileImageView)
            userId = account.id
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

        if (account != null) {

            postAdapter.setOnProfileImageClickListener { post ->
                val currentUser = GoogleSignIn.getLastSignedInAccount(this)?.id
                if (post.userId == currentUser) {
                    val intent = Intent(this, MyProfile::class.java)
                    intent.putExtra("userId", account.id)
                    intent.putExtra("name", account.displayName)
                    intent.putExtra("email", account.email)
                    intent.putExtra("photoUrl", account.photoUrl.toString())

                    startActivity(intent)
                } else {
                    showUserProfileBottomSheet(post)
                }
            }
        }

        postButton.setOnClickListener {
            postContentToDatabase(account?.displayName, account?.photoUrl.toString())
        }

        try {
            socket = IO.socket("https://dont-worry.onrender.com", IO.Options().apply {
                query = "userId=$userId"
            })

            socket.connect()

            socket.on(Socket.EVENT_CONNECT) {
                runOnUiThread {
                    Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show()
                }
            }

            socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                runOnUiThread {
                    Toast.makeText(this, "Error connecting to server: ${args[0]}", Toast.LENGTH_SHORT).show()
                }
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                runOnUiThread {
                    Toast.makeText(this, "Disconnected from server", Toast.LENGTH_SHORT).show()
                }
            }

            socket.on("user-status-change") { args ->
                runOnUiThread {
                    val data = args[0] as JSONObject
                    val statusUserId = data.getString("userId")
                    val status = data.getString("status")

                    if (statusUserId == userId) {
                        if (status == "online") {
                            statusIndicator.setBackgroundColor(resources.getColor(R.color.color_one))
                        } else {
                            statusIndicator.setBackgroundColor(resources.getColor(R.color.subtitlecolor))
                        }
                    }
                }
            }

        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Toast.makeText(this, "Connection URI error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    private fun postContentToDatabase(userName: String?, userPhotoUrl: String?) {
        val postContent = editTextPost.text.toString().trim()
        if (postContent.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            val postTime = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(Date(currentTime))

            val postId = databaseReference.push().key ?: return
            val userId = GoogleSignIn.getLastSignedInAccount(this)?.id ?: "unknown"
            val post = Post(userName ?: "Anonymous", userPhotoUrl ?: "", postContent, postTime, userId, postId)

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

    private fun showUserProfileBottomSheet(post: Post) {
        val profileImage = bottomSheetView.findViewById<ImageView>(R.id.profile_image_view)
        val userName = bottomSheetView.findViewById<TextView>(R.id.user_name_text_view)
        val sendMessageButton = bottomSheetView.findViewById<Button>(R.id.send_message_button)

        Glide.with(this).load(post.userPhotoUrl).placeholder(R.drawable.person).into(profileImage)
        userName.text = post.userName


        sendMessageButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }
}