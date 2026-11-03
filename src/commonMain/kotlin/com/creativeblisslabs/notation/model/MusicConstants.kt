package com.creativeblisslabs.notation.model

object MusicConstants {
    val NOTE_LABELS: List<String> = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val CHROMATIC_TO_DIATONIC_STEPS = intArrayOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6)
    val GUITAR_OPEN_STRINGS = listOf(40, 45, 50, 55, 59, 64)

    /**
     * Converts a MIDI pitch note into its diatonic staff position step (C4 = 28, middle C).
     */
    fun getDiatonicStep(midiNote: Int): Int {
        val octave = (midiNote / 12) - 1
        val stepInOctave = CHROMATIC_TO_DIATONIC_STEPS[((midiNote % 12) + 12) % 12]
        return (octave * 7) + stepInOctave
    }
}

