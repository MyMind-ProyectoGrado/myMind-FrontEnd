package com.example.mymindv2

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mymindv2.databinding.FragmentReportBinding
import com.example.mymindv2.models.visualizations.AggregatedEmotionSentiment
import com.example.mymindv2.services.visualizations.VisualizationPreferences
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: VisualizationPreferences
    private var enterTimestamp: Long = 0L

    private val emociones = listOf(
        "Felicidad", "Rabia", "Tristeza", "Disgusto", "Miedo",
        "Neutral", "Sorpresa", "Confianza", "Anticipación"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        preferences = VisualizationPreferences(requireContext())
        enterTimestamp = SystemClock.elapsedRealtime()

        val aggregated = preferences.getLast7DaysAggregated()
        if (aggregated != null && hasData(aggregated)) {
            setupRadarChart(aggregated)
            setupBarChart(aggregated)
            setupPieChart(aggregated)
            binding.tvNoDataMessage.visibility = View.GONE
        } else {
            binding.tvNoDataMessage.visibility = View.VISIBLE
            binding.radarChart.visibility = View.GONE
            binding.barChart.visibility = View.GONE
            binding.pieChart.visibility = View.GONE
        }

        parentFragmentManager.setFragmentResultListener("hide_report", viewLifecycleOwner) { _, _ ->
            Log.d("ReportFragment", "📵 Recibido hide_report: ocultando gráficos.")
            _binding?.apply {
                tvNoDataMessage.text = "⏳ Actualizando datos... por favor espera"
                tvNoDataMessage.visibility = View.VISIBLE
                radarChart.visibility = View.GONE
                barChart.visibility = View.GONE
                pieChart.visibility = View.GONE
            }

            lifecycleScope.launch {
                delay(3_000)
                val elapsed = SystemClock.elapsedRealtime() - enterTimestamp
                if (isAdded && elapsed >= 3_000) {
                    Log.d("ReportFragment", "⏱️ 60s cumplidos en pantalla: actualizando gráficos manualmente")
                    refreshChartsAfterDelay()
                }
            }
        }

        parentFragmentManager.setFragmentResultListener("view_report", viewLifecycleOwner) { _, _ ->
            Log.d("ReportFragment", "✅ Recibido view_report: mostrando gráficos actualizados.")
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    refreshChartsAfterDelay()
                }
            }
        }

        binding.btnInstructions.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AudioHistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.scrollContainer.post {
            binding.scrollContainer.smoothScrollTo(0, 250)
        }

        return binding.root
    }

    private fun hasData(aggregated: AggregatedEmotionSentiment): Boolean {
        val emotionSum = listOf(
            aggregated.emotion_probs_joy,
            aggregated.emotion_probs_anger,
            aggregated.emotion_probs_sadness,
            aggregated.emotion_probs_disgust,
            aggregated.emotion_probs_fear,
            aggregated.emotion_probs_neutral,
            aggregated.emotion_probs_surprise,
            aggregated.emotion_probs_trust,
            aggregated.emotion_probs_anticipation
        ).sum()

        val sentimentSum = listOf(
            aggregated.sentiment_probs_positive,
            aggregated.sentiment_probs_negative,
            aggregated.sentiment_probs_neutral
        ).sum()

        return emotionSum > 0 || sentimentSum > 0
    }

    private fun setupRadarChart(aggregated: AggregatedEmotionSentiment) {
        Log.d("ReportFragment", "🔧 Configurando RadarChart")
        val values = listOf(
            aggregated.emotion_probs_joy,
            aggregated.emotion_probs_anger,
            aggregated.emotion_probs_sadness,
            aggregated.emotion_probs_disgust,
            aggregated.emotion_probs_fear,
            aggregated.emotion_probs_neutral,
            aggregated.emotion_probs_surprise,
            aggregated.emotion_probs_trust,
            aggregated.emotion_probs_anticipation
        )

        val entries = values.map { RadarEntry(it.toFloat()) }
        val maxValue = values.maxOrNull()?.toFloat()?.coerceAtLeast(0.1f) ?: 1f

        val dataSet = RadarDataSet(entries, "").apply {
            color = getColorForMood(aggregated)
            fillColor = color
            setDrawFilled(true)
            setDrawValues(false)
        }

        binding.radarChart.apply {
            clear()
            data = RadarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(emociones)
            xAxis.textSize = 12f
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

    private fun setupBarChart(aggregated: AggregatedEmotionSentiment) {
        Log.d("ReportFragment", "🔧 Configurando BarChart")
        val emocionesMap = mapOf(
            "Felicidad" to aggregated.emotion_probs_joy,
            "Rabia" to aggregated.emotion_probs_anger,
            "Tristeza" to aggregated.emotion_probs_sadness,
            "Disgusto" to aggregated.emotion_probs_disgust,
            "Miedo" to aggregated.emotion_probs_fear,
            "Neutral" to aggregated.emotion_probs_neutral,
            "Sorpresa" to aggregated.emotion_probs_surprise,
            "Confianza" to aggregated.emotion_probs_trust,
            "Anticipación" to aggregated.emotion_probs_anticipation
        )

        val sorted = emocionesMap.entries.sortedByDescending { it.value }.take(3)
        val entries = sorted.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
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
            clear()
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

    private fun setupPieChart(aggregated: AggregatedEmotionSentiment) {
        Log.d("ReportFragment", "🔧 Configurando PieChart")
        val entries = listOf(
            PieEntry(aggregated.sentiment_probs_positive.toFloat(), "Positivo"),
            PieEntry(aggregated.sentiment_probs_negative.toFloat(), "Negativo"),
            PieEntry(aggregated.sentiment_probs_neutral.toFloat(), "Neutral")
        )

        val pieDataSet = PieDataSet(entries, "").apply {
            setColors(
                Color.parseColor("#f4c0e5"),
                Color.parseColor("#84bcf6"),
                Color.parseColor("#a5dfea")
            )
            setDrawValues(false)
        }

        binding.pieChart.apply {
            clear()
            data = PieData(pieDataSet)
            centerText = "Sentimientos"
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            invalidate()
        }
    }

    private fun getColorForMood(aggregated: AggregatedEmotionSentiment): Int {
        val emocionesMap = mapOf(
            "Felicidad" to aggregated.emotion_probs_joy,
            "Rabia" to aggregated.emotion_probs_anger,
            "Tristeza" to aggregated.emotion_probs_sadness,
            "Disgusto" to aggregated.emotion_probs_disgust,
            "Miedo" to aggregated.emotion_probs_fear,
            "Neutral" to aggregated.emotion_probs_neutral,
            "Sorpresa" to aggregated.emotion_probs_surprise,
            "Confianza" to aggregated.emotion_probs_trust,
            "Anticipación" to aggregated.emotion_probs_anticipation
        )

        return when (emocionesMap.maxByOrNull { it.value }?.key) {
            "Tristeza", "Miedo", "Disgusto", "Rabia" -> Color.parseColor("#FF8A65")
            "Felicidad", "Confianza", "Sorpresa" -> Color.parseColor("#64B5F6")
            else -> Color.parseColor("#90A4AE")
        }
    }

    fun refreshChartsAfterDelay() {
        val TAG = "ReportFragment"
        Log.d(TAG, "🔄 refreshChartsAfterDelay: intentando refrescar UI con datos locales")

        val aggregated = preferences.getLast7DaysAggregated()
        if (aggregated != null && hasData(aggregated)) {
            Log.d(TAG, "✅ Datos encontrados en preferencias. Actualizando gráficos.")
            _binding?.apply {
                setupRadarChart(aggregated)
                setupBarChart(aggregated)
                setupPieChart(aggregated)
                tvNoDataMessage.visibility = View.GONE
                radarChart.visibility = View.VISIBLE
                barChart.visibility = View.VISIBLE
                pieChart.visibility = View.VISIBLE
            }
        } else {
            Log.w(TAG, "⚠️ No hay datos suficientes aún. Mostrando mensaje de espera.")
            _binding?.apply {
                tvNoDataMessage.text = "No hay datos suficientes aún"
                tvNoDataMessage.visibility = View.VISIBLE
                radarChart.visibility = View.GONE
                barChart.visibility = View.GONE
                pieChart.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
