package com.guitartuner.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitartuner.app.model.MusicTheory
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    viewModel: TunerViewModel,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit
) {
    val state by viewModel.tunerState.collectAsState()
    var showTuningMenu by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }

    val hasSignal = state.detectedFreq > 20.0f
    val isInTune = state.matchedNote.isInTune && hasSignal

    val mainAccentColor by animateColorAsState(
        targetValue = when {
            !hasSignal -> Color(0xFF757575)
            isInTune -> Color(0xFF00E676)
            abs(state.matchedNote.centsOff) < 12.0f -> Color(0xFFFFB300)
            else -> Color(0xFFFF3D00)
        },
        label = "AccentColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Guitar Tuner Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    // Tuning Preset Selector Dropdown
                    Box {
                        FilledTonalButton(
                            onClick = { showTuningMenu = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(state.activeTuning.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Tuning")
                        }

                        DropdownMenu(
                            expanded = showTuningMenu,
                            onDismissRequest = { showTuningMenu = false }
                        ) {
                            MusicTheory.ALL_PRESETS.forEach { preset ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(preset.name, fontWeight = FontWeight.Bold)
                                            Text(preset.description, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setTuning(preset)
                                        showTuningMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Calibration Dialog Button
                    IconButton(onClick = { showCalibrationDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Calibration")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Permission Banner (if needed)
            if (!hasMicPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Microphone Permission Required", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Needed to listen to acoustic instrument frequency in real-time.", fontSize = 11.sp, color = Color(0xFFFFCDD2))
                        }
                        Button(
                            onClick = onRequestMicPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Enable", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Big Bold Note Display Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Target String / Note Header
                Text(
                    text = if (state.targetString != null) {
                        "Target: ${state.targetString?.displayLabel} (${state.targetString?.targetFreq?.toInt()} Hz)"
                    } else {
                        "Tuning: ${state.activeTuning.name} (Auto-Detect)"
                    },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Giant Circular Note Platter
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(mainAccentColor.copy(alpha = 0.25f), Color(0xFF1A1A1A))
                            )
                        )
                        .border(3.dp, mainAccentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = if (hasSignal) state.matchedNote.noteName else "-",
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Black,
                                color = if (hasSignal) mainAccentColor else Color.Gray
                            )
                            if (hasSignal && state.matchedNote.octave > 0) {
                                Text(
                                    text = "${state.matchedNote.octave}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = mainAccentColor.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direction Advice Badge
                Surface(
                    color = when {
                        !hasSignal -> Color(0xFF262626)
                        isInTune -> Color(0xFF1B5E20)
                        state.matchedNote.centsOff < 0 -> Color(0xFF4E342E)
                        else -> Color(0xFF3E2723)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            !hasSignal -> "PLUCK STRING"
                            isInTune -> "PERFECT IN TUNE 🎯"
                            state.matchedNote.centsOff < 0 -> "TUNE UP ↑ (FLAT)"
                            else -> "TUNE DOWN ↓ (SHARP)"
                        },
                        color = when {
                            !hasSignal -> Color.Gray
                            isInTune -> Color(0xFFB9F6CA)
                            else -> Color(0xFFFFCCBC)
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Frequency Numerical Readout
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasSignal) "${"%.1f".format(state.detectedFreq)} Hz" else "--.- Hz",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Text(
                        text = if (hasSignal && state.matchedNote.targetFreq > 0.0f) {
                            "Target: ${"%.1f".format(state.matchedNote.targetFreq)} Hz"
                        } else {
                            "Ref: ${state.referencePitch.toInt()} Hz"
                        },
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Central Animated Meter Gauge
            TunerMeterGauge(
                centsOff = state.matchedNote.centsOff,
                isInTune = isInTune,
                hasSignal = hasSignal,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Interactive String Pegs
            StringSelectorPegs(
                tuning = state.activeTuning,
                activeString = state.matchedNote.closestString,
                selectedString = state.targetString,
                isInTune = isInTune,
                onStringSelect = { viewModel.selectString(it) },
                onPlayTone = { viewModel.playStringTone(it) }
            )

            // Real-Time Audio Oscilloscope Waveform Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Acoustic Input Signal", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        if (hasSignal) "${state.rmsDb.toInt()} dB" else "Silent",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                LiveOscilloscope(
                    waveform = state.waveform,
                    hasSignal = hasSignal
                )
            }
        }

        // Calibration Dialog
        if (showCalibrationDialog) {
            CalibrationDialog(
                currentPitch = state.referencePitch,
                onPitchChange = { viewModel.setReferencePitch(it) },
                onDismiss = { showCalibrationDialog = false }
            )
        }
    }
}
