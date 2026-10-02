package com.guitartuner.app

import com.guitartuner.app.model.MusicTheory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MusicTheoryTest {

    @Test
    fun testStandardGuitarFrequencies() {
        // Low E2: 82.41 Hz
        val matchE2 = MusicTheory.findNote(82.41f)
        assertEquals("E", matchE2.noteName)
        assertEquals(2, matchE2.octave)
        assertTrue("E2 should be in tune", matchE2.isInTune)
        assertTrue("Cents should be near zero", abs(matchE2.centsOff) < 1.0f)

        // A2: 110.00 Hz
        val matchA2 = MusicTheory.findNote(110.00f)
        assertEquals("A", matchA2.noteName)
        assertEquals(2, matchA2.octave)
        assertTrue("A2 should be in tune", matchA2.isInTune)

        // D3: 146.83 Hz
        val matchD3 = MusicTheory.findNote(146.83f)
        assertEquals("D", matchD3.noteName)
        assertEquals(3, matchD3.octave)
        assertTrue("D3 should be in tune", matchD3.isInTune)

        // G3: 196.00 Hz
        val matchG3 = MusicTheory.findNote(196.00f)
        assertEquals("G", matchG3.noteName)
        assertEquals(3, matchG3.octave)
        assertTrue("G3 should be in tune", matchG3.isInTune)

        // B3: 246.94 Hz
        val matchB3 = MusicTheory.findNote(246.94f)
        assertEquals("B", matchB3.noteName)
        assertEquals(3, matchB3.octave)
        assertTrue("B3 should be in tune", matchB3.isInTune)

        // High E4: 329.63 Hz
        val matchE4 = MusicTheory.findNote(329.63f)
        assertEquals("E", matchE4.noteName)
        assertEquals(4, matchE4.octave)
        assertTrue("E4 should be in tune", matchE4.isInTune)
    }

    @Test
    fun testDropDFrequency() {
        // Low D2: 73.42 Hz
        val matchD2 = MusicTheory.findNote(73.42f, activeTuning = MusicTheory.DROP_D)
        assertEquals("D", matchD2.noteName)
        assertEquals(2, matchD2.octave)
        assertTrue("D2 should be in tune", matchD2.isInTune)
    }

    @Test
    fun testFlatAndSharpDeviation() {
        // Slightly flat E2 (81.5 Hz instead of 82.41 Hz)
        val flatMatch = MusicTheory.findNote(81.5f)
        assertEquals("E", flatMatch.noteName)
        assertEquals(2, flatMatch.octave)
        assertTrue("Expected negative cents for flat note", flatMatch.centsOff < 0)

        // Slightly sharp E2 (83.5 Hz instead of 82.41 Hz)
        val sharpMatch = MusicTheory.findNote(83.5f)
        assertEquals("E", sharpMatch.noteName)
        assertEquals(2, sharpMatch.octave)
        assertTrue("Expected positive cents for sharp note", sharpMatch.centsOff > 0)
    }

    @Test
    fun testConcertPitchA440() {
        val matchA440 = MusicTheory.findNote(440.0f)
        assertEquals("A", matchA440.noteName)
        assertEquals(4, matchA440.octave)
        assertEquals(0.0f, matchA440.centsOff, 0.05f)
        assertTrue(matchA440.isInTune)
    }
}
