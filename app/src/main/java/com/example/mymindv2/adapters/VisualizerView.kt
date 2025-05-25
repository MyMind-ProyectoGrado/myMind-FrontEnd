package com.example.mymindv2.adapters

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.mymindv2.R

class VisualizerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val linePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.mediumPurple)
        strokeWidth = 6f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND

    }

    private var amplitudes: MutableList<Float> = mutableListOf()
    private val maxAmplitudes = 50

    fun addAmplitude(amplitude: Float) {
        if (amplitudes.size >= maxAmplitudes) {
            amplitudes.removeAt(0)
        }
        amplitudes.add(amplitude)
        invalidate()
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthPerLine = width / maxAmplitudes.toFloat()
        val centerY = height / 2f
        val maxLineHeight = height * 0.9f

        amplitudes.forEachIndexed { index, amp ->
            // Aumentamos el factor multiplicador para resaltar más las diferencias grandes
            val normalized = if (amp > 0) (Math.log10(amp.toDouble()) * 16).toFloat() else 0f
            val lineHeight = normalized.coerceAtMost(maxLineHeight / 2f)

            val x = index * widthPerLine
            canvas.drawLine(x, centerY - lineHeight, x, centerY + lineHeight, linePaint)
        }
    }

    fun clear() {
        amplitudes.clear()
        invalidate()
    }
}
