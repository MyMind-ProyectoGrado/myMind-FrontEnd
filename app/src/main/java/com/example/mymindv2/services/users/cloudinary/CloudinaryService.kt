package com.example.mymindv2.services.users.cloudinary

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CloudinaryService {
    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") preset: RequestBody,
        @Part("folder") folder: RequestBody
    ): Response<String>
}
