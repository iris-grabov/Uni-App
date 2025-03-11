package com.example.uni.Fragments

import android.app.Activity
import com.example.uni.UserSettingActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uni.Model.User
import com.example.uni.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uni.Adapter.ProfilePostsAdapter
import com.example.uni.ChatActivity
import com.example.uni.Model.Post
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.GenericTypeIndicator
import de.hdodenhof.circleimageview.CircleImageView


class UserFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userNameTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var editButton: Button
    private lateinit var wantToKnowButton: Button
    private lateinit var startChatButton: Button
    private lateinit var profileImage: CircleImageView
    private lateinit var totalPostsTextView: TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var emptyPostsView: LinearLayout
    private lateinit var connectionButtonsLayout: LinearLayout

    private val USER_SETTING_REQUEST_CODE = 1001 // Request code for starting the user setting activity

    private var userId: String? = null
    private var isCurrentUser: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase objects
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Check if userId argument was passed
        userId = arguments?.getString("userId")

        // If userId is null or matches current user, it's the current user's profile
        val currentUserId = firebaseAuth.currentUser?.uid
        isCurrentUser = (userId == null) || (userId == currentUserId)

        // If userId is null but we're logged in, use current user's ID
        if (userId == null && currentUserId != null) {
            userId = currentUserId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        // Initialize views
        userNameTextView = view.findViewById(R.id.user_name)
        fullNameTextView = view.findViewById(R.id.full_user_name)
        bioTextView = view.findViewById(R.id.bio)
        editButton = view.findViewById(R.id.edit_button)
        wantToKnowButton = view.findViewById(R.id.want_to_know_button)
        startChatButton = view.findViewById(R.id.start_chat_button)
        profileImage = view.findViewById(R.id.user_image)
        totalPostsTextView = view.findViewById(R.id.total_posts)
        postsRecyclerView = view.findViewById(R.id.posts_recycler_view)
        emptyPostsView = view.findViewById(R.id.empty_posts)
        connectionButtonsLayout = view.findViewById(R.id.connection_buttons_layout)

        // Set up RecyclerView
        postsRecyclerView.layoutManager = GridLayoutManager(context, 3)

        // Configure view based on whether we're viewing current user or another user
        if (isCurrentUser) {
            // Current user profile - show edit button, hide connection buttons
            editButton.visibility = View.VISIBLE
            connectionButtonsLayout.visibility = View.GONE

            // Set edit button click listener
            editButton.setOnClickListener {
                startActivityForResult(Intent(activity, UserSettingActivity::class.java), USER_SETTING_REQUEST_CODE)
            }
        } else {
            // Other user profile - hide edit button, show connection buttons
            editButton.visibility = View.GONE
            connectionButtonsLayout.visibility = View.VISIBLE

            // Hide chat button initially (will show based on connection status)
            startChatButton.visibility = View.GONE

            // Set up connection buttons
            setupConnectionButtons()
        }

        // Load user data
        loadUserData()

        // Load user posts
        loadUserPosts()

        return view
    }

    private fun loadUserData() {
        val id = userId
        if (id != null) {
            val userRef = firebaseDatabase.reference.child("users").child(id)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            userNameTextView.text = it.userName
                            fullNameTextView.text = it.fullname
                            bioTextView.text = it.bio ?: ""

                            // Load profile image
                            val profileImageUrl = snapshot.child("image").getValue(String::class.java)
                            if (!profileImageUrl.isNullOrEmpty()) {
                                context?.let { ctx ->
                                    Glide.with(ctx)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.defualt_profile_icon)
                                        .into(profileImage)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Log.e("UserFragment", "Failed to get user data: ${error.message}")
                }
            })
        }
    }

    private fun loadUserPosts() {
        val id = userId
        if (id != null) {
            val postsRef = firebaseDatabase.reference.child("posts")

            postsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userPosts = mutableListOf<Post>()

                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        if (post != null && post.publisher == id) {
                            userPosts.add(post)
                        }
                    }

                    // Update post count
                    totalPostsTextView.text = userPosts.size.toString()

                    // Show or hide empty view based on post count
                    if (userPosts.isEmpty()) {
                        postsRecyclerView.visibility = View.GONE
                        emptyPostsView.visibility = View.VISIBLE
                    } else {
                        postsRecyclerView.visibility = View.VISIBLE
                        emptyPostsView.visibility = View.GONE

                        // Set the adapter with user's posts
                        val adapter = ProfilePostsAdapter(userPosts)
                        postsRecyclerView.adapter = adapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserFragment", "Failed to load posts: ${error.message}")
                }
            })
        }
    }

    private fun setupConnectionButtons() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val otherUserId = userId ?: return

        Log.d("UserFragment", "Setting up connection buttons for currentUser: $currentUserId, otherUser: $otherUserId")

        // Instead of using a separate 'connections' node, let's check if users have a mutual 'want_to_know' connection
        // We'll add 'want_to_know' array to each user to track connections

        // Get the current user's data reference
        val currentUserRef = firebaseDatabase.reference.child("users").child(currentUserId)
        // Get the other user's data reference
        val otherUserRef = firebaseDatabase.reference.child("users").child(otherUserId)

        val databaseRef = firebaseDatabase.reference.child("users")
        val currentUserTask = databaseRef.child(currentUserId).get()
        val otherUserTask = databaseRef.child(otherUserId).get()

        Tasks.whenAllSuccess<DataSnapshot>(currentUserTask, otherUserTask)
            .addOnSuccessListener { results ->
                val currentUserSnapshot = results[0] as DataSnapshot
                val otherUserSnapshot = results[1] as DataSnapshot

//                val currentUser = currentUserSnapshot.getValue(User::class.java)
//                val otherUser = otherUserSnapshot.getValue(User::class.java)

//                Log.d("UserFragment", "Fetched users: $currentUser and $otherUser")


                Log.d("UserFragment", "Users data changed. Checking connection status.")

                // Check if current user wants to know the other user
                val currentUserWantsToKnow = currentUserSnapshot
                    .child("want_to_know")
                    .hasChild(otherUserId)

                // Check if other user wants to know the current user
                val otherUserWantsToKnow = otherUserSnapshot
                    .child("want_to_know")
                    .hasChild(currentUserId)

                Log.d("UserFragment", "Connection status: current user wants to know = $currentUserWantsToKnow, other user wants to know = $otherUserWantsToKnow")

                // Update UI based on connection status
                activity?.runOnUiThread {
                    when {
                        currentUserWantsToKnow && otherUserWantsToKnow -> {
                            // Both users want to know each other - show chat option
                            wantToKnowButton.visibility = View.GONE
                            startChatButton.visibility = View.VISIBLE
                            Log.d("UserFragment", "Both users want to know each other. Showing chat button.")
                        }
                        currentUserWantsToKnow -> {
                            // Current user already wants to know, waiting for other user
                            wantToKnowButton.text = "Waiting..."
                            wantToKnowButton.isEnabled = false
                            startChatButton.visibility = View.GONE
                            Log.d("UserFragment", "Current user wants to know. Showing waiting state.")
                        }
                        else -> {
                            // No connection yet
                            wantToKnowButton.text = "Want to know"
                            wantToKnowButton.isEnabled = true
                            startChatButton.visibility = View.GONE
                            Log.d("UserFragment", "No connection yet. Showing Want to know button.")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserFragment", "Failed to fetch users: ${exception.message}")
            }



//        // Setup the connection status check
//        firebaseDatabase.reference.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("UserFragment", "Error checking connections: ${error.message}")
//            }
//        })

        // Set up Want to Know button click listener
        wantToKnowButton.setOnClickListener {
            Log.d("UserFragment", "Want to know button clicked")

            // First check if the user nodes exist
            currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Add the other user to the current user's want_to_know list
                        val timestamp = System.currentTimeMillis()

                        // Set a timestamp value for the connection
                        currentUserRef.child("want_to_know").child(otherUserId).setValue(timestamp)
                            .addOnSuccessListener {
                                Log.d("UserFragment", "Successfully sent connection request")
                                Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show()
                                // Update button state
                                wantToKnowButton.text = "Waiting..."
                                wantToKnowButton.isEnabled = false

                                // If this is a mutual connection, update the chat_with arrays too
                                otherUserRef.child("want_to_know").child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(otherSnapshot: DataSnapshot) {
                                        if (otherSnapshot.exists()) {
                                            // Both users want to know each other, update chat_with arrays
                                            addToChatWith(currentUserId, otherUserId)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("UserFragment", "Error checking other user want_to_know: ${error.message}")
                                    }
                                })
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserFragment", "Error sending request: ${e.message}", e)
                                Toast.makeText(context, "Failed to send request: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.e("UserFragment", "Current user node does not exist")
                        Toast.makeText(context, "Error: User data not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserFragment", "Failed to check user node: ${error.message}")
                    Toast.makeText(context, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Set up Start Chat button click listener
        startChatButton.setOnClickListener {
            // Create chat ID (combination of both user IDs, alphabetically sorted)
            val chatId = if (currentUserId < otherUserId) {
                "${currentUserId}_${otherUserId}"
            } else {
                "${otherUserId}_${currentUserId}"
            }

            val currentUserChatRef = firebaseDatabase.reference.child("users").child(currentUserId).child("chat_with")

            // Retrieve the existing chat_with list
            currentUserChatRef.get().addOnSuccessListener { snapshot ->
                val currentUserChats = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})?.toMutableList() ?: mutableListOf()

                // Add the new user ID if not already present
                if (!currentUserChats.contains(otherUserId)) {
                    currentUserChats.add(otherUserId)

                    // Update Firebase with the modified chat_with list
                    currentUserChatRef.setValue(currentUserChats).addOnSuccessListener {
                        Log.d("UserFragment", "Updated chat_with list for current user.")

                        // Start the chat activity
                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("chatId", chatId)
                        intent.putExtra("chatUserId", otherUserId)
                        startActivity(intent)
                    }.addOnFailureListener { e ->
                        Log.e("UserFragment", "Failed to update chat_with: ${e.message}")
                        Toast.makeText(context, "Failed to start chat: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // The chat is already in the list, just start the chat activity
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("chatId", chatId)
                    intent.putExtra("chatUserId", otherUserId)
                    startActivity(intent)
                }
            }.addOnFailureListener { e ->
                Log.e("UserFragment", "Error fetching chat_with data: ${e.message}")
                Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }

//            Log.d("UserFragment", "Starting chat with ID: $chatId")

//            // Navigate to chat activity
//            val intent = Intent(context, ChatActivity::class.java)
//            intent.putExtra("chatId", chatId)
//            intent.putExtra("chatUserId", otherUserId)
//            startActivity(intent)
        }
    }

    // Helper function to add users to each other's chat_with array
    private fun addToChatWith(userId1: String, userId2: String) {
        val user1Ref = firebaseDatabase.reference.child("users").child(userId1)
        val user2Ref = firebaseDatabase.reference.child("users").child(userId2)

        // Add user2 to user1's chat_with list if not already there
        user1Ref.child("chat_with").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatWith = mutableListOf<String>()

                // Get existing chat_with list
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val existingUserId = childSnapshot.getValue(String::class.java)
                        if (existingUserId != null) {
                            chatWith.add(existingUserId)
                        }
                    }
                }

                // Add user2 if not already in the list
                if (!chatWith.contains(userId2)) {
                    chatWith.add(userId2)
                    user1Ref.child("chat_with").setValue(chatWith)
                        .addOnFailureListener { e ->
                            Log.e("UserFragment", "Error updating chat_with for $userId1: ${e.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserFragment", "Error checking chat_with: ${error.message}")
            }
        })

        // Add user1 to user2's chat_with list if not already there
        user2Ref.child("chat_with").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatWith = mutableListOf<String>()

                // Get existing chat_with list
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val existingUserId = childSnapshot.getValue(String::class.java)
                        if (existingUserId != null) {
                            chatWith.add(existingUserId)
                        }
                    }
                }

                // Add user1 if not already in the list
                if (!chatWith.contains(userId1)) {
                    chatWith.add(userId1)
                    user2Ref.child("chat_with").setValue(chatWith)
                        .addOnFailureListener { e ->
                            Log.e("UserFragment", "Error updating chat_with for $userId2: ${e.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserFragment", "Error checking chat_with: ${error.message}")
            }
        })
    }

    // This is the callback when UserSettingActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == USER_SETTING_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            // Reload user data after returning from UserSettingActivity
            loadUserData()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String? = null): UserFragment {
            return UserFragment().apply {
                arguments = Bundle().apply {
                    if (userId != null) {
                        putString("userId", userId)
                    }
                }
            }
        }
    }
}