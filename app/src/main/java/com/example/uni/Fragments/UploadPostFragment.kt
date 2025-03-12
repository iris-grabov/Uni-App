package com.example.uni.Fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.uni.Model.Post
import com.example.uni.R
import com.example.uni.databinding.FragmentUploadPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UploadPostFragment : Fragment() {

    private lateinit var binding: FragmentUploadPostBinding
    private var imageUri: Uri? = null
    private lateinit var currentPhotoPath: String

    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST_CODE = 2
    private val CAMERA_PERMISSION_CODE = 100

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

        binding.uploadPostBTNCamera.setOnClickListener {
            checkCameraPermission()
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

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile: File? = createImageFile()
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.uni.fileprovider",
                    photoFile
                )
                imageUri = photoURI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Error while creating image file", Toast.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    imageUri = data?.data
                    Glide.with(this).load(imageUri).into(binding.uploadPostIMGPreview)
                }
                CAMERA_REQUEST_CODE -> {
                    imageUri?.let {
                        Glide.with(this).load(it).into(binding.uploadPostIMGPreview)
                    }
                }
            }
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
                        postImage = downloadUrl.toString(),
                        publisher = auth.currentUser?.uid ?: "unknown",
                        caption = caption,
                        likes = mutableMapOf()
                    )

                    database.reference.child("posts").child(postId).setValue(post)
                        .addOnSuccessListener {
                            Log.d("UploadPostFragment", "Post uploaded: $post")
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