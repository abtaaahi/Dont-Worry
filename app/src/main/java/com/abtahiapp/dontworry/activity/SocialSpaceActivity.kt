package com.abtahiapp.dontworry.activity

import android.content.Intent
import android.graphics.drawable.GradientDrawable
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Post
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.SocketManager
import com.abtahiapp.dontworry.adapter.PostAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException

class SocialSpaceActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var editTextPost: EditText
    private lateinit var postButton: Button
    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var statusIndicator: ImageView
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetView: View
    private lateinit var statusTextView: TextView

    private val onlineStatusMap = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_space)

        profileImageView = findViewById(R.id.profile_image)
        editTextPost = findViewById(R.id.edit_text_post)
        postButton = findViewById(R.id.post_button)
        recyclerViewPosts = findViewById(R.id.recycler_view_posts)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_user_profile, null)
        statusIndicator = bottomSheetView.findViewById(R.id.status_indicator)
        statusTextView = bottomSheetView.findViewById(R.id.status_text)

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

        val socketManager = SocketManager.getInstance(this)
        val socket = socketManager.getSocket()

        socket.on("all-user-status") { args ->
            runOnUiThread {
                val statuses = args[0] as JSONArray
                for (i in 0 until statuses.length()) {
                    val statusObject = statuses.getJSONObject(i)
                    val userId = statusObject.getString("user_id")
                    val status = statusObject.getString("status")
                    onlineStatusMap[userId] = status == "online"
                }

                if (bottomSheetDialog.isShowing) {
                    val displayedUserId = bottomSheetView.findViewById<TextView>(R.id.user_name_text_view).tag as? String
                    if (displayedUserId != null) {
                        updateStatusIndicator(onlineStatusMap[displayedUserId]?.let { if (it) "online" else "offline" } ?: "offline")
                    }
                }
            }
        }
    }

    private fun updateStatusIndicator(status: String) {
        val color = if (status == "online") resources.getColor(R.color.green) else resources.getColor(R.color.grey)
        val drawable = ContextCompat.getDrawable(this, R.drawable.circle) as GradientDrawable
        drawable.setColor(color)
        statusIndicator.setImageDrawable(drawable)

        val statusText = if (status == "online") "Active now" else "Offline"
        statusTextView.text = statusText
        statusTextView.setTextColor(color)
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
        userName.tag = post.userId

        sendMessageButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        updateStatusIndicator(onlineStatusMap[post.userId]?.let { if (it) "online" else "offline" } ?: "offline")
    }
}