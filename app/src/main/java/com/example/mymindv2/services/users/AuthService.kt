package com.example.mymindv2.services.users

import android.util.Log
import com.example.mymindv2.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AuthService {

    private val client = OkHttpClient()
    private var authToken: String? = null

    fun getAuthToken(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (authToken != null) {
            onSuccess(authToken!!)
            return
        }

        val url = "https://my-mind.us.auth0.com/oauth/token"

        val json = JSONObject().apply {
            put("client_id", BuildConfig.MGMT_CLIENT_ID)
            put("client_secret", BuildConfig.MGMT_CLIENT_SECRET)
            put("audience", BuildConfig.AUTH_AUDIENCE_MGMT)
            put("grant_type", "client_credentials")
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AuthService", "❌ Error conexión authToken: ${e.message}")
                onError("Error en la conexión: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    authToken = jsonResponse.optString("access_token", "")
                    if (!authToken.isNullOrEmpty()) {
                        onSuccess(authToken!!)
                    } else {
                        onError("No se obtuvo el token")
                    }
                } else {
                    onError("Error al obtener token: ${response.code} - $responseBody")
                }
            }
        })
    }

    fun loginUser(
        email: String,
        password: String,
        requireEmailVerified: Boolean = true,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://${BuildConfig.AUTH_DOMAIN}/oauth/token"
        Log.d("AuthService", "🚀 Iniciando login para $email (requireEmailVerified=$requireEmailVerified)")

        val json = JSONObject().apply {
            put("grant_type", "password")
            put("username", email)
            put("password", password)
            put("scope", "openid email profile")
            put("client_id", BuildConfig.AUTH_CLIENT_ID)
            put("client_secret", BuildConfig.AUTH_CLIENT_SECRET)
            put("audience", BuildConfig.AUTH_AUDIENCE_BACKEND)
            put("connection", "Username-Password-Authentication")
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        Log.d("AuthService", "📡 Enviando solicitud de login a $url")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AuthService", "❌ Error de conexión en login: ${e.message}")
                onError("Error de conexión: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("AuthService", "📬 Respuesta recibida: ${response.code}")
                Log.d("AuthService", "📬 Body: $responseBody")

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    val accessToken = jsonResponse.optString("access_token", "")

                    if (accessToken.isNotEmpty()) {
                        if (requireEmailVerified) {
                            verifyEmailStatus(accessToken,
                                onSuccess = { onSuccess(accessToken) },
                                onError = { onError("correo no verificado") }
                            )
                        } else {
                            onSuccess(accessToken)
                        }
                    } else {
                        onError("Token de acceso vacío")
                    }

                } else {
                    val errorJson = responseBody?.let { JSONObject(it) }
                    val errorDesc = errorJson?.optString("error_description", "") ?: ""

                    when {
                        response.code == 403 || errorDesc.contains("user is blocked") -> {
                            onError("403: usuario bloqueado o sin permisos")
                        }
                        response.code == 401 || errorDesc.contains("invalid") || errorDesc.contains("Wrong email or password") -> {
                            onError("401: credenciales inválidas")
                        }
                        else -> {
                            onError("Login fallido: ${response.code} - $errorDesc")
                        }
                    }
                }
            }
        })
    }



    fun sendResetPasswordEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val url = "https://${BuildConfig.AUTH_DOMAIN}/dbconnections/change_password"

        val json = JSONObject().apply {
            put("client_id", BuildConfig.AUTH_CLIENT_ID)
            put("email", email)
            put("connection", "Username-Password-Authentication")
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error de conexión: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Error al enviar el correo: ${response.code} - $responseBody")
                }
            }
        })
    }


    fun registerUserAndGetId(
        email: String,
        password: String,
        onSuccess: (userId: String, accessToken: String) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d("AuthService", "🟡 Iniciando registro de usuario con email: $email")

        getAuthToken(
            onSuccess = { mgmtToken ->
                Log.d("AuthService", "🟢 Token de gestión obtenido correctamente")

                val url = "https://my-mind.us.auth0.com/api/v2/users"

                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                    put("connection", "Username-Password-Authentication")
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer $mgmtToken")
                    .addHeader("Content-Type", "application/json")
                    .build()

                Log.d("AuthService", "📡 Enviando solicitud de creación de usuario a $url")

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("AuthService", "❌ Error al crear usuario: ${e.message}")
                        onError("Error de conexión: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        Log.d("AuthService", "📬 Respuesta de creación de usuario: ${response.code}")
                        Log.d("AuthService", "📬 Body: $responseBody")

                        if (response.isSuccessful) {
                            val jsonResponse = JSONObject(responseBody ?: "{}")
                            val userId = jsonResponse.optString("user_id", "")
                            Log.d("AuthService", "✅ Usuario creado con ID: $userId")

                            Log.d("AuthService", "🔐 Iniciando login sin verificación de email")
                            loginUser(
                                email,
                                password,
                                requireEmailVerified = false,
                                onSuccess = { userAccessToken ->
                                    Log.d("AuthService", "✅ Login exitoso tras registro")
                                    onSuccess(userId, userAccessToken)
                                },
                                onError = { error ->
                                    Log.e("AuthService", "⚠️ Usuario creado pero falló login: $error")
                                    onError("Usuario creado pero error al obtener token de acceso: $error")
                                }
                            )
                        } else {
                            Log.e("AuthService", "❌ Error al registrar usuario: ${response.code} - $responseBody")
                            onError("Error al registrar usuario: ${response.code} - $responseBody")
                        }
                    }
                })
            },
            onError = { tokenError ->
                Log.e("AuthService", "❌ Error al obtener token de gestión: $tokenError")
                onError("Error al obtener token del sistema: $tokenError")
            }
        )
    }


    fun deleteUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        getAuthToken(
            onSuccess = { token ->
                val url = "https://my-mind.us.auth0.com/api/v2/users/$userId"

                val request = Request.Builder()
                    .url(url)
                    .delete()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onError("Error de conexión: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        if (response.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("Error al eliminar usuario: ${response.code} - ${responseBody}")
                        }
                    }
                })
            },
            onError = { error -> onError("Error al obtener token: $error") }
        )
    }

    fun getUserId(
        accessToken: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://my-mind.us.auth0.com/userinfo"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error de conexión: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    val userId = jsonResponse.optString("sub", "")
                    if (userId.isNotEmpty()) {
                        onSuccess(userId)
                    } else {
                        onError("No se obtuvo el ID del usuario")
                    }
                } else {
                    onError("Error al obtener ID de usuario: ${response.code} - ${responseBody}")
                }
            }
        })
    }


    fun updateUserPassword(
        accessToken: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        getUserId(
            accessToken = accessToken,
            onSuccess = { userId ->
                getAuthToken(
                    onSuccess = { token ->
                        val url = "https://my-mind.us.auth0.com/api/v2/users/$userId"

                        val json = JSONObject().apply {
                            put("password", newPassword)
                            put(
                                "connection",
                                "Username-Password-Authentication"
                            ) // Necesario para actualizar la contraseña
                        }

                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val body = json.toString().toRequestBody(mediaType)

                        val request = Request.Builder()
                            .url(url)
                            .patch(body)
                            .addHeader("Authorization", "Bearer $token")
                            .addHeader("Content-Type", "application/json")
                            .build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                onError("Error de conexión: ${e.message}")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseBody = response.body?.string()
                                if (response.isSuccessful) {
                                    onSuccess()
                                } else {
                                    onError("Error al actualizar contraseña: ${response.code} - ${responseBody}")
                                }
                            }
                        })
                    },
                    onError = onError
                )
            },
            onError = onError
        )
    }

    fun verifyEmailStatus(
        accessToken: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://my-mind.us.auth0.com/userinfo"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AuthService", "❌ Error al verificar email: ${e.message}")
                onError("Error de conexión: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("AuthService", "📬 Email verification response code: ${response.code}")
                Log.d("AuthService", "📬 Response body: $responseBody")

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    val emailVerified = jsonResponse.optBoolean("email_verified", false)

                    if (emailVerified) {
                        Log.d("AuthService", "✅ Email verificado")
                        onSuccess(accessToken)
                    } else {
                        Log.w("AuthService", "⚠️ Email no verificado")
                        onError("Debes verificar tu correo electrónico antes de iniciar sesión.")
                    }
                } else {
                    onError("Error al verificar email: ${response.code} - $responseBody")
                }
            }
        })
    }

}