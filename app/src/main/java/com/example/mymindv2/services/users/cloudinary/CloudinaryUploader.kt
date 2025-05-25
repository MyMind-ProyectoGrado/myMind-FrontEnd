package com.example.mymindv2.services.users.cloudinary

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

object CloudinaryUploader {

    suspend fun uploadImageToCloudinary(imagePath: String): String? {
        return try {
            val file = File(imagePath)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val preset = "mymind_unsigned".toRequestBody("text/plain".toMediaTypeOrNull())
            val folder = "profileImages/".toRequestBody("text/plain".toMediaTypeOrNull())

            val service = CloudinaryClient.instance.create(CloudinaryService::class.java)
            val response = service.uploadImage(body, preset, folder)

            if (response.isSuccessful) {
                val json = JSONObject(response.body()!!)
                json.getString("secure_url")
            } else {
                Log.e("CloudinaryUploader", "❌ Error Cloudinary: ${response.code()}\n${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("CloudinaryUploader", "❌ Excepción al subir imagen: ${e.message}", e)
            null
        }
    }
}

