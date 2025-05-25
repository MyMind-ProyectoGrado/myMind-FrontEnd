package com.example.mymindv2.models.users

data class FinalUserRegisterRequest(
    val name: String,
    val email: String,
    val profilePic: String,
    val birthdate: String,
    val city: String,
    val personality: String,
    val university: String,
    val degree: String,
    val gender: String,
    val notifications: Boolean = true,
    val data_treatment: DataTreatment
)