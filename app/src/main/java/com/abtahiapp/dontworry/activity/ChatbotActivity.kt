package com.abtahiapp.dontworry.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.adapter.ChatAdapter
import com.abtahiapp.dontworry.utils.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.abtahiapp.dontworry.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatbotActivity : AppCompatActivity() {

        private lateinit var messageInput: EditText
        private lateinit var sendButton: ImageButton
        private lateinit var chatRecyclerView: RecyclerView
        private lateinit var chatAdapter: ChatAdapter
        private val chatMessages = mutableListOf<ChatMessage>()
        private lateinit var photoUrl : String

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_chatbot)

            messageInput = findViewById(R.id.messageInput)
            sendButton = findViewById(R.id.sendButton)
            chatRecyclerView = findViewById(R.id.chatRecyclerView)

            val userId = intent?.getStringExtra("userId") ?: ""
            val username = intent?.getStringExtra("name") ?: ""
            photoUrl = intent?.getStringExtra("photoUrl") ?: ""

            setupRecyclerView()

            sendButton.setOnClickListener {
                val message = messageInput.text.toString()
                if (message.isNotEmpty()) {
                    sendMessage(message)
                    messageInput.text.clear()
                }
            }
        }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages, photoUrl)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter
    }

    private fun sendMessage(message: String) {
        chatMessages.add(ChatMessage("user", message))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        val typingMessage = ChatMessage("bot", isTyping = true)
        chatMessages.add(typingMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        chatRecyclerView.scrollToPosition(chatMessages.size - 1)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = BuildConfig.GEMINI_CHATBOT_API
                )
                val response = generativeModel.generateContent(message)

                withContext(Dispatchers.Main) {
                    chatMessages.removeAt(chatMessages.size - 1)
                    chatAdapter.notifyItemRemoved(chatMessages.size)

                    response.text?.let {
                        chatMessages.add(ChatMessage("bot", it))
                        chatAdapter.notifyItemInserted(chatMessages.size - 1)
                        chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    chatMessages.removeAt(chatMessages.size - 1)
                    chatAdapter.notifyItemRemoved(chatMessages.size)
                    Toast.makeText(this@ChatbotActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}