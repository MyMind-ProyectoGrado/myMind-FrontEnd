package com.example.mymindv2.services.users

import com.example.mymindv2.models.users.FinalUserRegisterRequest
import com.example.mymindv2.models.users.FullUserRequest
import com.example.mymindv2.models.users.NotificationSettingsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {

    @POST("users/register")
    fun registerUserBackend(
        @Header("Authorization") token: String,
        @Body request: FinalUserRegisterRequest
    ): Call<Void>

    @GET("users/profile")
    fun getUserProfile(@Header("Authorization") token: String): Call<FullUserRequest>

    @DELETE("users/delete")
    fun deleteUserFromBackend(@Header("Authorization") token: String): Call<Void>

    @PATCH("users/update-name")
    fun updateUserName(
        @Query("new_name") newName: String,
        @Header("Authorization") token: String
    ): Call<Void>;

    @PATCH("users/update-profile-pic")
    fun updateUserProfilePic(
        @Header("Authorization") token: String,
        @Body pic: Map<String, String>
    ): Call<Void>

    @GET("users/notifications")
    fun getNotificationSettings(@Header("Authorization") token: String): Call<NotificationSettingsResponse>

    @PATCH("users/update-notifications")
    fun toggleNotifications(@Header("Authorization") token: String): Call<Void>

}