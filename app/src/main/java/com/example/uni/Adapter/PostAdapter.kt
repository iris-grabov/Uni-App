package com.example.uni.Fragments;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.uni.Model.Post;
import com.example.uni.R;
import com.example.uni.UserProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;

class PostAdapter(private var posts: MutableList<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance();
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance();

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile);
        val userName: TextView = itemView.findViewById(R.id.user_app_name);
        val postImage: ImageView = itemView.findViewById(R.id.picture_post);
        val caption: TextView = itemView.findViewById(R.id.post_caption);
        val likesCount: TextView = itemView.findViewById(R.id.likes_number);
        val likeButton: ImageView = itemView.findViewById(R.id.like_button);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false);
        return PostViewHolder(view);
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position] ?: run {
            Log.e("PostAdapter", "Post at position $position is null");
            return;
        };

        try {
            holder.caption.text = post.caption;
            holder.likesCount.text = post.likesCount.toString();
            loadUserName(post.publisher, holder.userName);

            // Set click listeners for user profile elements to always navigate to profile
            holder.userName.setOnClickListener {
                navigateToUserProfile(it, post.publisher)
            }

            holder.userProfileImage.setOnClickListener {
                navigateToUserProfile(it, post.publisher)
            }

            if (post.postImage != null && post.postImage.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(post.postImage)
                    .into(holder.postImage);
            } else {
                Log.w("PostAdapter", "Post image URL is null or empty for post ${post.postId}");
                holder.postImage.setImageResource(R.drawable.camera_icon);
            }

            loadUserProfileImage(holder.userProfileImage, post.publisher);

            holder.likeButton.setImageResource(if (post.likes.containsKey(auth.currentUser?.uid)) R.drawable.heart_icon else R.drawable.empty_heart_icon);

            holder.likeButton.setOnClickListener {
                toggleLike(post, holder);
            };

            Log.d("PostAdapter", "Bound post ${post.postId} at position $position");
        } catch (e: Exception) {
            Log.e("PostAdapter", "Error binding post at position $position: ${e.message}", e);
        }
    }

    private fun navigateToUserProfile(view: View, userId: String) {
        val context = view.context

        // Always navigate to the profile page, passing the userId
        val intent = Intent(context, UserProfileActivity::class.java)
        intent.putExtra("userId", userId)
        context.startActivity(intent)
    }

    private fun loadUserProfileImage(imageView: CircleImageView, userId: String) {
        if (userId.isEmpty()) {
            Log.w("PostAdapter", "User ID is empty, cannot load profile image");
            imageView.setImageResource(R.drawable.defualt_profile_icon);
            return;
        }

        val userRef = database.reference.child("users").child(userId);
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val profileImageUrl = snapshot.child("image").getValue(String::class.java);
                    if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                        Glide.with(imageView.context).load(profileImageUrl).into(imageView);
                    } else {
                        Log.w("PostAdapter", "Profile image URL is null or empty for user $userId");
                        imageView.setImageResource(R.drawable.defualt_profile_icon);
                    }
                } else {
                    Log.w("PostAdapter", "User $userId does not exist");
                    imageView.setImageResource(R.drawable.defualt_profile_icon);
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Error loading profile image for user $userId: ${error.message}");
                imageView.setImageResource(R.drawable.defualt_profile_icon);
            }
        });
    }

    private fun toggleLike(post: Post, holder: PostViewHolder) {
        val userId = auth.currentUser?.uid;
        if (userId == null) {
            Log.w("PostAdapter", "User is not authenticated, cannot toggle like");
            return;
        }

        val postRef = database.reference.child("posts").child(post.postId);

        if (post.likes.containsKey(userId)) {
            postRef.child("likes").child(userId).removeValue();
        } else {
            postRef.child("likes").child(userId).setValue(true);
        }

        holder.likeButton.setImageResource(if (post.likes.containsKey(userId)) R.drawable.heart_icon else R.drawable.empty_heart_icon);
        holder.likesCount.text = post.likesCount.toString();

        Log.d("PostAdapter", "Toggled like for post ${post.postId} by user $userId");
    }

    fun updatePosts(newPosts: List<Post>) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    override fun getItemCount() = posts.size;

    private fun loadUserName(userId: String, textView: TextView) {
        val userRef = database.reference.child("users").child(userId).child("fullname")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)
                textView.text = username ?: "Unknown User"  // Fallback if username is missing
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Failed to fetch username: ${error.message}")
                textView.text = "Unknown User"  // Display fallback text
            }
        })
    }
}