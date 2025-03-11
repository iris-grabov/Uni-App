package com.example.uni.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uni.Adapter.CuriousUsersAdapter
import com.example.uni.Model.User
import com.example.uni.R
import com.example.uni.databinding.FragmentCuriousTogetherBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class CuriousTogetherFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: CuriousUsersAdapter
    private var usersList: MutableList<User> = mutableListOf() // This will hold the list of users

//    private var param1: String? = null
//    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }

        // Initialize RecyclerView and adapter
        userAdapter = CuriousUsersAdapter(usersList)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentCuriousTogetherBinding.inflate(inflater, container, false)
        recyclerView = binding.curiousRecycleView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = userAdapter

        // Query the Firebase database to get the users who satisfy the condition
        fetchUsersWhoCurious()

        return binding.root
    }

    fun getCurrentUserWantToKnow(snapshot: DataSnapshot, currentUserUid: String): HashMap<String, Long>? {
        // Loop through all user records in the snapshot
        for (userSnapshot in snapshot.children) {
            // Check if the uid in the snapshot matches the current user's uid
            if (userSnapshot.child("uid").getValue(String::class.java) == currentUserUid) {
                // This is the current user's record, extract "want_to_know"
                return userSnapshot.child("want_to_know")
                    .getValue(object : GenericTypeIndicator<HashMap<String, Long>>() {})
            }
        }
        // Return null if current user's record is not found
        return null
    }


    private fun fetchUsersWhoCurious() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        if (currentUserUid != null) {
            usersRef.get().addOnSuccessListener { snapshot ->
                var currentUserWantToKnow = getCurrentUserWantToKnow(snapshot, currentUserUid)
                if (currentUserWantToKnow == null) {
                    return@addOnSuccessListener
                }


                val filteredUsers = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)

                    // Extract the 'want_to_know' field from the snapshot dynamically
                    val wantToKnow = userSnapshot.child("want_to_know").getValue(object : GenericTypeIndicator<HashMap<String, Long>>() {})
                    Log.e("User", "blabla")

                    // If the user exists and has a valid 'want_to_know' field
                    if (user != null && wantToKnow != null) {
                        // Check if current user is in their "want_to_know" list AND they are in current user's "want_to_know" list
                        if (wantToKnow.containsKey(currentUserUid) && currentUserWantToKnow.containsKey(user.uid)) {
                            filteredUsers.add(user)
                        }
                    }
                }
                // Update the RecyclerView with the filtered list of users
                usersList.clear()
                usersList.addAll(filteredUsers)
                userAdapter.notifyDataSetChanged()
            }
                .addOnFailureListener { e ->
                    Log.e("UserFragment", "Error fetching chat_with data: ${e.message}")
                }
        }
    }

//    companion object {
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            CuriousTogetherFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}
