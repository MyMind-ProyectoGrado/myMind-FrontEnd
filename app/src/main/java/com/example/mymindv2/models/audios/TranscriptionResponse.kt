package com.example.mymindv2.models.audios

data class TranscriptionResponse(
    val task_id: String,
    val result: TranscriptionResult?,
    val cached: Boolean
)