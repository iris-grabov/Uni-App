package com.example.uni.Fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.uni.Model.Post
import com.example.uni.R
import com.example.uni.databinding.FragmentUploadPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class UploadPostFragment : Fragment() {

    private lateinit var binding: FragmentUploadPostBinding
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadPostBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.uploadPostBTNSelect.setOnClickListener {
            selectImage()
        }

        binding.uploadPostBTNUpload.setOnClickListener {
            uploadPost()
        }

        return binding.root
    }

    private fun selectImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(binding.uploadPostIMGPreview)
        }
    }

    private fun uploadPost() {
        val caption = binding.uploadPostEditCaption.text.toString().trim()
        if (imageUri == null || caption.isEmpty()) {
            Toast.makeText(context, "Please select an image and add a caption", Toast.LENGTH_SHORT).show()
            return
        }

        val postId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("posts/$postId.jpg")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val post = Post(
                        postId = postId,
                        postImage = downloadUrl.toString(), // Store the download URL
                        publisher = auth.currentUser?.uid ?: "unknown",
                        caption = caption,
                        likes = mutableMapOf()
                    )

                    database.reference.child("posts").child(postId).setValue(post)
                        .addOnSuccessListener {
                            Log.d("UploadPostFragment", "Post uploaded: $post") // Add this line
                            Toast.makeText(context, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                            clearFields()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to upload post to database", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload image to storage", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        imageUri = null
        binding.uploadPostIMGPreview.setImageResource(R.drawable.camera_icon)
        binding.uploadPostEditCaption.text?.clear()
    }
}