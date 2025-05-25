package com.example.mymindv2.models.audios

data class TranscriptionResult(
    val date: String,
    val time: String,
    val text: String,
    val emotion: String,
    val emotionProbabilities: Map<String, Float>,
    val sentiment: String,
    val sentimentProbabilities: Map<String, Float>,
    val topic: String?
)