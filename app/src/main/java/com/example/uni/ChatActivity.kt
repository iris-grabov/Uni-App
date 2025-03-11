package com.example.uni

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uni.Adapter.ChatAdapter
import com.example.uni.Model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var profileImageView: CircleImageView
    private lateinit var usernameTextView: TextView
    private lateinit var fullnameTextView: TextView

    private val messageList = mutableListOf<ChatMessage>()

    private lateinit var database: DatabaseReference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var chatUserId: String? = null

    private var chatId: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        // Initialize views
        profileImageView = findViewById(R.id.profileImageView)
        usernameTextView = findViewById(R.id.usernameTextView)
        fullnameTextView = findViewById(R.id.fullnameTextView)

        recyclerView = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        recyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(messageList)
        recyclerView.adapter = chatAdapter

        // Get chatUserId from intent
        chatUserId = intent.getStringExtra("chatUserId")
        chatUserId?.let {
            chatId = if (currentUserId!! < it) "${currentUserId}_$it" else "${it}_$currentUserId"
            database = FirebaseDatabase.getInstance().getReference("messages").child(chatId!!)

            loadMessages()
        }
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(chatUserId!!)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("userName").getValue(String::class.java)
                    val fullname = snapshot.child("fullname").getValue(String::class.java)
                    val profileImage = snapshot.child("image").getValue(String::class.java)

                    // Set data to UI
                    //usernameTextView.text = username
                    fullnameTextView.text = fullname
                    Glide.with(this@ChatActivity).load(profileImage).into(profileImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })

        // update the chat_with
        val currentUserChatRef = FirebaseDatabase.getInstance().reference.child("users").child(currentUserId!!).child("chat_with")
        currentUserChatRef.get().addOnSuccessListener { snapshot ->
            val currentUserChats = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})?.toMutableList() ?: mutableListOf()

            // Add the new user ID if not already present
            if (!currentUserChats.contains(chatUserId)) {
                currentUserChats.add(chatUserId!!)

                // Update Firebase with the modified chat_with list
                currentUserChatRef.setValue(currentUserChats).addOnSuccessListener {
                    Log.d("UserFragment", "Updated chat_with list for current user.")
                }.addOnFailureListener { e ->
                    Log.e("UserFragment", "Failed to update chat_with: ${e.message}")
                }
            }
        }.addOnFailureListener { e ->
            Log.e("UserFragment", "Error fetching chat_with data: ${e.message}")
        }

        buttonSend.setOnClickListener {
            sendMessage()
        }
        val backButton = findViewById<android.widget.ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // This will navigate back
        }

    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Ensures the activity is closed and navigated back
    }


    private fun loadMessages() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (child in snapshot.children) {
                    val message = child.getValue(ChatMessage::class.java)
                    message?.let { messageList.add(it) }
                }
                chatAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messageList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage() {
        val text = editTextMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            val messageId = database.push().key!! // Generate a unique key for the message
            val message = ChatMessage(currentUserId!!, text, System.currentTimeMillis())

            // Add the message to the correct path under the messages collection
            database.child(messageId).setValue(message)
                .addOnSuccessListener {
                    // Log success
                    Log.d("ChatApp", "Message sent successfully to $chatId")
                }
                .addOnFailureListener { exception ->
                    // Log failure
                    Log.e("ChatApp", "Failed to send message to $chatId", exception)
                }

            // Clear the input field after sending the message
            editTextMessage.text.clear()
        }
    }
}
