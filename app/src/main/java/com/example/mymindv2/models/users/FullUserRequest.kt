package com.example.mymindv2.models.users

data class FullUserRequest(
    val email: String,
    val name: String,
    val profilePic: String,
    val birthdate: String,
    val city: String,
    val university: String,
    val degree: String,
    val gender: String,
    val personality: String
)