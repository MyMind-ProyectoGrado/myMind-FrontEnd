package com.example.mymindv2.models.visualizations

data class TranscriptionItem(
    val transcription_id: String,
    val transcription_date: String,
    val transcription_time: String
)

data class AggregatedEmotionSentiment(
    val emotion_probs_joy: Double,
    val emotion_probs_anger: Double,
    val emotion_probs_sadness: Double,
    val emotion_probs_disgust: Double,
    val emotion_probs_fear: Double,
    val emotion_probs_neutral: Double,
    val emotion_probs_surprise: Double,
    val emotion_probs_trust: Double,
    val emotion_probs_anticipation: Double,
    val sentiment_probs_positive: Double,
    val sentiment_probs_negative: Double,
    val sentiment_probs_neutral: Double
)
