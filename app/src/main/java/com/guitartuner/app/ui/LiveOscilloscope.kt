package com.guitartuner.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LiveOscilloscope(
    waveform: FloatArray,
    hasSignal: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
    ) {
        val w = size.width
        val h = size.height
        val midY = h / 2.0f

        // Draw centerline
        drawLine(
            color = Color(0x22FFFFFF),
            start = Offset(0.0f, midY),
            end = Offset(w, midY),
            strokeWidth = 1.dp.toPx()
        )

        if (waveform.size > 1 && hasSignal) {
            val path = Path()
            val step = w / (waveform.size - 1)
            val amplitudeScale = (h / 2.0f) * 0.9f

            path.moveTo(0.0f, midY - (waveform[0] * amplitudeScale).coerceIn(-midY, midY))
            for (i in 1 until waveform.size) {
                val x = i * step
                val y = midY - (waveform[i] * amplitudeScale).coerceIn(-midY, midY)
                path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = Color(0xFF00E5FF),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
