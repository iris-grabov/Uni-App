package com.example.uni.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uni.Model.User
import com.example.uni.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val context: Context,
    private val userList: List<User>, // Renamed 'user' to 'userList'
    private val isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_layut, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.userAppName.text = user.userName // Corrected: user.username
        Picasso.get()
            .load(user.image) // Corrected: user.image
            .placeholder(R.drawable.defualt_profile_icon)
            .into(holder.userImageProfile)

        // Optionally handle button visibility based on isFragment
        if (isFragment) {
            holder.userWantToKnowButton.visibility = View.VISIBLE
        } else {
            holder.userWantToKnowButton.visibility = View.GONE
        }
        holder.userWantToKnowButton.setOnClickListener{

        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAppName: TextView = itemView.findViewById(R.id.user_app_name)
        val userImageProfile: CircleImageView = itemView.findViewById(R.id.user_image)
        val userWantToKnowButton: Button = itemView.findViewById(R.id.want_to_know_button)
    }
}