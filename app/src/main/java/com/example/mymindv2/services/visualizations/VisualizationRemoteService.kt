package com.example.mymindv2.services.visualizations

import android.util.Log
import com.example.mymindv2.models.visualizations.*
import retrofit2.awaitResponse

class VisualizationRemoteService(
    private val token: String,
    val service: VisualizationService = com.example.mymindv2.services.RetrofitClient
        .getInstance()
        .create(VisualizationService::class.java)
) {

    private val TAG = "VisualizationService"

    suspend fun fetchAndSaveUserTranscriptions(preferences: VisualizationPreferences) {
        try {
            val response = service.getUserTranscriptions("Bearer $token").awaitResponse()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    preferences.saveTranscriptions(it)
                    Log.d(TAG, "✅ Transcripciones guardadas (${it.size} items):\n$it")
                } ?: Log.w(TAG, "⚠️ Respuesta de userTranscriptions vacía")
            } else {
                Log.e(TAG, "❌ Error HTTP en userTranscriptions: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción en userTranscriptions: ${e.message}", e)
        }
    }

    suspend fun fetchAndSaveLast7DaysAggregated(preferences: VisualizationPreferences) {
        try {
            val response = service.getLast7DaysAggregated("Bearer $token").awaitResponse()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    preferences.saveLast7DaysAggregated(it)
                    Log.d(TAG, "✅ Agregados de los últimos 7 días guardados:\n$it")
                } ?: Log.w(TAG, "⚠️ Respuesta de last7DaysAggregated vacía")
            } else {
                Log.e(TAG, "❌ Error HTTP en last7DaysAggregated: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción en last7DaysAggregated: ${e.message}", e)
        }
    }
}
