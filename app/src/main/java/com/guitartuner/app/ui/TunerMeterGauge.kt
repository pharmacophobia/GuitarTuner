package com.guitartuner.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun TunerMeterGauge(
    centsOff: Float,
    isInTune: Boolean,
    hasSignal: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedCents by animateFloatAsState(
        targetValue = if (hasSignal) centsOff else 0.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NeedleCents"
    )

    val meterColor by animateColorAsState(
        targetValue = when {
            !hasSignal -> Color(0xFF555555)
            isInTune -> Color(0xFF00E676) // Radiant Green
            abs(centsOff) < 12.0f -> Color(0xFFFFB300) // Amber
            else -> Color(0xFFFF3D00) // Red Flat/Sharp
        },
        label = "MeterColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2.0f
                val baselineY = h - 20.0f

                // 1. Draw Background Track Line
                drawLine(
                    color = Color(0xFF2A2A2A),
                    start = Offset(40.0f, baselineY),
                    end = Offset(w - 40.0f, baselineY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 2. Draw Center In-Tune Sweet Spot Zone
                val zoneWidth = (w - 80.0f) * (6.0f / 100.0f) // +/- 3 cents
                drawLine(
                    color = if (isInTune && hasSignal) Color(0x6600E676) else Color(0x3300E676),
                    start = Offset(centerX - zoneWidth / 2, baselineY),
                    end = Offset(centerX + zoneWidth / 2, baselineY),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 3. Draw Graduation Ticks (-50 to +50 cents in steps of 10)
                val totalRange = w - 80.0f
                for (cents in -50..50 step 5) {
                    val progress = (cents + 50) / 100.0f
                    val tickX = 40.0f + progress * totalRange
                    val isMajor = cents % 10 == 0
                    val isCenter = cents == 0

                    val tickHeight = when {
                        isCenter -> 28.dp.toPx()
                        isMajor -> 18.dp.toPx()
                        else -> 10.dp.toPx()
                    }

                    val tickColor = when {
                        isCenter -> meterColor
                        abs(cents) <= 5 -> Color(0xFF81C784)
                        isMajor -> Color(0xFF757575)
                        else -> Color(0xFF424242)
                    }

                    drawLine(
                        color = tickColor,
                        start = Offset(tickX, baselineY - tickHeight),
                        end = Offset(tickX, baselineY),
                        strokeWidth = if (isCenter) 3.dp.toPx() else 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 4. Draw Animated Indicator Needle
                val needleProgress = ((animatedCents + 50.0f) / 100.0f).coerceIn(0.0f, 1.0f)
                val needleX = 40.0f + needleProgress * totalRange

                // Needle shadow / glow
                if (hasSignal) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(meterColor.copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(needleX, baselineY - 45.dp.toPx()),
                            radius = 32.dp.toPx()
                        ),
                        radius = 32.dp.toPx(),
                        center = Offset(needleX, baselineY - 45.dp.toPx())
                    )
                }

                // Needle line
                drawLine(
                    color = meterColor,
                    start = Offset(needleX, baselineY - 55.dp.toPx()),
                    end = Offset(needleX, baselineY + 8.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Needle pivot circle
                drawCircle(
                    color = meterColor,
                    radius = 6.dp.toPx(),
                    center = Offset(needleX, baselineY)
                )
            }
        }

        // Cents readout
        Row(
            modifier = Modifier.padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (hasSignal) {
                    when {
                        isInTune -> "IN TUNE (±${"%.1f".format(abs(centsOff))}¢)"
                        centsOff < 0 -> "FLAT (${"%.1f".format(centsOff)}¢)"
                        else -> "SHARP (+${"%.1f".format(centsOff)}¢)"
                    }
                } else {
                    "PLUCK A STRING"
                },
                color = meterColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
