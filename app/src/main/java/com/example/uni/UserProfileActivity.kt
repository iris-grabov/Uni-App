package com.example.uni

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.ImageView
import com.example.uni.Fragments.UserFragment
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()

        // Get userId from intent
        val userId = intent.getStringExtra("userId")

        // Set up the toolbar and back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressed()  // Handles the back navigation
        }

        // Check if the userId is valid
        if (userId != null) {
            // Check if this is the current user
            val currentUserId = auth.currentUser?.uid

            if (userId == currentUserId) {
                // If it's the current user, we still show the profile
                // The UserFragment will handle showing the edit button instead of "Want to know"
                val userFragment = UserFragment.newInstance(userId)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, userFragment)
                    .commit()
            } else {
                // If it's another user, show their profile
                val userFragment = UserFragment.newInstance(userId)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, userFragment)
                    .commit()
            }
        } else {
            finish()
        }
    }
}
