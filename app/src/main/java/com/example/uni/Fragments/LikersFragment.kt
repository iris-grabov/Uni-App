package com.example.uni.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uni.Adapter.LikersAdapter
import com.example.uni.Model.User
import com.example.uni.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.atomic.AtomicInteger

class LikersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LikersAdapter
    private var userList = mutableListOf<User>()
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_likers, container, false)

        recyclerView = view.findViewById(R.id.notifications_recycle_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext()) // Use requireContext() here
        adapter = LikersAdapter(userList, requireContext()) // Pass context here
        recyclerView.adapter = adapter

        fetchLikers()

        return view
    }



    private fun fetchLikers() {
        currentUserId?.let { userId ->
            database.child("posts").orderByChild("publisher").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userIdSet = HashSet<String>()

                        for (postSnapshot in snapshot.children) {
                            val likesSnapshot = postSnapshot.child("likes")
                            for (userLike in likesSnapshot.children) {
                                userLike.key?.let { userIdSet.add(it) }
                            }
                        }

                        getUserDetails(userIdSet)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun getUserDetails(userIds: Set<String>) {
        val tempList = mutableListOf<User>()
        val count = AtomicInteger(userIds.size)

        for (userId in userIds) {
            database.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue(User::class.java)?.let { tempList.add(it) }
                        if (count.decrementAndGet() == 0) {
                            userList.clear()
                            userList.addAll(tempList)
                            adapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}
