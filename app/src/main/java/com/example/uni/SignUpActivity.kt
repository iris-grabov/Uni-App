package com.example.uni

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.uni.MainActivity
import com.example.uni.SignInActivity
import com.example.uni.databinding.ActivitySignUpBinding // Import binding class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding // Declare binding variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater) // Initialize binding
        setContentView(binding.root) // Use binding.root as content view

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets -> // Use binding.main
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.signInButton.setOnClickListener { // Use binding.signInButton
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.signUpButton.setOnClickListener { // Use binding.signUpButton
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        val fullName = binding.fullNameSignUp.text.toString()
        val userName = binding.usernameSignUp.text.toString()
        val email = binding.emailSignUp.text.toString()
        val password = binding.passwordSignUp.text.toString()

        Log.d("SignUpActivity", "CreateAccount() called") // Log when account creation starts

        when {
            TextUtils.isEmpty(fullName) -> {
                Toast.makeText(this, "Please enter your full name.", Toast.LENGTH_LONG).show()
                Log.d("SignUpActivity", "Full name is empty")
            }

            TextUtils.isEmpty(userName) -> {
                Toast.makeText(this, "Please enter a username.", Toast.LENGTH_LONG).show()
                Log.d("SignUpActivity", "Username is empty")
            }

            TextUtils.isEmpty(email) -> {
                Toast.makeText(this, "Please enter your email.", Toast.LENGTH_LONG).show()
                Log.d("SignUpActivity", "Email is empty")
            }

            TextUtils.isEmpty(password) -> {
                Toast.makeText(this, "Please enter a password.", Toast.LENGTH_LONG).show()
                Log.d("SignUpActivity", "Password is empty")
            }

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please Wait")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                // All fields are valid, proceed with account creation
                Log.d("SignUpActivity", "All fields are valid. Creating user...")

                val auth: FirebaseAuth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SignUpActivity", "Account created successfully.")
                        updateFcmToken(fullName, userName, email, progressDialog)
                    } else {
                        val message = task.exception?.toString() ?: "Unknown error"
                        Log.e("SignUpActivity", "Error creating account: $message")
                        Toast.makeText(this, "Error $message", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun updateFcmToken(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        Log.d("SignUpActivity", "Fetching FCM token...")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("SignUpActivity", "Failed to get FCM token.")
                saveUserInfo(fullName, userName, email, progressDialog, "")
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("SignUpActivity", "FCM token fetched successfully: $token")
            saveUserInfo(fullName, userName, email, progressDialog, token)
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog, fcmToken: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserID == null) {
            Log.e("SignUpActivity", "Current user ID is null")
            return
        }

        Log.d("SignUpActivity", "Saving user info to the database...")

        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        val userMap = hashMapOf<String, Any>(
            "uid" to currentUserID,
            "fullname" to fullName,
            "userName" to userName,
            "email" to email,
            "bio" to "Loving my Uni",
            "image" to "https://firebasestorage.googleapis.com/v0/b/uni-app-4e078.firebasestorage.app/o/Default%20Images%2Fyouth.png?alt=media&token=a564cb1f-896d-40a1-bb31-7b858c957f8f",
            "fcmToken" to fcmToken
        )

        userRef.child(currentUserID).setValue(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("SignUpActivity", "User info saved successfully.")
                progressDialog.dismiss()
                Toast.makeText(this, "Account created", Toast.LENGTH_LONG).show()
                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                val message = task.exception?.toString() ?: "Unknown error"
                Log.e("SignUpActivity", "Error saving user info: $message")
                FirebaseAuth.getInstance().signOut()
                progressDialog.dismiss()
            }
        }
    }
}
