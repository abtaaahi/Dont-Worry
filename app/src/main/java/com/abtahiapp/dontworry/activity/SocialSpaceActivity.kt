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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.utils.Post
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.SocketManager
import com.abtahiapp.dontworry.adapter.PostAdapter
import com.abtahiapp.dontworry.utils.BaseUrls
import com.abtahiapp.dontworry.utils.InfoBottomSheetDialog
import com.abtahiapp.dontworry.utils.NetworkUtil.isOnline
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

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

        val backButton: ImageButton = findViewById(R.id.back)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val infoButton = findViewById<ImageButton>(R.id.infoButton)
        infoButton.setOnClickListener {
            val infoMessage = getString(R.string.social_space_info_message).trimIndent()
            val infoBottomSheetDialog = InfoBottomSheetDialog(this, infoMessage)
            infoBottomSheetDialog.show()
        }

        postButton.visibility = Button.GONE

        editTextPost.setOnTouchListener { _, _ ->
            editTextPost.isCursorVisible = true
            false
        }

        editTextPost.setOnFocusChangeListener { _, hasFocus ->
            editTextPost.isCursorVisible = hasFocus
        }

        editTextPost.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                postButton.visibility = if (s.isNullOrEmpty()) Button.GONE else Button.VISIBLE
            }
        })

        recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(mutableListOf())
        recyclerViewPosts.adapter = postAdapter

        checkAndUpdatePosts(account?.displayName, account?.photoUrl.toString())

        if (account != null) {
            postAdapter.setOnProfileImageClickListener { post ->
                val currentUser = GoogleSignIn.getLastSignedInAccount(this)?.id
                if (post.userId == currentUser) {
                    val intent = Intent(this, MyProfileActivity::class.java)
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
            if (isOnline(this)) {
                postContentToDatabase(account?.displayName, account?.photoUrl.toString())
            } else {
                Toast.makeText(this, "You are offline. Cannot post at this time.", Toast.LENGTH_SHORT).show()
            }
        }

        val socketManager = SocketManager.getInstance(this)
        val socket = socketManager.getSocket()

        socket.on("user-status-change") { args ->
            runOnUiThread {
                try {
                    val data = args[0] as JSONObject
                    val statusUserId = data.getString("userId")
                    val status = data.getString("status")
                    onlineStatusMap[statusUserId] = status == "online"

                    if (bottomSheetDialog.isShowing) {
                        val displayedUserId = bottomSheetView.findViewById<TextView>(R.id.user_name_text_view).tag as? String
                        if (statusUserId == displayedUserId) {
                            updateStatusIndicator(status)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SocialSpaceActivity", "Error processing user status: ${e.message}")
                }
            }
        }

        socket.on("all-user-status") { args ->
            runOnUiThread {
                try {
                    val dataArray = args[0] as JSONArray
                    for (i in 0 until dataArray.length()) {
                        val data = dataArray.getJSONObject(i)
                        val statusUserId = data.getString("user_id")
                        val status = data.getString("status")
                        onlineStatusMap[statusUserId] = status == "online"

                        if (bottomSheetDialog.isShowing) {
                            val displayedUserId = bottomSheetView.findViewById<TextView>(R.id.user_name_text_view).tag as? String
                            if (statusUserId == displayedUserId) {
                                updateStatusIndicator(status)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SocialSpaceActivity", "Error processing all user statuses: ${e.message}")
                }
            }
        }
    }

    private fun checkAndUpdatePosts(userName: String?, userPhotoUrl: String?) {
        if (isOnline(this)) {
            fetchPostsFromFirebase()
        } else {
            Toast.makeText(this, "You are offline", Toast.LENGTH_SHORT).show()
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
            val email = GoogleSignIn.getLastSignedInAccount(this)?.email ?: "unknown"
            val post = Post(userName ?: "Anonymous", userPhotoUrl ?: "", postContent, postTime, userId, postId, email)
            databaseReference.child(postId).setValue(post).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    editTextPost.text.clear()
                    postButton.visibility = Button.GONE
                }
            }
        }
    }

    private fun fetchPostsFromFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("social_posts")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        posts.add(post)
                    }
                }
                postAdapter.updatePosts(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SocialSpaceActivity, "Failed to fetch posts", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUserProfileBottomSheet(post: Post) {
        val profileImage = bottomSheetView.findViewById<ImageView>(R.id.profile_image_view)
        val userName = bottomSheetView.findViewById<TextView>(R.id.user_name_text_view)
        val connectButton = bottomSheetView.findViewById<Button>(R.id.connect)
        val userNameCurrent = GoogleSignIn.getLastSignedInAccount(this)?.displayName
        val postUserEmail = post.email // Receiver Email
        val userEmail = GoogleSignIn.getLastSignedInAccount(this)?.email // Sender Email
        val photoURL = GoogleSignIn.getLastSignedInAccount(this)?.photoUrl.toString() // Sender Profile Photo

        //Toast.makeText(this@SocialSpaceActivity, "$postUserEmail\n$userEmail\n$userNameCurrent", Toast.LENGTH_SHORT).show()

        Glide.with(this).load(post.userPhotoUrl).placeholder(R.drawable.person).into(profileImage)
        userName.text = post.userName
        userName.tag = post.userId
        connectButton.setOnClickListener {
            if (userNameCurrent != null) {
                sendEmail(postUserEmail, userEmail, photoURL, userNameCurrent)
            }
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        updateStatusIndicator(onlineStatusMap[post.userId]?.let { if (it) "online" else "offline" } ?: "offline")
    }

    private fun sendEmail(receiverEmail: String, senderEmail: String?, senderPhotoUrl: String, senderName: String) {
        val emailData = JSONObject().apply {
            put("senderName", senderName)
            put("senderEmail", senderEmail)
            put("receiverEmail", receiverEmail)
            put("senderPhotoUrl", senderPhotoUrl)
        }

        val client = OkHttpClient()

        val requestBody = emailData.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${BaseUrls.BASE_URL_SOCIAL_SPACE}/send-email")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Email Error", "Failed to send email: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@SocialSpaceActivity, "Failed to send email", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SocialSpaceActivity, "Email sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("Email Error", "Error sending email: ${response.message}")
                        Toast.makeText(this@SocialSpaceActivity, "Error sending email", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}