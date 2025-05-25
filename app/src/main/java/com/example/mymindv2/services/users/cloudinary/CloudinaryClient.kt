package com.example.mymindv2.services.users.cloudinary

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object CloudinaryClient {
    private const val BASE_URL = "https://api.cloudinary.com/v1_1/dkm4nlyxr/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
}
