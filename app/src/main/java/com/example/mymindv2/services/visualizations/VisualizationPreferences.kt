package com.example.mymindv2.services.visualizations

import android.content.Context
import android.content.SharedPreferences
import com.example.mymindv2.models.audios.TranscriptionAudioResult
import com.google.gson.Gson
import com.example.mymindv2.models.visualizations.*
import com.example.mymindv2.models.audios.TranscriptionResult
import com.example.mymindv2.models.audios.toTranscriptionResult

class VisualizationPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("visualization_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Acepta TranscriptionResult directamente
    fun saveLatestTranscriptionResult(result: TranscriptionResult) {
        sharedPreferences.edit()
            .putString("latest_transcription_result", gson.toJson(result))
            .apply()
    }

    // Acepta TranscriptionAudioResult y lo transforma antes de guardar
    fun saveLatestTranscriptionResult(result: TranscriptionAudioResult) {
        val transformed = result.toTranscriptionResult()
        saveLatestTranscriptionResult(transformed)
    }

    fun getLatestTranscriptionResult(): TranscriptionResult? {
        val json = sharedPreferences.getString("latest_transcription_result", null)
        return json?.let { gson.fromJson(it, TranscriptionResult::class.java) }
    }

    fun saveTranscriptions(data: List<TranscriptionItem>) {
        sharedPreferences.edit().putString("user_transcriptions", gson.toJson(data)).apply()
    }

    fun saveLast7DaysAggregated(data: AggregatedEmotionSentiment) {
        sharedPreferences.edit().putString("last7_aggregated", gson.toJson(data)).apply()
    }

    fun getLast7DaysAggregated(): AggregatedEmotionSentiment? {
        val json = sharedPreferences.getString("last7_aggregated", null)
        return json?.let { gson.fromJson(it, AggregatedEmotionSentiment::class.java) }
    }

    fun getTranscriptions(): List<TranscriptionItem>? {
        val json = sharedPreferences.getString("user_transcriptions", null)
        return json?.let {
            gson.fromJson(it, Array<TranscriptionItem>::class.java).toList()
        }
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}