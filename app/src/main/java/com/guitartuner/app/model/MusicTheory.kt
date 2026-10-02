package com.guitartuner.app.model

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

data class GuitarString(
    val stringNumber: Int, // 1 to 6
    val noteName: String,  // e.g. "E"
    val octave: Int,       // e.g. 2
    val targetFreq: Float  // e.g. 82.41f
) {
    val displayLabel: String
        get() = "$stringNumber: $noteName$octave"
}

data class TuningPreset(
    val id: String,
    val name: String,
    val description: String,
    val strings: List<GuitarString>
)

data class NoteMatch(
    val noteName: String,
    val octave: Int,
    val detectedFreq: Float,
    val targetFreq: Float,
    val centsOff: Float,     // -50.0f to +50.0f
    val isInTune: Boolean,   // true if within +/- 3 cents
    val closestString: GuitarString? = null
) {
    val fullName: String
        get() = "$noteName$octave"
}

object MusicTheory {

    private val NOTE_NAMES_SHARP = arrayOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
    private val NOTE_NAMES_FLAT  = arrayOf("C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B")

    val STANDARD_GUITAR = TuningPreset(
        id = "standard",
        name = "Standard E",
        description = "E A D G B E (Standard 6-string)",
        strings = listOf(
            GuitarString(6, "E", 2, 82.41f),
            GuitarString(5, "A", 2, 110.00f),
            GuitarString(4, "D", 3, 146.83f),
            GuitarString(3, "G", 3, 196.00f),
            GuitarString(2, "B", 3, 246.94f),
            GuitarString(1, "E", 4, 329.63f)
        )
    )

    val DROP_D = TuningPreset(
        id = "drop_d",
        name = "Drop D",
        description = "D A D G B E (Heavy rock / metal)",
        strings = listOf(
            GuitarString(6, "D", 2, 73.42f),
            GuitarString(5, "A", 2, 110.00f),
            GuitarString(4, "D", 3, 146.83f),
            GuitarString(3, "G", 3, 196.00f),
            GuitarString(2, "B", 3, 246.94f),
            GuitarString(1, "E", 4, 329.63f)
        )
    )

    val DADGAD = TuningPreset(
        id = "dadgad",
        name = "DADGAD",
        description = "D A D G A D (Celtic / Folk modal)",
        strings = listOf(
            GuitarString(6, "D", 2, 73.42f),
            GuitarString(5, "A", 2, 110.00f),
            GuitarString(4, "D", 3, 146.83f),
            GuitarString(3, "G", 3, 196.00f),
            GuitarString(2, "A", 3, 220.00f),
            GuitarString(1, "D", 4, 293.66f)
        )
    )

    val HALF_STEP_DOWN = TuningPreset(
        id = "half_step_down",
        name = "Half-Step Down (E♭)",
        description = "E♭ A♭ D♭ G♭ B♭ E♭ (Hendrix / SRV / Slash)",
        strings = listOf(
            GuitarString(6, "E♭", 2, 77.78f),
            GuitarString(5, "A♭", 2, 103.83f),
            GuitarString(4, "D♭", 3, 138.59f),
            GuitarString(3, "G♭", 3, 185.00f),
            GuitarString(2, "B♭", 3, 233.08f),
            GuitarString(1, "E♭", 4, 311.13f)
        )
    )

    val OPEN_D = TuningPreset(
        id = "open_d",
        name = "Open D",
        description = "D F♯ A D F♯ A (Slide / Blues)",
        strings = listOf(
            GuitarString(6, "D", 2, 73.42f),
            GuitarString(5, "A", 2, 110.00f),
            GuitarString(4, "D", 3, 146.83f),
            GuitarString(3, "F♯", 3, 185.00f),
            GuitarString(2, "A", 3, 220.00f),
            GuitarString(1, "D", 4, 293.66f)
        )
    )

    val OPEN_G = TuningPreset(
        id = "open_g",
        name = "Open G",
        description = "D G D G B D (Rolling Stones / Blues)",
        strings = listOf(
            GuitarString(6, "D", 2, 73.42f),
            GuitarString(5, "G", 2, 98.00f),
            GuitarString(4, "D", 3, 146.83f),
            GuitarString(3, "G", 3, 196.00f),
            GuitarString(2, "B", 3, 246.94f),
            GuitarString(1, "D", 4, 293.66f)
        )
    )

    val BASS_STANDARD = TuningPreset(
        id = "bass_standard",
        name = "Bass Guitar (4-string)",
        description = "E A D G (Standard Bass)",
        strings = listOf(
            GuitarString(4, "E", 1, 41.20f),
            GuitarString(3, "A", 1, 55.00f),
            GuitarString(2, "D", 2, 73.42f),
            GuitarString(1, "G", 2, 98.00f)
        )
    )

    val UKULELE_STANDARD = TuningPreset(
        id = "ukulele_standard",
        name = "Ukulele Standard",
        description = "G C E A (Soprano / Concert)",
        strings = listOf(
            GuitarString(4, "G", 4, 392.00f),
            GuitarString(3, "C", 4, 261.63f),
            GuitarString(2, "E", 4, 329.63f),
            GuitarString(1, "A", 4, 440.00f)
        )
    )

    val ALL_PRESETS = listOf(
        STANDARD_GUITAR,
        DROP_D,
        HALF_STEP_DOWN,
        DADGAD,
        OPEN_D,
        OPEN_G,
        BASS_STANDARD,
        UKULELE_STANDARD
    )

    /**
     * Map any detected frequency to musical note, octave, and cents deviation.
     */
    fun findNote(
        frequency: Float,
        referencePitch: Float = 440.0f,
        useFlats: Boolean = false,
        activeTuning: TuningPreset = STANDARD_GUITAR,
        inTuneToleranceCents: Float = 3.0f
    ): NoteMatch {
        if (frequency <= 20.0f || frequency >= 4200.0f) {
            return NoteMatch(
                noteName = "-",
                octave = 0,
                detectedFreq = frequency,
                targetFreq = 0.0f,
                centsOff = 0.0f,
                isInTune = false,
                closestString = null
            )
        }

        // MIDI formula: 69 + 12 * log2(freq / refPitch)
        val midiFloat = 69.0 + 12.0 * (ln(frequency.toDouble() / referencePitch.toDouble()) / ln(2.0))
        val midiNote = midiFloat.roundToInt()
        val centsOff = ((midiFloat - midiNote) * 100.0).toFloat().coerceIn(-50.0f, 50.0f)

        val noteIndex = ((midiNote % 12) + 12) % 12
        val octave = (midiNote / 12) - 1

        val names = if (useFlats) NOTE_NAMES_FLAT else NOTE_NAMES_SHARP
        val noteName = names[noteIndex]

        val targetFreq = (referencePitch.toDouble() * 2.0.pow((midiNote - 69).toDouble() / 12.0)).toFloat()

        // Match to closest string in active tuning
        val closestString = activeTuning.strings.minByOrNull { str ->
            kotlin.math.abs(str.targetFreq - frequency)
        }

        return NoteMatch(
            noteName = noteName,
            octave = octave,
            detectedFreq = frequency,
            targetFreq = targetFreq,
            centsOff = centsOff,
            isInTune = kotlin.math.abs(centsOff) <= inTuneToleranceCents,
            closestString = closestString
        )
    }
}
