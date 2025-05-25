package com.example.mymindv2

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.mymindv2.databinding.ActivityMainBinding
import com.example.mymindv2.services.users.UserPreferences

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        userPreferences = UserPreferences(this)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 3000) // 3-second delay

    }

    private fun checkUserSession() {
        val userData = userPreferences.getUserData()
        val nextActivity = if (userData?.token != null) {
            HomeActivity::class.java
        } else {
            WelcomeActivity::class.java
        }

        startActivity(Intent(this, nextActivity))
        finish()
    }

}
