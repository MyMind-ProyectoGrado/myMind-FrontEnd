package com.example.mymindv2.models.audios

fun TranscriptionAudioResult.toTranscriptionResult(): TranscriptionResult {
    val emotionProbs = mapOf(
        "joy" to emotion_probs_joy.toFloat(),
        "anger" to emotion_probs_anger.toFloat(),
        "sadness" to emotion_probs_sadness.toFloat(),
        "disgust" to emotion_probs_disgust.toFloat(),
        "fear" to emotion_probs_fear.toFloat(),
        "neutral" to emotion_probs_neutral.toFloat(),
        "surprise" to emotion_probs_surprise.toFloat(),
        "trust" to emotion_probs_trust.toFloat(),
        "anticipation" to emotion_probs_anticipation.toFloat()
    )
    val sentimentProbs = mapOf(
        "positive" to sentiment_probs_positive.toFloat(),
        "negative" to sentiment_probs_negative.toFloat(),
        "neutral" to sentiment_probs_neutral.toFloat()
    )
    return TranscriptionResult(
        date = transcription_date,
        time = transcription_time,
        text = "", // Este campo no viene en el modelo por ID
        emotion = emotion,
        emotionProbabilities = emotionProbs,
        sentiment = sentiment,
        sentimentProbabilities = sentimentProbs,
        topic = null
    )
}
