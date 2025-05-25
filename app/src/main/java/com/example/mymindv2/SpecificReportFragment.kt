package com.example.mymindv2

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.mymindv2.databinding.FragmentSpecificReportBinding
import com.example.mymindv2.models.audios.TranscriptionResult
import com.example.mymindv2.services.visualizations.VisualizationPreferences
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class SpecificReportFragment : Fragment() {

    private var _binding: FragmentSpecificReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var transcription: TranscriptionResult

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpecificReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transcription = VisualizationPreferences(requireContext()).getLatestTranscriptionResult()
            ?: return

        setupRadarChart()
        setupPodiumChart()
        setupSentimentPieChart()

        binding.scrollContainer.post {
            binding.scrollContainer.smoothScrollTo(0, 250)
        }
    }

    private fun getMoodColor(): Int {
        val top = transcription.emotionProbabilities.maxByOrNull { it.value }?.key
        return when (top) {
            "anger", "sadness", "disgust", "fear" -> Color.parseColor("#FF8A65")
            "joy", "trust", "surprise" -> Color.parseColor("#64B5F6")
            else -> Color.parseColor("#90A4AE")
        }
    }

    private fun setupRadarChart() {
        val emociones = listOf(
            "Alegría", "Rabia", "Tristeza", "Disgusto", "Miedo",
            "Neutral", "Sorpresa", "Confianza", "Anticipación"
        )
        val claves = listOf(
            "joy", "anger", "sadness", "disgust", "fear",
            "neutral", "surprise", "trust", "anticipation"
        )

        val valores = claves.map { transcription.emotionProbabilities[it] ?: 0f }
        val entries = valores.map { RadarEntry(it) }
        val maxValue = valores.maxOrNull()?.coerceAtLeast(0.1f) ?: 1f

        val dataSet = RadarDataSet(entries, "").apply {
            color = Color.parseColor("#F48FB1")
            fillColor = Color.parseColor("#F48FB1")
            fillColor = color
            setDrawFilled(true)
            setDrawValues(false)
        }

        binding.radarChart.apply {
            data = RadarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(emociones)
            xAxis.textSize = 14f
            yAxis.axisMinimum = 0f
            yAxis.axisMaximum = maxValue
            yAxis.setDrawLabels(false)
            legend.isEnabled = false
            description.isEnabled = false
            setTouchEnabled(false)
            isRotationEnabled = true
            invalidate()
        }
    }

    private fun setupPodiumChart() {
        val emocionesMap = transcription.emotionProbabilities.mapKeys {
            when (it.key) {
                "joy" -> "Alegría"
                "anger" -> "Rabia"
                "sadness" -> "Tristeza"
                "disgust" -> "Disgusto"
                "fear" -> "Miedo"
                "neutral" -> "Neutral"
                "surprise" -> "Sorpresa"
                "trust" -> "Confianza"
                "anticipation" -> "Anticipación"
                else -> it.key
            }
        }

        val sorted = emocionesMap.entries.sortedByDescending { it.value }.take(3)
        val entries = sorted.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value)
        }

        val dataSet = BarDataSet(entries, "").apply {
            setColors(
                Color.parseColor("#f4c0e5"),
                Color.parseColor("#84bcf6"),
                Color.parseColor("#a5dfea")
            )
            setDrawValues(false)
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            setScaleEnabled(false)
            setPinchZoom(false)
            setDoubleTapToZoomEnabled(false)
            setTouchEnabled(false)
            axisLeft.axisMinimum = 0f
            axisLeft.setDrawLabels(false)
            axisRight.isEnabled = false
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(sorted.map { it.key })
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textSize = 12f
                setDrawGridLines(false)
                yOffset = 0f
            }
            legend.isEnabled = false
            description.isEnabled = false
            invalidate()
        }
    }

    private fun setupSentimentPieChart() {
        val probs = transcription.sentimentProbabilities
        val entries = listOf(
            PieEntry(probs["positive"] ?: 0f, "Positivo"),
            PieEntry(probs["negative"] ?: 0f, "Negativo"),
            PieEntry(probs["neutral"] ?: 0f, "Neutral")
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(
                Color.parseColor("#f4c0e5"),
                Color.parseColor("#84bcf6"),
                Color.parseColor("#a5dfea")
            )
            setDrawValues(false)
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            centerText = "Sentimientos"
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
