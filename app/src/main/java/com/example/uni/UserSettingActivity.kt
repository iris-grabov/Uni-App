package com.example.uni

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.uni.Model.User
import com.example.uni.databinding.ActivityUserSettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.io.ByteArrayOutputStream

class UserSettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserSettingBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private var imageUri: Uri? = null
    private var currentUser: User? = null
    private var isImageChanged = false

    // Gallery picker launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Launch the cropper with the selected image
            launchImageCropper(it)
        }
    }

    // Define the cropImage contract using the modern ActivityResult API
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Get the cropped image Uri
            imageUri = result.uriContent

            // Mark that the image has been changed
            isImageChanged = true

            // Display the cropped image in the ImageView
            binding.userImage.setImageURI(imageUri)

            // Optionally, you can add a toast to indicate success
            Toast.makeText(this, "Image cropped successfully!", Toast.LENGTH_SHORT).show()

            // Optionally, you can add logging
            Log.d("UserSettingActivity", "Image cropped successfully! URI: $imageUri")
        } else {
            // Handle crop error
            val exception = result.error

            // More specific error handling (optional)
            when (exception) {
                is OutOfMemoryError -> {
                    Toast.makeText(this, "Image too large to crop", Toast.LENGTH_SHORT).show()
                    Log.e("UserSettingActivity", "Image cropping failed: OutOfMemoryError")
                }
                else -> {
                    Toast.makeText(this, "Image cropping failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserSettingActivity", "Image cropping failed: ${exception?.message}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUserSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        // Load current user info
        loadUserInfo()

        // Set up click listeners
        binding.changeImageProfile.setOnClickListener {
            openGallery()  // First open gallery to select an image
        }

        binding.userImage.setOnClickListener {
            openGallery()  // Also allow clicking the image itself
        }

        binding.saveUserEdit.setOnClickListener {
            updateUserInfo()
        }

        binding.closeUserEdit.setOnClickListener {
            // Close the activity without saving changes
            finish()
        }

        // Logout button
        binding.logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@UserSettingActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserInfo() {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId != null) {
            val userRef = firebaseDatabase.reference.child("users").child(currentUserId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.getValue(User::class.java)
                        currentUser = userData

                        // Populate UI with user data
                        binding.userFullName.setText(userData?.fullname)
                        binding.userAppName.setText(userData?.userName)
                        binding.userBio.setText(userData?.bio)

                        // Load profile image
                        if (userData?.image?.isNotEmpty() == true) {
                            Glide.with(this@UserSettingActivity)
                                .load(userData.image)
                                .placeholder(R.drawable.defualt_profile_icon)
                                .into(binding.userImage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserSettingActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun launchImageCropper(sourceUri: Uri) {
        // Create crop options with your desired settings
        val options = CropImageOptions().apply {
            // Customize your crop options here (e.g., aspect ratio, fix aspect ratio, etc.)
            aspectRatioX = 1
            aspectRatioY = 1
            fixAspectRatio = true
            cropShape = CropImageView.CropShape.OVAL
            outputCompressFormat = Bitmap.CompressFormat.JPEG
            outputCompressQuality = 90
            guidelines = CropImageView.Guidelines.ON
        }

        // Launch image cropping activity using the CanHub Cropper library with the specified options
        cropImage.launch(CropImageContractOptions(sourceUri, options))
    }

    private fun updateUserInfo() {
        val fullname = binding.userFullName.text.toString().trim()
        val username = binding.userAppName.text.toString().trim()
        val bio = binding.userBio.text.toString().trim()

        if (fullname.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Name and username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Show a loading indicator
        Toast.makeText(this, "Updating profile...", Toast.LENGTH_SHORT).show()

        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        if (isImageChanged && imageUri != null) {
            // Create a more detailed log to debug
            Log.d("UserSettingActivity", "Starting image upload to Firebase Storage: $imageUri")

            val fileRef = firebaseStorage.reference.child("ProfileImages").child("$currentUserId.jpg")

            // First, try to compress the image to reduce upload size and potential issues
            try {
                val inputStream = contentResolver.openInputStream(imageUri!!)
                inputStream?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    val data = baos.toByteArray()

                    // Upload compressed image
                    val uploadTask = fileRef.putBytes(data)
                    uploadTask.addOnSuccessListener {
                        Log.d("UserSettingActivity", "Image upload successful")
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            Log.d("UserSettingActivity", "Download URL retrieved: $uri")
                            saveUserToDatabase(currentUserId, fullname, username, bio, uri.toString())
                        }.addOnFailureListener { e ->
                            Log.e("UserSettingActivity", "Failed to get download URL", e)
                            Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()

                            // Even if getting URL fails, still update other user information
                            saveUserToDatabase(currentUserId, fullname, username, bio, currentUser?.image ?: "")
                        }
                    }.addOnFailureListener { e ->
                        Log.e("UserSettingActivity", "Image upload failed", e)
                        Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()

                        // Even if image upload fails, still update other user information
                        saveUserToDatabase(currentUserId, fullname, username, bio, currentUser?.image ?: "")
                    }
                } ?: run {
                    // Handle null inputStream
                    Toast.makeText(this, "Could not open image file", Toast.LENGTH_SHORT).show()
                    saveUserToDatabase(currentUserId, fullname, username, bio, currentUser?.image ?: "")
                }
            } catch (e: Exception) {
                Log.e("UserSettingActivity", "Error processing image", e)
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()

                // Fall back to updating user info without the image
                saveUserToDatabase(currentUserId, fullname, username, bio, currentUser?.image ?: "")
            }
        } else {
            // No image change, just update the other user info
            saveUserToDatabase(currentUserId, fullname, username, bio, currentUser?.image ?: "")
        }
    }

    private fun saveUserToDatabase(uid: String, fullname: String, username: String, bio: String, imageUrl: String) {
        val userRef = firebaseDatabase.reference.child("users").child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val existingUser = snapshot.getValue(User::class.java)
                    val fcmToken = existingUser?.fcmToken ?: "" // Preserve the FCM token

                    val updatedUser = mapOf(
                        "fullname" to fullname,
                        "userName" to username,
                        "bio" to bio,
                        "image" to imageUrl,
                        "fcmToken" to fcmToken // Ensure the token remains unchanged
                    )

                    userRef.updateChildren(updatedUser)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@UserSettingActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK) // Set result OK so UserFragment knows to refresh
                                finish()
                            } else {
                                Toast.makeText(this@UserSettingActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserSettingActivity, "Error retrieving user data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}