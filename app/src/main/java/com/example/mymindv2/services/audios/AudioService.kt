package com.example.mymindv2.services.audios

import com.example.mymindv2.models.audios.AudioUploadResponse
import com.example.mymindv2.models.audios.TranscriptionAudioResult
import com.example.mymindv2.models.audios.TranscriptionResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface AudioService {

    @Multipart
    @POST("transcription/add-transcription/audio")
    fun uploadAudioFile(
        @Header("Authorization") token: String,
        @Part audio: MultipartBody.Part
    ): Call<AudioUploadResponse>

    @DELETE("users/transcriptions/delete-transcription/{transcription_id}")
    fun deleteTranscription(
        @Header("Authorization") token: String,
        @Path("transcription_id") id: String
    ): Call<Void>

    @DELETE("users/transcriptions/delete-all-transcriptions")
    fun deleteAllTranscriptions(
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("transcription/stream/{task_id}")
    fun getTranscriptionResult(
        @Header("Authorization") token: String,
        @Path("task_id") taskId: String
    ): Call<TranscriptionResponse>

    @GET("transcriptions/user/audio/{audio_id}")
    fun getTranscriptionById(
        @Header("Authorization") token: String,
        @Path("audio_id") audioId: String
    ): Call<TranscriptionAudioResult>

}
