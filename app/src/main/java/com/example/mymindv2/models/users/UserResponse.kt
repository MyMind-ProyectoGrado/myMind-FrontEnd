package com.example.mymindv2.models.users

data class UserResponse(
    val email: String,
    val name: String,
    val profileImageUri: String,
    val userId: String,
    val token: String?
)