package com.example.mymindv2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mymindv2.databinding.ActivityHomeBinding
import com.example.mymindv2.services.LoadingStatusManager
import com.example.mymindv2.services.NotificationService
import com.example.mymindv2.services.users.UserPreferences

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val REQUEST_NOTIFICATION_PERMISSION = 1001
    private val REQUEST_MIC_PERMISSION = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        checkNotificationPermission()
        checkMicrophonePermission()
        val userPreferences = UserPreferences(this)
        if (userPreferences.isLoginExpired()) {
            userPreferences.clearUserData()
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        if (savedInstanceState == null) {
            if (!userPreferences.hasSeenInstructions()) {
                supportFragmentManager.setFragmentResultListener(
                    "instructions_completed",
                    this
                ) { _, _ ->
                    userPreferences.setHasSeenInstructions(true)
                    replaceFragment(RecordingFragment())
                }
                replaceFragment(InstructionsFragment())
            } else if (intent?.getBooleanExtra("go_to_specific_report", false) == true) {
                replaceFragment(SpecificReportFragment())
            } else {
                replaceFragment(RecordingFragment())
            }
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

            // Lógica global para botón "Grabación" sin importar en qué fragmento estés
            if (item.itemId == R.id.nav_recording) {
                if (currentFragment is ConfirmationFragment) {
                    currentFragment.showExitConfirmation {
                        if (LoadingStatusManager.isLoading(this)) {
                            replaceFragment(LoadingFragment())
                        } else {
                            replaceFragment(RecordingFragment())
                        }
                    }
                    return@setOnItemSelectedListener false
                }

                if (LoadingStatusManager.isLoading(this)) {
                    if (currentFragment !is LoadingFragment) {
                        replaceFragment(LoadingFragment())
                    }
                } else {
                    if (currentFragment !is RecordingFragment) {
                        replaceFragment(RecordingFragment())
                    }
                }
                return@setOnItemSelectedListener true
            }

            // Para otros ítems del menú
            if (currentFragment is ConfirmationFragment) {
                currentFragment.showExitConfirmation {
                    when (item.itemId) {
                        R.id.nav_reports -> replaceFragment(ReportFragment())
                        R.id.nav_profile -> replaceFragment(ProfileFragment())
                    }
                }
                return@setOnItemSelectedListener false
            }
            when (item.itemId) {
                R.id.nav_reports -> replaceFragment(ReportFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }

            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            } else {
                val userPreferences = UserPreferences(this)
                scheduleDefaultNotification(userPreferences)
            }
        } else {
            val userPreferences = UserPreferences(this)
            scheduleDefaultNotification(userPreferences)
        }
    }

    private fun scheduleDefaultNotification(userPreferences: UserPreferences) {
        val (notificationsEnabled, hour, minute) = userPreferences.getNotificationSettings()

        if (notificationsEnabled) {
            NotificationService.scheduleNotification(this, hour, minute)
        } else {
            NotificationService.cancelNotification(this)
        }
    }

    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_MIC_PERMISSION
            )
        }
    }
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is ConfirmationFragment) {
            currentFragment.showExitConfirmation {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // importante para acceder a extras nuevos

        // Manejar lógica si llega desde notificación
        if (intent?.getBooleanExtra("go_to_specific_report", false) == true) {
            replaceFragment(SpecificReportFragment())
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_MIC_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                // Permiso denegado
                Toast.makeText(this, "Necesitas conceder el permiso de micrófono para grabar.", Toast.LENGTH_LONG).show()
            }
        }
    }

}
