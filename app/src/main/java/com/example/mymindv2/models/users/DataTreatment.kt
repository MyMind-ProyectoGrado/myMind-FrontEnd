package com.example.mymindv2.models.users

data class DataTreatment(
    val accept_policies: Boolean,
    val acceptance_date: String,
    val acceptance_ip: String,
    val privacy_preferences: PrivacyPreferences
)

data class PrivacyPreferences(
    val allow_anonimized_usage: Boolean
)
