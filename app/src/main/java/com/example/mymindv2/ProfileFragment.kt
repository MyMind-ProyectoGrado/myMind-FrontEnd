package com.example.mymindv2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mymindv2.databinding.FragmentProfileBinding
import com.example.mymindv2.services.users.AuthService
import com.example.mymindv2.services.users.UserPreferences
import com.example.mymindv2.services.users.UserRemoteService
import com.example.mymindv2.services.visualizations.VisualizationPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private val authService = AuthService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        loadUserData()

        binding.btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnEditAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnPrivacy.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PrivacyFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnAbout.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun loadUserData() {
        val userData = userPreferences.getUserData()
        if (userData != null) {
            binding.tvName.text = userData.name
            binding.tvEmail.text = userData.email

            val profileImageUri = userData.profileImageUri
            if (!profileImageUri.isNullOrEmpty()) {
                Glide.with(this)
                    .load(profileImageUri)
                    .circleCrop()
                    .into(binding.profileImageView)
            } else {
                binding.profileImageView.setImageResource(R.drawable.logo2_mymind)
            }
        }
    }

    private fun logoutUser() {
        // 🧹 Limpiar todos los datos de preferencias
        UserPreferences(requireContext()).clearUserData()
        VisualizationPreferences(requireContext()).clearAll()

        // 🔄 Redirigir a inicio de sesión
        val intent = Intent(requireContext(), SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Cuenta")
            .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> deleteUserAccount() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUserAccount() {
        val accessToken = userPreferences.getAuthToken()
        val userId = userPreferences.getUserId()

        if (!accessToken.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            val remoteService = UserRemoteService("Bearer $accessToken")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    remoteService.deleteUserFromBackend()
                    authService.deleteUser(
                        userId,
                        onSuccess = {
                            UserPreferences(requireContext()).clearUserData()
                            VisualizationPreferences(requireContext()).clearAll()

                            val intent = Intent(requireContext(), SignInActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        },
                        onError = {
                            requireActivity().runOnUiThread {
                                showErrorDialog("Tu cuenta fue eliminada del sistema, pero hubo un error técnico eliminándola de Auth0.")
                            }
                        }
                    )
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        showErrorDialog("No se pudo eliminar tu cuenta. Intenta más tarde.")
                    }
                }
            }
        } else {
            showErrorDialog("No se pudo obtener el identificador del usuario.")
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
