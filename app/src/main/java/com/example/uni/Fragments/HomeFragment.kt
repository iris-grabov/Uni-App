package com.example.uni.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uni.ChatListActivity
import com.example.uni.Model.Post
import com.example.uni.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("HomeFragment", "onCreateView started") // Log onCreateView start
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val chatIcon = view.findViewById<ImageView>(R.id.chat_icon)
        chatIcon.setOnClickListener {
            Log.d("HomeFragment", "Chat icon clicked") // Log chat icon click
            val intent = Intent(activity, ChatListActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycle_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val posts = mutableListOf<Post>() // Create an empty mutable list.
        postAdapter = PostAdapter(posts) // Initialize the adapter.
        recyclerView.adapter = postAdapter

        retrievePosts() // Call the function to retrieve posts.
        Log.d("HomeFragment", "onCreateView finished") // Log onCreateView finish
        return view
    }

    private fun retrievePosts() {
        Log.d("HomeFragment", "retrievePosts started")
        val postsRef = database.reference.child("posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HomeFragment", "onDataChange started")
                val postsList = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let {
                        postsList.add(it)
                        Log.d("HomeFragment", "Retrieved post: $it")
                    }
                }

                // Sort the postsList by timestamp in descending order (newest first)
                val sortedPostsList = postsList.sortedByDescending { it.timestamp }

                Log.d("HomeFragment", "Posts list size: ${sortedPostsList.size}")
                postAdapter.updatePosts(sortedPostsList)
                Log.d("HomeFragment", "onDataChange finished")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "onCancelled: ${error.message}")
                Log.d("HomeFragment", "onCancelled finished")
            }
        })
        Log.d("HomeFragment", "retrievePosts finished")
    }
}