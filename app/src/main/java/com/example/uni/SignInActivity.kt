package com.example.uni

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.uni.databinding.ActivitySignInBinding // Import binding class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding // Declare binding variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater) // Initialize binding
        setContentView(binding.root) // Use binding.root as content view

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets -> // Use binding.main
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.signUpButton.setOnClickListener { // Use binding.signUpButton
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener { // Use binding.loginButton
            logInUser()
        }
    }

    private fun logInUser() {
        val email = binding.emailLogin.text.toString() // Use binding.emailSignIn
        val password = binding.passwordLogin.text.toString() // Use binding.passwordSignIn
        // ... your login logic ...

        when
        {
            TextUtils.isEmpty(email) -> {
                Toast.makeText(this, "Please enter your email.", Toast.LENGTH_LONG).show()

            }

            TextUtils.isEmpty(password) -> {
                Toast.makeText(this, "Please enter a password.", Toast.LENGTH_LONG).show()

            }
            else ->
            {
                val progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("SighUp")
                progressDialog.setMessage("Please Wait")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val auth : FirebaseAuth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task->
                    if(task.isSuccessful)
                    {
                        progressDialog.dismiss()

                        updateFcmToken()
                    }
                    else
                    {
                        val message = task.exception !! .toString ()
                        Toast.makeText(this, "Error $message", Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                goToMainActivity()
                return@addOnCompleteListener
            }

            val token = task.result
            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

            // Save the FCM token in Firebase Realtime Database
            val database = FirebaseDatabase.getInstance().reference
            database.child("users").child(currentUserId).child("fcmToken").setValue(token)
            goToMainActivity()
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}