package com.guitartuner.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CalibrationDialog(
    currentPitch: Float,
    onPitchChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var pitchSliderValue by remember { mutableFloatStateOf(currentPitch) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tuner Calibration", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "A4 Reference Frequency",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pitchSliderValue.roundToInt()} Hz",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF)
                    )
                    TextButton(onClick = { pitchSliderValue = 440.0f }) {
                        Text("Reset to 440Hz")
                    }
                }

                Slider(
                    value = pitchSliderValue,
                    onValueChange = { pitchSliderValue = it },
                    valueRange = 430.0f..450.0f,
                    steps = 19
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("430 Hz", fontSize = 11.sp, color = Color.Gray)
                    Text("432 Hz (Verdi)", fontSize = 11.sp, color = Color(0xFF80D8FF))
                    Text("440 Hz (Concert)", fontSize = 11.sp, color = Color(0xFF00E676))
                    Text("450 Hz", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0x33FFFFFF))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Detection Engine: YIN Normalized Autocorrelation with sub-cent parabolic refinement.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPitchChange(pitchSliderValue)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
