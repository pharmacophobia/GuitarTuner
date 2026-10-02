package com.guitartuner.app

import com.guitartuner.app.audio.PitchDetector
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class PitchDetectorTest {

    @Test
    fun testSyntheticSineWaveDetection() {
        val sampleRate = 44100
        val bufferSize = 4096
        val detector = PitchDetector(sampleRate, bufferSize)

        // Test frequencies: A2 (110 Hz), A4 (440 Hz), E2 (82.4 Hz)
        val testFrequencies = listOf(82.4f, 110.0f, 196.0f, 440.0f)

        for (targetFreq in testFrequencies) {
            val audioData = ShortArray(bufferSize)
            for (i in 0 until bufferSize) {
                val sample = sin(2.0 * PI * targetFreq * i / sampleRate)
                audioData[i] = (sample * 25000.0).toInt().toShort()
            }

            val result = detector.detectPitch(audioData, bufferSize)
            assertNotNull("Detection result should not be null for $targetFreq Hz", result)
            assertTrue("Detected frequency should be close to $targetFreq Hz but was ${result?.frequency}",
                abs((result?.frequency ?: 0f) - targetFreq) < 1.5f
            )
            assertTrue("Probability should be high for pure sine wave", (result?.probability ?: 0f) > 0.8f)
        }
    }

    @Test
    fun testSilenceRejection() {
        val sampleRate = 44100
        val bufferSize = 4096
        val detector = PitchDetector(sampleRate, bufferSize)

        val silenceData = ShortArray(bufferSize) // all zeros
        val result = detector.detectPitch(silenceData, bufferSize)

        assertNotNull(result)
        assertTrue("Silent input should report 0 Hz", (result?.frequency ?: 0f) == 0.0f)
    }
}
