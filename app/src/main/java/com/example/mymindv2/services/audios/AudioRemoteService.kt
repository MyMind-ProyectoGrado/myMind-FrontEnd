package com.example.mymindv2.services.audios

import android.util.Log
import com.example.mymindv2.models.audios.TranscriptionAudioResult
import com.example.mymindv2.models.audios.TranscriptionResult
import com.example.mymindv2.services.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import retrofit2.awaitResponse

class AudioRemoteService(
    private val token: String,
    val service: AudioService = RetrofitClient.getInstance().create(AudioService::class.java)
) {

    private val TAG = "AudioRemoteService"

    suspend fun uploadAudio(file: File): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎙️ Iniciando subida de audio...")
            val realFilename = "recorded_audio.m4a"
            val mediaType = "audio/mp4".toMediaTypeOrNull()
            val requestFile = file.asRequestBody(mediaType)
            val multipartBody = MultipartBody.Part.createFormData("audio", realFilename, requestFile)

            val response = service.uploadAudioFile("Bearer $token", multipartBody).awaitResponse()
            Log.d(TAG, "📡 Respuesta recibida: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "✅ task_id recibido: ${body?.task_id}")
                return@withContext body?.task_id ?: throw Exception("No se recibió task_id del backend.")
            } else {
                val errorJson = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al subir audio: ${response.code()} - $errorJson")
                throw Exception("Error al subir audio: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al subir audio: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteSingleTranscription(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ Eliminando transcripción con ID: $id")
            val response = service.deleteTranscription("Bearer $token", id).awaitResponse()
            Log.d(TAG, "📡 Respuesta al eliminar una: ${response.code()}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando transcripción $id: ${e.message}", e)
            false
        }
    }

    suspend fun deleteAllTranscriptions(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🧹 Eliminando todas las transcripciones...")
            val response = service.deleteAllTranscriptions("Bearer $token").awaitResponse()
            Log.d(TAG, "📡 Respuesta al eliminar todas: ${response.code()}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando todas las transcripciones: ${e.message}", e)
            false
        }
    }

    suspend fun getTranscriptionById(id: String): TranscriptionAudioResult? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔎 Buscando transcripción con ID: $id")
            val response = service.getTranscriptionById("Bearer $token", id).awaitResponse()
            Log.d(TAG, "📡 Respuesta al obtener por ID: ${response.code()}")

            if (response.isSuccessful) {
                val result = response.body()
                Log.d(TAG, "✅ Transcripción obtenida: $result")
                return@withContext result
            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al obtener transcripción: ${response.code()} - $error")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al obtener transcripción por ID: ${e.message}", e)
        }
        null
    }

}
