package com.example.mymindv2

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.TypedValue
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mymindv2.databinding.ActivitySignInBinding
import com.example.mymindv2.services.users.AuthService
import com.example.mymindv2.services.users.UserPreferences
import com.example.mymindv2.services.users.UserRemoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.mymindv2.services.visualizations.VisualizationRemoteService
import com.example.mymindv2.services.visualizations.VisualizationPreferences
import androidx.appcompat.app.AlertDialog


class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val authService = AuthService()
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        binding.togglePasswordButton.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.forgotPassword.setOnClickListener {
            val input = EditText(this).apply {
                hint = "Correo electrónico"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }

            val container = FrameLayout(this).apply {
                val marginInDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16f,
                    resources.displayMetrics
                ).toInt()

                setPadding(marginInDp, 0, marginInDp, 0)
                addView(input)
            }

            AlertDialog.Builder(this)
                .setTitle("Recuperar contraseña")
                .setMessage("Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña.")
                .setView(container)
                .setPositiveButton("Enviar") { _, _ ->
                    val email = input.text.toString().trim()
                    if (email.isNotEmpty()) {
                        authService.sendResetPasswordEmail(
                            email = email,
                            onSuccess = {
                                runOnUiThread {
                                    AlertDialog.Builder(this)
                                        .setTitle("Correo enviado")
                                        .setMessage("Se ha enviado un correo con instrucciones para restablecer tu contraseña.")
                                        .setPositiveButton("Aceptar", null)
                                        .show()
                                }
                            },
                            onError = { error ->
                                runOnUiThread {
                                    Toast.makeText(this, "⚠️ $error", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    } else {
                        Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun togglePasswordVisibility() {
        if (binding.passwordEditText.transformationMethod is PasswordTransformationMethod) {
            binding.passwordEditText.transformationMethod = null
            binding.togglePasswordButton.setImageResource(R.drawable.baseline_visibility_24)
        } else {
            binding.passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.togglePasswordButton.setImageResource(R.drawable.baseline_visibility_off_24)
        }
        binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
    }

    private fun loginUser(email: String, password: String) {
        authService.loginUser(
            email,
            password,
            requireEmailVerified = true,
            onSuccess = { accessToken ->
                Log.d("SignInActivity", "✅ Login exitoso. AccessToken obtenido. $accessToken")
                userPreferences.saveAuthToken(accessToken)

                authService.getUserId(
                    accessToken,
                    onSuccess = { userId ->
                        Log.d("SignInActivity", "✅ UserId obtenido: $userId")
                        userPreferences.saveUserId(userId)

                        val userRemoteService = UserRemoteService("Bearer $accessToken")

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                Log.d("SignInActivity", "⏳ Cargando perfil...")
                                userRemoteService.fetchAndSaveUserProfile(userPreferences)

                                val visualizationPrefs = VisualizationPreferences(this@SignInActivity)
                                val visualizationRemote = VisualizationRemoteService(accessToken)
                                visualizationRemote.fetchAndSaveUserTranscriptions(visualizationPrefs)
                                visualizationRemote.fetchAndSaveLast7DaysAggregated(visualizationPrefs)

                                userPreferences.saveLoginTimestamp()

                                runOnUiThread {
                                    Toast.makeText(this@SignInActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@SignInActivity, HomeActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    startActivity(intent)
                                }
                            } catch (e: Exception) {
                                Log.e("SignInActivity", "❌ Error perfil: ${e.message}")
                                runOnUiThread {
                                    Toast.makeText(
                                        this@SignInActivity,
                                        when {
                                            e.message?.contains("timeout") == true ||
                                                    e.message?.contains("Unable to resolve host") == true ->
                                                "Error de conexión. Verifica tu Internet."
                                            e.message?.contains("401") == true ->
                                                "Tu sesión expiró. Vuelve a iniciar sesión."
                                            e.message?.contains("403") == true ->
                                                "No tienes permisos para acceder."
                                            else -> "Error al cargar tu perfil. Intenta más tarde."
                                        },
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    onError = {
                        Log.e("SignInActivity", "❌ Error al obtener userId: $it")
                        runOnUiThread {
                            Toast.makeText(this, "No se pudo obtener tu perfil. Intenta nuevamente.", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            },
            onError = { error ->
                Log.e("SignInActivity", "❌ Error en loginUser: $error")
                runOnUiThread {
                    val message = when {
                        "correo no verificado" in error.lowercase() ->
                            "Debes verificar tu correo electrónico antes de iniciar sesión."

                        "401" in error || "credenciales inválidas" in error.lowercase() ->
                            "Credenciales incorrectas. Verifica tus datos."

                        "403" in error || "bloqueado" in error.lowercase() ->
                            "Credenciales incorrectas. Verifica tus datos."

                        "timeout" in error.lowercase() || "connection" in error.lowercase() ->
                            "No se pudo conectar. Verifica tu Internet."

                        else -> "Ocurrió un error al iniciar sesión. Intenta más tarde."
                    }
                    Toast.makeText(this, "⚠️ $message", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

}
