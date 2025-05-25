package com.example.mymindv2.models.users

data class UserData(
    val email: String,
    val name: String,
    val profileImageUri: String,
    val token: String? = null,
    val userId: String? = null,
    val city: String? = null,
    val university: String? = null,
    val career: String? = null,
    val gender: String? = null,
    val personality: String? = null,
    val birthday: String? = null
)