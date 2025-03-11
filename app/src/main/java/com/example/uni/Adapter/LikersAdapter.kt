package com.example.uni.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uni.Model.User
import com.example.uni.R
import com.example.uni.UserProfileActivity

class LikersAdapter(private val userList: List<User>, private val context: Context) :
    RecyclerView.Adapter<LikersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val username: TextView = itemView.findViewById(R.id.username_text)
        val fullName: TextView = itemView.findViewById(R.id.user_full_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.username.text = user.userName
        holder.fullName.text = user.fullname

        // Load the profile image using Glide
        if (user.image.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(user.image).into(holder.profileImage)
        }

        // Set up click listener for profile image and username
        val openProfile = View.OnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("userId", user.uid) // Pass the user ID to the UserProfileActivity
            context.startActivity(intent)
        }

        // Set click listeners
        holder.profileImage.setOnClickListener(openProfile)
        holder.username.setOnClickListener(openProfile)
    }

    override fun getItemCount(): Int = userList.size
}
