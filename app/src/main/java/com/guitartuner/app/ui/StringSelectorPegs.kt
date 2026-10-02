package com.guitartuner.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitartuner.app.model.GuitarString
import com.guitartuner.app.model.TuningPreset

@Composable
fun StringSelectorPegs(
    tuning: TuningPreset,
    activeString: GuitarString?,
    selectedString: GuitarString?,
    isInTune: Boolean,
    onStringSelect: (GuitarString?) -> Unit,
    onPlayTone: (GuitarString) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector: AUTO vs MANUAL
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedString == null,
                onClick = { onStringSelect(null) },
                label = { Text("AUTO DETECT", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )

            if (selectedString != null) {
                Surface(
                    color = Color(0xFF263238),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "LOCKED TO ${selectedString.displayLabel}",
                        color = Color(0xFF80D8FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // String Pegs Grid / Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tuning.strings.forEach { guitarString ->
                val isCurrentMatch = (selectedString == null && activeString?.stringNumber == guitarString.stringNumber) ||
                        (selectedString?.stringNumber == guitarString.stringNumber)

                StringPegItem(
                    guitarString = guitarString,
                    isMatched = isCurrentMatch,
                    isInTune = isCurrentMatch && isInTune,
                    isSelected = selectedString?.stringNumber == guitarString.stringNumber,
                    onClick = {
                        if (selectedString?.stringNumber == guitarString.stringNumber) {
                            onStringSelect(null) // deselect -> Auto
                        } else {
                            onStringSelect(guitarString)
                        }
                    },
                    onPlayTone = { onPlayTone(guitarString) }
                )
            }
        }
    }
}

@Composable
fun StringPegItem(
    guitarString: GuitarString,
    isMatched: Boolean,
    isInTune: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPlayTone: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isInTune -> Color(0xFF00E676)
            isMatched -> Color(0xFF00E5FF)
            isSelected -> Color(0xFFFFD600)
            else -> Color(0xFF37474F)
        },
        label = "PegBorder"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isInTune -> Color(0xFF1B5E20)
            isMatched -> Color(0xFF004D40)
            isSelected -> Color(0xFF37474F)
            else -> Color(0xFF1E1E1E)
        },
        label = "PegBg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(2.dp, borderColor, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = guitarString.noteName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = "${guitarString.stringNumber}",
                    fontSize = 9.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Small target frequency label
        Text(
            text = "${guitarString.targetFreq.toInt()}Hz",
            fontSize = 9.sp,
            color = Color.Gray
        )

        // Hear Tone button
        IconButton(
            onClick = onPlayTone,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Hear tone",
                modifier = Modifier.size(14.dp),
                tint = Color(0xFF90A4AE)
            )
        }
    }
}
