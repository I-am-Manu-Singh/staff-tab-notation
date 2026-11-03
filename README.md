# Staff & Tab Notation Viewer (KMP)

[![Maven Central](https://img.shields.io/maven-central/v/com.creativeblisslabs/staff-tab-notation.svg)](https://central.sonatype.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-Android%20%7C%20iOS%20%7C%20Desktop-orange.svg)](https://kotlinlang.org/)

A production-grade, highly performant **Kotlin Multiplatform (Compose Multiplatform)** musical notation component for rendering both **Standard Music Staff (Treble, Bass, and Grand Staff)** and **Guitar / Bass Tablature (TAB)** in interactive, scrolling sheet-music views with synchronized playback cursor, real-time MIDI active-pitch highlights, custom color coding, chords, lyrics, annotations, complete musical articulation semantics, and a full file **Importer & Parsers** engine.

---

## ⚡ Highlights

- 🎼 **Dual Mode Support**: Standard Notation (Treble Clef, Bass Clef, Grand Staff), Guitar/Bass Tablature (TAB), or **Both simultaneously**.
- 📥 **Universal Importer (`ScoreImporter`)**:
  - Asynchronous coroutine-based importing on `Dispatchers.Default`.
  - Automatic duration calculation in milliseconds, measure counting, and note counting.
  - Returns type-safe `ScoreImportResult.Success` or `ScoreImportResult.Error`.
- 📂 **Multi-format Parsers Built-in**:
  - **MIDI (`.mid`, `.midi`)**: Quantizes timing jitter into clean musical grids, auto-detects triplets, dotted notes, key signatures, and splits piano tracks.
  - **MusicXML (`.musicxml`, `.xml`)**: Parses 3.1 & 4.0 partwise/timewise notations, tablature frets/strings, accidentals, ornaments, and lyrics.
  - **`UniversalScoreParser`**: Auto-detects file type from bytes or extension and converts directly to a `Score`.
- 🚀 **Kotlin Multiplatform**: Supports **Android**, **iOS** (x64, arm64, simulatorArm64), and **Desktop** (JVM).
- 🎹 **Intelligent Grand Staff Splitting**: Built-in `StaffAssignmentEngine` automatically balances high notes on Treble clef and low notes on Bass clef with cost scoring algorithms.
- 🔴 **Real-time Playback Head & Visualizer**: Accurate beat-level playback head tracking with current sounding pitch glow, active MIDI notes highlighting, and damper pedal states.
- 🎸 **Full Guitar Articulations**: Bends, Pre-bends, Releases, Hammer-ons, Pull-offs, Slides (up/down/legato), Dead notes, Barre chords, Harmonics (Natural, Artificial, Pinch, Tap), Whammy dips, and Pick directions.
- 📜 **Flexible Layout & Scrolling**: Supports both horizontal endless score scrolling and vertical system stacking.

---

## 📦 Installation

### 1. Add Maven Central in your `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

### 2. Add Dependency in `build.gradle.kts`

#### In a Kotlin Multiplatform Project (`commonMain`):
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.creativeblisslabs:staff-tab-notation:1.0.0")
        }
    }
}
```

#### In an Android-only project:
```kotlin
dependencies {
    implementation("com.creativeblisslabs:staff-tab-notation:1.0.0")
}
```

---

## 🚀 Quick Start

### 1. Import any MIDI or MusicXML File

```kotlin
import com.creativeblisslabs.notation.importer.ScoreImporter
import com.creativeblisslabs.notation.importer.ScoreImportResult

// In your ViewModel / CoroutineScope:
viewModelScope.launch {
    when (val result = ScoreImporter.importScore(fileBytes, "song.mid")) {
        is ScoreImportResult.Success -> {
            val score = result.score
            println("Loaded ${result.totalMeasures} measures (${result.calculatedDurationMs} ms)")
        }
        is ScoreImportResult.Error -> {
            println("Error: ${result.message}")
        }
    }
}
```

### 2. Render with `StaffAndTabNotationViewer`

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.creativeblisslabs.notation.model.*
import com.creativeblisslabs.notation.ui.StaffAndTabNotationViewer

@Composable
fun NotationViewerScreen(score: Score?) {
    // Format switcher: "Both", "Staff", or "Tab"
    var format by remember { mutableStateOf("Both") }

    StaffAndTabNotationViewer(
        score = score,
        playheadMeasure = 0,
        playheadOffset = 0.0f,
        activeMidiNotes = mapOf(60 to true), // Real-time key/fret highlights
        playingNote = 60,
        primaryColor = Color(0xFF6C63FF),
        currentFormat = format,
        onFormatChange = { format = it },
        showLetters = true,
        showLyrics = true,
        isSplitStaff = true,
        modifier = Modifier.fillMaxSize()
    )
}
```

---

## 🛠️ Complete API Reference & Manual

See the full [API Reference & Manual](API_REFERENCE.md) for exhaustive documentation of all parsers, importer APIs, data classes, parameters, and guitar articulation semantics.

---

## 📄 License

```text
Copyright 2026 Creative Bliss Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
