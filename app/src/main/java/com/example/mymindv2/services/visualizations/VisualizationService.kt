package com.example.mymindv2.services.visualizations

import com.example.mymindv2.models.visualizations.AggregatedEmotionSentiment
import com.example.mymindv2.models.visualizations.TranscriptionItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface VisualizationService {

    @GET("/transcriptions/user")
    fun getUserTranscriptions(@Header("Authorization") token: String): Call<List<TranscriptionItem>>

    @GET("transcriptions/user/last-7-days")
    fun getLast7DaysAggregated(@Header("Authorization") token: String): Call<AggregatedEmotionSentiment>
}
