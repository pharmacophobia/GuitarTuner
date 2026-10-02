package com.guitartuner.app.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Process
import com.guitartuner.app.model.GuitarString
import com.guitartuner.app.model.MusicTheory
import com.guitartuner.app.model.NoteMatch
import com.guitartuner.app.model.TuningPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin

data class TunerState(
    val isListening: Boolean = false,
    val detectedFreq: Float = 0.0f,
    val matchedNote: NoteMatch = MusicTheory.findNote(0.0f),
    val rmsDb: Float = -100.0f,
    val waveform: FloatArray = FloatArray(64),
    val activeTuning: TuningPreset = MusicTheory.STANDARD_GUITAR,
    val targetString: GuitarString? = null, // null means Auto-Detect mode
    val referencePitch: Float = 440.0f,
    val isTonePlaying: Boolean = false,
    val inTuneStreak: Int = 0 // frames in tune for visual locked feedback
)

class AudioEngine(
    private val scope: CoroutineScope
) {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 4096
        private const val HOP_SIZE = 2048 // 50% overlap (~21.5 updates/sec)
    }

    private val pitchDetector = PitchDetector(SAMPLE_RATE, BUFFER_SIZE)
    private val isRecording = AtomicBoolean(false)
    private var recordThread: Thread? = null
    private var audioRecord: AudioRecord? = null
    private var toneJob: Job? = null

    private val _tunerState = MutableStateFlow(TunerState())
    val tunerState: StateFlow<TunerState> = _tunerState.asStateFlow()

    private var smoothedFreq = 0.0f
    private var currentStreak = 0

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (isRecording.getAndSet(true)) return

        val minBufSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val actualBufSize = maxOf(minBufSize, BUFFER_SIZE * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                actualBufSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                isRecording.set(false)
                return
            }

            audioRecord?.startRecording()
            _tunerState.value = _tunerState.value.copy(isListening = true)

            recordThread = Thread {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                val audioBuffer = ShortArray(BUFFER_SIZE)
                val ringBuffer = ShortArray(BUFFER_SIZE)
                var ringIndex = 0

                while (isRecording.get()) {
                    val read = audioRecord?.read(audioBuffer, 0, HOP_SIZE) ?: -1
                    if (read > 0) {
                        // Shift ring buffer and append new samples
                        System.arraycopy(ringBuffer, HOP_SIZE, ringBuffer, 0, BUFFER_SIZE - HOP_SIZE)
                        System.arraycopy(audioBuffer, 0, ringBuffer, BUFFER_SIZE - HOP_SIZE, HOP_SIZE)

                        val result = pitchDetector.detectPitch(ringBuffer, BUFFER_SIZE)
                        if (result != null) {
                            processPitchResult(result)
                        }
                    }
                }
            }.apply { start() }

        } catch (e: Exception) {
            e.printStackTrace()
            isRecording.set(false)
            _tunerState.value = _tunerState.value.copy(isListening = false)
        }
    }

    private fun processPitchResult(result: DetectionResult) {
        val rawFreq = result.frequency
        val currentState = _tunerState.value

        val finalFreq: Float
        if (rawFreq > 20.0f && result.probability > 0.65f) {
            // Smooth frequency when holding string, jump fast when changing note
            finalFreq = if (smoothedFreq > 0.0f && kotlin.math.abs(rawFreq - smoothedFreq) < 20.0f) {
                0.7f * smoothedFreq + 0.3f * rawFreq
            } else {
                rawFreq
            }
            smoothedFreq = finalFreq
        } else {
            // Low volume or silence: decay frequency smoothly
            finalFreq = 0.0f
            smoothedFreq = 0.0f
        }

        // Calculate note match
        val matched = if (finalFreq > 20.0f) {
            val baseMatch = MusicTheory.findNote(
                frequency = finalFreq,
                referencePitch = currentState.referencePitch,
                activeTuning = currentState.activeTuning
            )

            // If user manually locked onto a string, override cents relative to that string
            if (currentState.targetString != null) {
                val target = currentState.targetString
                val centsRelativeToTarget = (1200.0 * (kotlin.math.ln(finalFreq.toDouble() / target.targetFreq.toDouble()) / kotlin.math.ln(2.0)))
                    .toFloat().coerceIn(-50.0f, 50.0f)

                baseMatch.copy(
                    targetFreq = target.targetFreq,
                    centsOff = centsRelativeToTarget,
                    isInTune = kotlin.math.abs(centsRelativeToTarget) <= 3.0f,
                    closestString = target
                )
            } else {
                baseMatch
            }
        } else {
            MusicTheory.findNote(0.0f)
        }

        if (matched.isInTune && finalFreq > 20.0f) {
            currentStreak++
        } else {
            currentStreak = 0
        }

        _tunerState.value = currentState.copy(
            detectedFreq = finalFreq,
            matchedNote = matched,
            rmsDb = result.rmsDb,
            waveform = result.waveformPreview,
            inTuneStreak = currentStreak
        )
    }

    fun setTuning(tuning: TuningPreset) {
        _tunerState.value = _tunerState.value.copy(activeTuning = tuning, targetString = null)
    }

    fun setTargetString(string: GuitarString?) {
        _tunerState.value = _tunerState.value.copy(targetString = string)
    }

    fun setReferencePitch(pitchHz: Float) {
        _tunerState.value = _tunerState.value.copy(referencePitch = pitchHz)
    }

    /**
     * Play an acoustic reference tone for tuning by ear.
     */
    fun playReferenceTone(frequency: Float, durationMs: Long = 2000L) {
        toneJob?.cancel()
        toneJob = scope.launch(Dispatchers.IO) {
            _tunerState.value = _tunerState.value.copy(isTonePlaying = true)
            try {
                val sampleRate = 44100
                val numSamples = (durationMs * sampleRate / 1000).toInt()
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                val buffer = ShortArray(numSamples)
                val twoPiF = 2.0 * PI * frequency / sampleRate

                // Sine wave synthesis with smooth attack/decay envelope to prevent clicks
                val attackSamples = (sampleRate * 0.05).toInt()
                val decaySamples = (sampleRate * 0.15).toInt()

                for (i in 0 until numSamples) {
                    val rawSine = sin(twoPiF * i)
                    val envelope = when {
                        i < attackSamples -> i.toFloat() / attackSamples
                        i > numSamples - decaySamples -> (numSamples - i).toFloat() / decaySamples
                        else -> 1.0f
                    }
                    buffer[i] = (rawSine * envelope * 24000.0).toInt().toShort()
                }

                audioTrack.write(buffer, 0, numSamples)
                audioTrack.play()
                kotlinx.coroutines.delay(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _tunerState.value = _tunerState.value.copy(isTonePlaying = false)
            }
        }
    }

    fun stopListening() {
        isRecording.set(false)
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore teardown exceptions
        }
        audioRecord = null
        recordThread = null
        _tunerState.value = _tunerState.value.copy(isListening = false)
    }

    fun release() {
        stopListening()
        toneJob?.cancel()
    }
}
