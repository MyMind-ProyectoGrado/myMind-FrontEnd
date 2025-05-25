package com.example.mymindv2.services.users

import android.util.Log
import com.example.mymindv2.models.users.*
import com.example.mymindv2.services.RetrofitClient
import com.google.gson.GsonBuilder
import retrofit2.awaitResponse
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class UserRemoteService(
    private val token: String,
    var service: UserService = RetrofitClient.getInstance().create(UserService::class.java)
) {

    suspend fun registerFinalUser(
        email: String,
        name: String,
        profileImageUri: String,
        city: String,
        university: String,
        career: String,
        gender: String,
        personality: String,
        birthday: String
    ) {
        val dateNow = getCurrentFormattedTimestamp()
        val ip = getLocalIpAddress() ?: "0.0.0.0"
        val formattedBirthday = formatDate(birthday)

        val request = FinalUserRegisterRequest(
            name = name,
            email = email,
            profilePic = profileImageUri,
            birthdate = formattedBirthday,
            city = city,
            personality = personality,
            university = university,
            degree = career,
            gender = gender,
            notifications = true,
            data_treatment = DataTreatment(
                accept_policies = true,
                acceptance_date = dateNow,
                acceptance_ip = ip,
                privacy_preferences = PrivacyPreferences(
                    allow_anonimized_usage = true
                )
            )
        )

        val gson = GsonBuilder().setPrettyPrinting().create()
        Log.d("UserRemoteService", "📝 JSON enviado al backend:\n${gson.toJson(request)}")

        val response = service.registerUserBackend(token, request).awaitResponse()
        Log.d("UserRemoteService", "📨 Response Code: ${response.code()}")
        Log.d("UserRemoteService", "📨 Response Message: ${response.message()}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e("UserRemoteService", "❌ Error Body: $errorBody")
            throw Exception("Error en registro final: ${response.code()} - $errorBody")
        }
    }

    suspend fun fetchAndSaveUserProfile(userPreferences: UserPreferences) {
        val response = service.getUserProfile(token).awaitResponse()
        val userResponse = response.body()

        if (userResponse != null) {
            userPreferences.saveFullUserData(
                email = userResponse.email,
                name = userResponse.name,
                profileImageUri = userResponse.profilePic,
                city = userResponse.city,
                university = userResponse.university,
                career = userResponse.degree,
                gender = userResponse.gender,
                personality = userResponse.personality,
                birthday = userResponse.birthdate
            )
        } else {
            Log.e("UserRemoteService", "❌ No se recibió cuerpo del perfil del usuario")
            throw Exception("El perfil del usuario está vacío.")
        }
    }

    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
            null
        } catch (ex: Exception) {
            Log.e("IP", "No se pudo obtener IP: ${ex.message}")
            null
        }
    }

    fun formatDate(input: String): String {
        return try {
            val inputFormatter = SimpleDateFormat("d/M/yyyy")
            val outputFormatter = SimpleDateFormat("yyyy-MM-dd")
            val date = inputFormatter.parse(input)
            outputFormatter.format(date!!)
        } catch (e: Exception) {
            input // si falla, se envía como está
        }
    }

    fun getCurrentFormattedTimestamp(): String {
        val zonedNow = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        return formatter.format(zonedNow)
    }

    suspend fun deleteUserFromBackend() {
        val response = service.deleteUserFromBackend(token).awaitResponse()
        if (!response.isSuccessful) {
            throw Exception("No se pudo eliminar el usuario del backend: ${response.code()}")
        }
    }

    suspend fun updateUserName(newName: String) {
        service.updateUserName(newName, token).awaitResponse()
    }

    suspend fun updateUserProfilePic(uri: String) {
        service.updateUserProfilePic(token, mapOf("profilePic" to uri)).awaitResponse()
    }

    suspend fun fetchNotificationSettings(): Pair<Boolean, Boolean> {
        val response = service.getNotificationSettings(token).awaitResponse()
        if (response.isSuccessful) {
            val notifications = response.body()?.notifications ?: true
            return Pair(true, notifications)
        } else {
            throw Exception("Error al obtener configuración de notificaciones: ${response.code()}")
        }
    }

    suspend fun toggleNotificationSettings() {
        val response = service.toggleNotifications(token).awaitResponse()
        if (!response.isSuccessful) {
            throw Exception("Error al actualizar configuración de notificaciones: ${response.code()}")
        }
    }
}
