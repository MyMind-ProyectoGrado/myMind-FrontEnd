package com.example.mymindv2.services.users

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.mymindv2.models.users.UserData

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveFullUserData(
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
        sharedPreferences.edit().apply {
            putString("email", email)
            putString("name", name)
            putString("profileImageUri", profileImageUri)
            putString("city", city)
            putString("university", university)
            putString("career", career)
            putString("gender", gender)
            putString("personality", personality)
            putString("birthday", birthday)
            apply()
        }
        Log.d("UserPreferences", "Guardando datos: $email, $name, $profileImageUri, $city, $university, $career, $gender, $personality, $birthday")

    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    fun getUserEmail(): String {
        return sharedPreferences.getString("email", "") ?: ""
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getUserData(): UserData? {
        val email = sharedPreferences.getString("email", null)
        val name = sharedPreferences.getString("name", null)
        val profileImageUri = sharedPreferences.getString("profileImageUri", null)
        val token = sharedPreferences.getString("auth_token", null)
        val userId = sharedPreferences.getString("user_id", null)

        return if (email != null && name != null && profileImageUri != null && userId != null) {
            UserData(email, name, profileImageUri, token, userId)
        } else {
            null
        }
    }

    fun getUserName(): String {
        return sharedPreferences.getString("name", "") ?: ""
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("name", name).apply()
    }

    fun getProfileImage(): String {
        return sharedPreferences.getString("profileImageUri", "") ?: ""
    }

    fun saveProfileImage(uri: String) {
        sharedPreferences.edit().putString("profileImageUri", uri).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }

    fun clearAuthToken() {
        sharedPreferences.edit().remove("auth_token").apply()
    }

    fun clearUserId() {
        sharedPreferences.edit().remove("user_id").apply()
    }

    fun saveNotificationSettings(enabled: Boolean, hour: Int, minute: Int) {
        sharedPreferences.edit().apply {
            putBoolean("notifications_enabled", enabled)
            putInt("notification_hour", hour)
            putInt("notification_minute", minute)
            apply()
        }
    }

    // 🔔 Obtener configuración de notificaciones
    fun getNotificationSettings(): Triple<Boolean, Int, Int> {
        val enabled = sharedPreferences.getBoolean("notifications_enabled", true)
        val hour = sharedPreferences.getInt("notification_hour", 20) // Por defecto 8 PM
        val minute = sharedPreferences.getInt("notification_minute", 0)
        return Triple(enabled, hour, minute)
    }

    fun getUserCity(): String = sharedPreferences.getString("city", "") ?: ""
    fun getUserUniversity(): String = sharedPreferences.getString("university", "") ?: ""
    fun getUserCareer(): String = sharedPreferences.getString("career", "") ?: ""
    fun getUserGender(): String = sharedPreferences.getString("gender", "") ?: ""
    fun getUserPersonality(): String = sharedPreferences.getString("personality", "") ?: ""
    fun getUserBirthDate(): String = sharedPreferences.getString("birthday", "") ?: ""


    fun saveUserCity(city: String) {
        sharedPreferences.edit().putString("city", city).apply()
    }

    fun saveUserUniversity(university: String) {
        sharedPreferences.edit().putString("university", university).apply()
    }

    fun saveUserCareer(career: String) {
        sharedPreferences.edit().putString("career", career).apply()
    }

    fun saveUserGender(gender: String) {
        sharedPreferences.edit().putString("gender", gender).apply()
    }

    fun saveUserPersonality(personality: String) {
        sharedPreferences.edit().putString("personality", personality).apply()
    }

    fun saveUserBirthDate(birthday: String) {
        sharedPreferences.edit().putString("birthday", birthday).apply()
    }

    fun hasSeenInstructions(): Boolean {
        return sharedPreferences.getBoolean("has_seen_instructions", false)
    }

    fun setHasSeenInstructions(seen: Boolean) {
        sharedPreferences.edit().putBoolean("has_seen_instructions", seen).apply()
    }

    // Guarda el timestamp del login
    fun saveLoginTimestamp(timestamp: Long = System.currentTimeMillis()) {
        sharedPreferences.edit().putLong("login_timestamp", timestamp).apply()
    }

    // Recupera el timestamp guardado
    fun getLoginTimestamp(): Long {
        return sharedPreferences.getLong("login_timestamp", 0)
    }

    // Verifica si han pasado más de 30 días desde el último login
    fun isLoginExpired(): Boolean {
        val saved = getLoginTimestamp()
        val now = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        return (now - saved) > thirtyDaysInMillis
    }

}