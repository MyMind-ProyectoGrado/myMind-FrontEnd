package com.example.mymindv2.models.users

fun UserResponse.toUserData(): UserData {
    return UserData(
        email = this.email,
        name = this.name,
        profileImageUri = this.profileImageUri,
        token = this.token,
        userId = this.userId
    )
}
