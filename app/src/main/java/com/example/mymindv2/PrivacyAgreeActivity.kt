package com.example.mymindv2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mymindv2.adapters.FileUtils
import com.example.mymindv2.databinding.ActivityPrivacyAgreeBinding
import com.example.mymindv2.services.users.AuthService
import com.example.mymindv2.services.users.UserRemoteService
import com.example.mymindv2.services.users.cloudinary.CloudinaryUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrivacyAgreeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyAgreeBinding
    private var name: String? = null
    private var email: String? = null
    private var password: String? = null
    private var profileImageUri: String? = null
    private var city: String? = null
    private var university: String? = null
    private var career: String? = null
    private var gender: String? = null
    private var personality: String? = null
    private var birthday: String? = null

    private val authService = AuthService()
    private lateinit var userRemoteService: UserRemoteService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyAgreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadTermsAndConditions()

        name = intent.getStringExtra("name")
        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")
        profileImageUri = intent.getStringExtra("profileImageUri")
        city = intent.getStringExtra("city")
        university = intent.getStringExtra("university")
        career = intent.getStringExtra("career")
        gender = intent.getStringExtra("gender")
        personality = intent.getStringExtra("personality")
        birthday = intent.getStringExtra("birthday")

        binding.btnDecline.setOnClickListener {
            Toast.makeText(this, "Has rechazado los términos", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        binding.btnAccept.setOnClickListener {
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    var uploadedUrl = profileImageUri ?: ""
                    if (!profileImageUri.isNullOrEmpty()) {
                        val uri = Uri.parse(profileImageUri)
                        val file = FileUtils.getFileFromUri(this@PrivacyAgreeActivity, uri)
                        uploadedUrl = file?.let { CloudinaryUploader.uploadImageToCloudinary(it.absolutePath) } ?: ""
                    }

                    runOnUiThread {
                        profileImageUri = uploadedUrl
                        Log.d("PrivacyAgreeActivity", profileImageUri.toString())
                        registerUser(email!!, password!!)
                    }
                }

            } else {
                Toast.makeText(this, "Error al recuperar datos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadTermsAndConditions() {
        val inputStream = resources.openRawResource(R.raw.terms)
        val termsText = inputStream.bufferedReader().use { it.readText() }
        binding.tvTerms.text = termsText
    }

    private fun registerUser(email: String, password: String) {
        Log.d("PrivacyAgreeActivity", "🧠 Registrando usuario0...")

        authService.registerUserAndGetId(
            email,
            password,
            onSuccess = { userId, accessToken ->

                Log.d("ACCESS_TOKEN", accessToken)
                runOnUiThread {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                }
                Log.d("PrivacyAgreeActivity", "🧠 Registrando usuario...")

                userRemoteService = UserRemoteService("Bearer $accessToken")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d("PrivacyAgreeActivity", "🧠 Registrando usuario2...")

                        userRemoteService.registerFinalUser(
                            email = email,
                            name = name ?: "",
                            profileImageUri = profileImageUri ?: "",
                            city = city ?: "",
                            university = university ?: "",
                            career = career ?: "",
                            gender = gender ?: "",
                            personality = personality ?: "",
                            birthday = birthday ?: ""
                        )
                        Log.d("PrivacyAgreeActivity", "🧠 Registrando usuario3...")
                        runOnUiThread {
                            Toast.makeText(this@PrivacyAgreeActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@PrivacyAgreeActivity, SuccessActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } catch (e: Exception) {

                        // Eliminar usuario de Auth0 si el backend falla
                        authService.deleteUser(
                            userId = userId,
                            onSuccess = {
                                showErrorToast("Error al guardar tus datos. La cuenta fue eliminada.")
                            },
                            onError = { deleteError ->
                                showErrorToast("Ocurrió un error grave. Contacta soporte.")
                            }
                        )

                        when {
                            e.message?.contains("400") == true -> {
                                showErrorToast("Error de validación. Revisa tus datos.")
                            }
                            e.message?.contains("401") == true -> {
                                showErrorToast("Tu sesión ha expirado. Vuelve a iniciar sesión.")
                            }
                            e.message?.contains("403") == true -> {
                                showErrorToast("No autorizado para registrar usuario.")
                            }
                            e.message?.contains("timeout") == true || e.message?.contains("Unable to resolve host") == true -> {
                                showErrorToast("Sin conexión. Intenta nuevamente cuando tengas Internet.")
                            }
                            else -> {
                                showErrorToast("Error al guardar los datos. Intenta más tarde.")
                            }
                        }
                    }
                }
            },
            onError = { errorMessage ->

                when {
                    "400" in errorMessage || "invalid" in errorMessage.lowercase() -> {
                        showErrorToast("Correo inválido o ya registrado.")
                    }
                    "401" in errorMessage || "unauthorized" in errorMessage.lowercase() -> {
                        showErrorToast("Autenticación fallida. Verifica tus credenciales.")
                    }
                    "403" in errorMessage -> {
                        showErrorToast("No tienes permisos para realizar esta acción.")
                    }
                    "timeout" in errorMessage.lowercase() || "connection" in errorMessage.lowercase() -> {
                        showErrorToast("No se pudo conectar. Revisa tu conexión a Internet.")
                    }
                    else -> {
                        showErrorToast("Ocurrió un error al registrar el usuario. Intenta más tarde.")
                    }
                }
            }
        )
    }

    private fun showErrorToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, "⚠️ $message", Toast.LENGTH_LONG).show()
        }
    }
}
