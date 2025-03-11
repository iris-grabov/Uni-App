package com.example.uni

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uni.Adapter.ChatListAdapter
import com.example.uni.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private val userList = mutableListOf<User>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recyclerView = findViewById(R.id.recyclerViewChatList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ChatListAdapter(userList) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatUserId", user.uid)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Handle back button click
        findViewById<ImageButton>(R.id.backButtonsearchButton).setOnClickListener {
            finish() // Close this activity and return
        }

        loadChatList()
    }


    private fun loadChatList() {
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(currentUserId).child("chat_with")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (child in snapshot.children) {
                    val chatUserId = child.getValue(String::class.java)
                    if (chatUserId != null) {
                        fetchUserDetails(chatUserId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatList", "Error loading chat list", error.toException())
            }
        })
    }

    private fun fetchUserDetails(userId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userMap = snapshot.value as? Map<String, Any?>
                if (userMap != null) {
                    val user = User.Builder()
                        .userName(userMap["userName"] as? String ?: "")
                        .fullname(userMap["fullname"] as? String ?: "")
                        .bio(userMap["bio"] as? String ?: "")
                        .image(userMap["image"] as? String ?: "")
                        .uid(userId)
                        .build()

                    userList.add(user)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatList", "Error loading user details", error.toException())
            }
        })
    }
}
