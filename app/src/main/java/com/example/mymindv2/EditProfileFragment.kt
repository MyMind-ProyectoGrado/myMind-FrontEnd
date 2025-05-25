package com.example.mymindv2

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mymindv2.databinding.FragmentEditProfileBinding
import com.example.mymindv2.services.users.AuthService
import com.example.mymindv2.services.users.UserPreferences
import com.example.mymindv2.services.users.UserRemoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.UnknownHostException
import com.example.mymindv2.services.users.cloudinary.CloudinaryUploader
import com.example.mymindv2.adapters.FileUtils

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var authService: AuthService
    private lateinit var remoteService: UserRemoteService
    private var selectedImageUri: Uri? = null


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) pickImageLauncher.launch("image/*")
        else showError("Permiso denegado para acceder a las imágenes.")
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).circleCrop().into(binding.profileImageView)
            binding.uploadIcon.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userPreferences = UserPreferences(requireContext())
        authService = AuthService()
        remoteService = UserRemoteService("Bearer ${userPreferences.getAuthToken()}")
        loadUserData()

        binding.imageFrameLayout.setOnClickListener { checkPermissionsAndOpenGallery() }
        binding.saveChangesButton.setOnClickListener { saveChanges() }
        binding.togglePasswordButton.setOnClickListener { togglePasswordVisibility() }

        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            binding.passwordInstructionsLayout.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }


        binding.passwordEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                val password = s.toString()

                binding.tvLength.visibility = if (password.length >= 8) View.GONE else View.VISIBLE
                binding.tvUpper.visibility = if (password.any { it.isUpperCase() }) View.GONE else View.VISIBLE
                binding.tvLower.visibility = if (password.any { it.isLowerCase() }) View.GONE else View.VISIBLE
                binding.tvDigit.visibility = if (password.any { it.isDigit() }) View.GONE else View.VISIBLE
                binding.tvSpecial.visibility = if (password.any { "!@#\$%&*?+-_/".contains(it) }) View.GONE else View.VISIBLE
            }
        })

    }

    private fun loadUserData() {
        binding.nameEditText.setText(userPreferences.getUserName())
        val imageUri = userPreferences.getProfileImage()
        if (imageUri.isNotEmpty()) {
            Glide.with(this).load(Uri.parse(imageUri)).circleCrop().into(binding.profileImageView)
        }
    }

    private fun checkPermissionsAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> pickImageLauncher.launch("image/*")
            shouldShowRequestPermissionRationale(permission) -> showRationaleDialog(permission)
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun showRationaleDialog(permission: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Permiso requerido")
            .setMessage("Se necesita acceso a tus fotos para cambiar la imagen de perfil.")
            .setPositiveButton("Aceptar") { _, _ -> requestPermissionLauncher.launch(permission) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveChanges() {
        val newName = binding.nameEditText.text.toString().trim()
        val newPassword = binding.passwordEditText.text.toString().trim()
        val currentName = userPreferences.getUserName()
        val currentImageUri = userPreferences.getProfileImage()

        val errors = mutableListOf<String>()

        if (newName.isNotEmpty() && !newName.matches(Regex("^[a-zA-Z ]+$"))) {
            errors.add("El nombre no debe contener números ni caracteres especiales.")
        }

        if (newPassword.isNotEmpty() && !newPassword.matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&+\\-_/])[A-Za-z\\d@\$!%*?&+\\-_/]{8,}$"))) {
            errors.add("La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.")
        }

        if (errors.isNotEmpty()) {
            Toast.makeText(requireContext(), errors.joinToString("\n"), Toast.LENGTH_LONG).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            var updated = false

            if (newName.isNotEmpty() && newName != currentName) {
                try {
                    remoteService.updateUserName(newName)
                    userPreferences.saveUserName(newName)
                    updated = true
                } catch (e: Exception) {
                    handlePatchError(e, "nombre")
                    return@launch
                }
            }

            if (selectedImageUri != null && selectedImageUri.toString() != currentImageUri) {
                try {
                    val tempFile = FileUtils.getFileFromUri(requireContext(), selectedImageUri!!)
                    val cloudinaryUrl = CloudinaryUploader.uploadImageToCloudinary(tempFile!!.absolutePath)
                    if (!cloudinaryUrl.isNullOrEmpty()) {
                        remoteService.updateUserProfilePic(cloudinaryUrl)
                        userPreferences.saveProfileImage(cloudinaryUrl)
                        updated = true
                    }
                } catch (e: Exception) {
                    handlePatchError(e, "imagen")
                    return@launch
                }
            }


            if (newPassword.isNotEmpty()) {
                requireActivity().runOnUiThread { showPasswordDialog(newPassword) }
                return@launch
            }

            requireActivity().runOnUiThread {
                val msg = if (updated) "Perfil actualizado" else "No se realizaron cambios"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                navigateToProfileFragment()
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        val token = userPreferences.getAuthToken() ?: return
        authService.updateUserPassword(token, newPassword, {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Contraseña actualizada.", Toast.LENGTH_SHORT).show()
                navigateToProfileFragment()
            }
        }, {
            showError("Error al actualizar contraseña.")
        })
    }

    private fun verifyCurrentPassword(currentPassword: String, newPassword: String) {
        val email = userPreferences.getUserEmail()
        authService.loginUser(email, currentPassword, requireEmailVerified = true, {
            updatePassword(newPassword)
        }, {
            showError("Contraseña actual incorrecta")
        })
    }

    private fun showPasswordDialog(newPassword: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_password, null)
        val input = dialogView.findViewById<EditText>(R.id.etCurrentPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar contraseña")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { _, _ ->
                val current = input.text.toString().trim()
                if (current.isNotEmpty()) {
                    verifyCurrentPassword(current, newPassword)
                } else {
                    showError("Debes ingresar tu contraseña actual.")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navigateToProfileFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun togglePasswordVisibility() {
        val cursor = binding.passwordEditText.selectionStart
        val isHidden = binding.passwordEditText.transformationMethod is PasswordTransformationMethod
        binding.passwordEditText.transformationMethod = if (isHidden) null else PasswordTransformationMethod.getInstance()
        binding.togglePasswordButton.setImageResource(if (isHidden) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24)
        binding.passwordEditText.setSelection(cursor)
    }

    private fun showError(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), "⚠️ $message", Toast.LENGTH_LONG).show()
        }
    }

    private fun handlePatchError(e: Exception, field: String) {
        val message = when (e) {
            is UnknownHostException -> "Sin conexión a Internet. Intenta más tarde."
            is HttpException -> when (e.code()) {
                400 -> "Datos inválidos al actualizar $field. Revisa los campos."
                401 -> "No autorizado. Inicia sesión nuevamente."
                403 -> "Permiso denegado para actualizar $field."
                404 -> "No se encontró el recurso de $field en el servidor."
                500 -> "Error del servidor al actualizar $field. Intenta más tarde."
                else -> "Error inesperado ($field): ${e.code()}"
            }
            else -> "Ocurrió un error al actualizar $field. Intenta más tarde."
        }
        showError(message)
    }

}
