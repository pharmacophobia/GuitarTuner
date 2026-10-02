package com.guitartuner.app.audio

import kotlin.math.sqrt

data class DetectionResult(
    val frequency: Float,
    val probability: Float,
    val rmsDb: Float,
    val waveformPreview: FloatArray
)

class PitchDetector(
    private val sampleRate: Int = 44100,
    private val bufferSize: Int = 4096,
    private val yinThreshold: Float = 0.15f
) {

    private val halfBufferSize = bufferSize / 2
    private val yinBuffer = FloatArray(halfBufferSize)

    // Range: ~40 Hz (Bass E1) to ~1200 Hz (High D6)
    private val minTau = (sampleRate / 1200).coerceAtLeast(10)
    private val maxTau = (sampleRate / 40).coerceAtMost(halfBufferSize - 2)

    fun detectPitch(audioData: ShortArray, length: Int): DetectionResult? {
        if (length < bufferSize) return null

        // 1. Calculate RMS Energy
        var sumSquares = 0.0
        val floatBuffer = FloatArray(length)
        for (i in 0 until length) {
            val sample = audioData[i] / 32768.0f
            floatBuffer[i] = sample
            sumSquares += sample * sample
        }
        val rms = sqrt(sumSquares / length).toFloat()
        val rmsDb = if (rms > 1e-5f) 20.0f * kotlin.math.log10(rms) else -100.0f

        // Silence / Noise gate (typical threshold: -45 dB)
        if (rms < 0.008f) {
            return DetectionResult(
                frequency = 0.0f,
                probability = 0.0f,
                rmsDb = rmsDb,
                waveformPreview = extractWaveform(floatBuffer, length, 64)
            )
        }

        // 2. YIN Difference function: d(tau) = sum((x[j] - x[j + tau])^2)
        yinBuffer[0] = 1.0f
        for (tau in 1 until halfBufferSize) {
            var diff = 0.0f
            for (j in 0 until halfBufferSize) {
                val delta = floatBuffer[j] - floatBuffer[j + tau]
                diff += delta * delta
            }
            yinBuffer[tau] = diff
        }

        // 3. Cumulative Mean Normalized Difference: d'(tau)
        var runningSum = 0.0f
        yinBuffer[0] = 1.0f
        for (tau in 1 until halfBufferSize) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = if (runningSum > 0.0f) {
                yinBuffer[tau] * tau / runningSum
            } else {
                1.0f
            }
        }

        // 4. Absolute Thresholding: Find first dip below threshold
        var tau = minTau
        while (tau < maxTau) {
            if (yinBuffer[tau] < yinThreshold) {
                // Find local minimum in this valley
                while (tau + 1 < maxTau && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tau++
                }
                break
            }
            tau++
        }

        // Fallback: If no point below threshold, find global minimum in valid range
        if (tau >= maxTau) {
            var bestTau = minTau
            var minVal = Float.MAX_VALUE
            for (t in minTau until maxTau) {
                if (yinBuffer[t] < minVal) {
                    minVal = yinBuffer[t]
                    bestTau = t
                }
            }
            if (minVal < 0.40f) {
                tau = bestTau
            } else {
                // Unpitchable / noise
                return DetectionResult(
                    frequency = 0.0f,
                    probability = 0.0f,
                    rmsDb = rmsDb,
                    waveformPreview = extractWaveform(floatBuffer, length, 64)
                )
            }
        }

        // 5. Parabolic Interpolation for Sub-Sample Peak Refinement
        val refinedTau = if (tau > 0 && tau < halfBufferSize - 1) {
            val s0 = yinBuffer[tau - 1]
            val s1 = yinBuffer[tau]
            val s2 = yinBuffer[tau + 1]
            val denominator = 2.0f * (s0 - 2.0f * s1 + s2)
            if (kotlin.math.abs(denominator) > 1e-6f) {
                val delta = (s0 - s2) / denominator
                tau + delta
            } else {
                tau.toFloat()
            }
        } else {
            tau.toFloat()
        }

        val estimatedFreq = if (refinedTau > 0.0f) sampleRate / refinedTau else 0.0f
        val probability = (1.0f - yinBuffer[tau]).coerceIn(0.0f, 1.0f)

        return DetectionResult(
            frequency = estimatedFreq,
            probability = probability,
            rmsDb = rmsDb,
            waveformPreview = extractWaveform(floatBuffer, length, 64)
        )
    }

    private fun extractWaveform(buffer: FloatArray, length: Int, targetPoints: Int): FloatArray {
        val result = FloatArray(targetPoints)
        val step = length / targetPoints
        for (i in 0 until targetPoints) {
            result[i] = buffer[i * step]
        }
        return result
    }
}
