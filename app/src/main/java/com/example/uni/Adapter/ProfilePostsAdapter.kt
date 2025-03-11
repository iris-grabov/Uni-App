package com.example.uni.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uni.Model.Post
import com.example.uni.R

class ProfilePostsAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<ProfilePostsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImage: ImageView = view.findViewById(R.id.post_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]

        // Load the post image
        if (post.postImage != null && post.postImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.postImage)
                .placeholder(R.drawable.camera_icon)
                .into(holder.postImage)
        } else {
            holder.postImage.setImageResource(R.drawable.camera_icon)
        }
    }

    override fun getItemCount(): Int = posts.size
}