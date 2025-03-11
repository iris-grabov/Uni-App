package com.example.uni

import com.example.uni.Fragments.UserFragment
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.uni.Fragments.CuriousTogetherFragment
import com.example.uni.Fragments.HomeFragment
import com.example.uni.Fragments.LikersFragment
import com.example.uni.Fragments.UploadPostFragment

//import com.example.uni.Fragments.UploadNewPostFragmentTry

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        Log.d("MainActivity", "Navigation item selected: ${item.itemId}")
        when (item.itemId) {
            R.id.navigation_home -> {
                Log.d("MainActivity", "Navigating to HomeFragment")
                goToFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_likes -> {
                Log.d("MainActivity", "Navigating to NotificationFragment")
                goToFragment(LikersFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_add_post -> {
                Log.d("MainActivity", "Navigating to UploadNewPostFragment")
                goToFragment(UploadPostFragment()) // Assuming you have this Fragment
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_user -> {
                Log.d("MainActivity", "Navigating to UserFragment")
                goToFragment(UserFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_together -> {
                Log.d("MainActivity", "Navigating to CuriousTogetherFragment")
                goToFragment(CuriousTogetherFragment())
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Log for onCreate to verify activity creation
        Log.d("MainActivity", "Activity created")

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // Log to check initial fragment
        Log.d("MainActivity", "Initial fragment is HomeFragment")
        goToFragment(HomeFragment())

        // Permission check for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Requesting POST_NOTIFICATIONS permission")
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun goToFragment(fragment: Fragment) {
        // Log to check which fragment is being navigated to
        Log.d("MainActivity", "Navigating to fragment: ${fragment.javaClass.simpleName}")

        val fragmentT = supportFragmentManager.beginTransaction()
        fragmentT.replace(R.id.fragment, fragment)
        fragmentT.commit()
    }
}
