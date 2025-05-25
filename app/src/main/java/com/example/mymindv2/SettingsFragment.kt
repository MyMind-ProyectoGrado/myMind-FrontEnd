package com.example.mymindv2

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mymindv2.databinding.FragmentSettingsBinding
import com.example.mymindv2.services.NotificationService
import com.example.mymindv2.services.users.UserPreferences
import com.example.mymindv2.services.users.UserRemoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences // Usar UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.visibility = View.INVISIBLE
        binding.root.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(300).start()
        }


        userPreferences = UserPreferences(requireContext()) // Inicializar UserPreferences
       // setupSpinner()
        loadSettings()

        binding.saveSettingsButton.setOnClickListener {
            saveSettings()
        }

    }

    private fun applyThemeColor(colorModeIndex: Int) {
        val backgroundDrawable = when (colorModeIndex) {
            0 -> R.drawable.background_gradient_white // Light mode
            else -> R.drawable.background_gradient_white
        }
        binding.root.background = ContextCompat.getDrawable(requireContext(), backgroundDrawable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveSettings() {
        val notificationsEnabled = binding.switchNotifications.isChecked
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteService = UserRemoteService("Bearer ${userPreferences.getAuthToken()}")

                val (_, backendNotificationState) = remoteService.fetchNotificationSettings()

                if (notificationsEnabled != backendNotificationState) {
                    // Solo si cambia, enviar PATCH
                    remoteService.toggleNotificationSettings()
                }

                userPreferences.saveNotificationSettings(notificationsEnabled, hour, minute)

                requireActivity().runOnUiThread {
                    if (notificationsEnabled) {
                        NotificationService.scheduleNotification(requireContext(), hour, minute)
                    } else {
                        NotificationService.cancelNotification(requireContext())
                    }
                    navigateToProfileFragment()
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showErrorDialog("Error al guardar notificaciones: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteService = UserRemoteService("Bearer ${userPreferences.getAuthToken()}")
                val (_, backendNotificationState) = remoteService.fetchNotificationSettings()

                requireActivity().runOnUiThread {
                    binding.switchNotifications.isChecked = backendNotificationState
                    val (_, hour, minute) = userPreferences.getNotificationSettings()
                    binding.timePicker.apply {
                        setIs24HourView(true)
                        this.hour = hour
                        this.minute = minute
                    }
                }
            } catch (e: Exception) {
                // Si falla backend, carga lo que está local
                requireActivity().runOnUiThread {
                    val (notificationsEnabled, hour, minute) = userPreferences.getNotificationSettings()
                    binding.switchNotifications.isChecked = notificationsEnabled
                    binding.timePicker.apply {
                        setIs24HourView(true)
                        this.hour = hour
                        this.minute = minute
                    }
                    showErrorDialog("No se pudo consultar el estado de las notificaciones. Se usarán los datos locales.")
                }
            }
        }
        binding.root.visibility = View.VISIBLE
    }


    private fun navigateToProfileFragment() {
        if (isAdded) { // Check if fragment is still active
            requireActivity().runOnUiThread {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            Log.e("SettingsFragment", "Fragment is no longer available")
        }
    }
}
