package com.creativeblisslabs.notation.model

import kotlinx.serialization.Serializable

@Serializable
enum class HarmonicType {
    NONE,
    ROOT,
    THIRD,
    FIFTH,
    SEVENTH,
    TRIAD,
    SEVENTH_CHORD,
    EXTENSION,
    ALTERATION
}
