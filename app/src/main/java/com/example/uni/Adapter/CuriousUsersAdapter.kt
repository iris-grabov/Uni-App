package com.example.uni.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uni.Model.User
import com.example.uni.R
import com.example.uni.UserProfileActivity
import de.hdodenhof.circleimageview.CircleImageView

class CuriousUsersAdapter(private val users: List<User>, private val context: Context) :
    RecyclerView.Adapter<CuriousUsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, context)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val usernameText: TextView = itemView.findViewById(R.id.username_text)
        private val userFullName: TextView = itemView.findViewById(R.id.user_full_name)
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)

        fun bind(user: User, context: Context) {
            usernameText.text = user.userName
            userFullName.text = user.fullname

            // Load the profile image using Glide
            Glide.with(itemView.context)
                .load(user.image)
                .placeholder(R.drawable.defualt_profile_icon)
                .into(profileImage)

            // Set click listener for profile image and username to open user profile
            val openProfile = View.OnClickListener {
                val intent = Intent(context, UserProfileActivity::class.java)
                intent.putExtra("userId", user.uid) // Pass the user ID to UserProfileActivity
                context.startActivity(intent)
            }

            profileImage.setOnClickListener(openProfile)
            usernameText.setOnClickListener(openProfile)
        }
    }
}
