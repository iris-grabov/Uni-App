package com.example.uni.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uni.Model.User
import de.hdodenhof.circleimageview.CircleImageView
import com.example.uni.R

class CuriousUsersAdapter(private val users: List<User>) : RecyclerView.Adapter<CuriousUsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val usernameText: TextView = itemView.findViewById(R.id.username_text)
        private val userFullName: TextView = itemView.findViewById(R.id.user_full_name)
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)

        fun bind(user: User) {
            usernameText.text = user.userName
            userFullName.text = user.fullname
            // Set the profile image using Glide or Picasso
            Glide.with(itemView.context)
                .load(user.image)
                .placeholder(R.drawable.defualt_profile_icon)
                .into(profileImage)
        }
    }
}

