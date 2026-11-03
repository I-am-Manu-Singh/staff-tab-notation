# Staff & Tab Notation Viewer — Exhaustive API Reference & Manual

This manual provides complete and exhaustive documentation for all classes, composables, parsers, importers, and algorithms in `com.creativeblisslabs.notation`.

---

## Table of Contents
1. [Core Composable: `StaffAndTabNotationViewer`](#1-core-composable-staffandtabnotationviewer)
2. [Importer: `ScoreImporter` & `ScoreImportResult`](#2-importer-scoreimporter--scoreimportresult)
3. [Parsers: `UniversalScoreParser`, `MusicXmlParser`, `MidiScoreParser`](#3-parsers)
4. [Data Model: `Score`](#4-data-model-score)
5. [Data Model: `Measure`](#5-data-model-measure)
6. [Data Model: `ScoreNote`](#6-data-model-scorenote)
7. [Enums & Musical Qualifiers](#7-enums--musical-qualifiers)
   - [NoteDuration](#noteduration)
   - [Clef](#clef)
   - [Accidental](#accidental)
   - [MusicalArticulation](#musicalarticulation)
   - [TabArticulation](#tabarticulation)
   - [BendType & BendInfo](#bendtype--bendinfo)
   - [GuitarHarmonicType](#guitarharmonictype)
   - [OrnamentType](#ornamenttype)
   - [BarreInfo](#barreinfo)
   - [BeamInfo](#beaminfo)
   - [WhammyInfo & WhammyDipInfo](#whammyinfo--whammydipinfo)
   - [HairpinSpan & HairpinType](#hairpinspan--hairpintype)
   - [PedalEvent](#pedalevent)
   - [RestSymbol](#restsymbol)
8. [Engine: `StaffAssignmentEngine`](#8-engine-staffassignmentengine)
9. [Publishing Guide to Maven Central](#9-publishing-guide-to-maven-central)

---

## 1. Core Composable: `StaffAndTabNotationViewer`

```kotlin
package com.creativeblisslabs.notation.ui

@Composable
fun StaffAndTabNotationViewer(
    score: Score?,
    playheadMeasure: Int,
    playheadOffset: Float,
    activeMidiNotes: Map<Int, Boolean>,
    playingNote: Int?,
    primaryColor: Color,
    currentFormat: String,
    onFormatChange: (String) -> Unit,
    showLetters: Boolean = true,
    showLyrics: Boolean = true,
    isSplitStaff: Boolean = true,
    isVerticalScroll: Boolean = false,
    onVerticalScrollChange: ((Boolean) -> Unit)? = null,
    leftHandColor: Color = Color(0xFF6C63FF),
    rightHandColor: Color = Color(0xFF4A90E2),
    isLeftHandHidden: Boolean = false,
    isRightHandHidden: Boolean = false,
    enabledTrackIndices: Map<Int, Boolean> = emptyMap(),
    isPedalPressed: Boolean = false,
    showScoreHeader: Boolean = true,
    compactStaffMode: Boolean = false,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState()
)
```

### Parameter Reference:

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `score` | `Score?` | *(Required)* | The score structure containing title, metadata, measures, notes, and track settings. If `null`, an empty placeholder state is rendered. |
| `playheadMeasure` | `Int` | *(Required)* | 0-based index of the currently playing measure for the real-time playback cursor. |
| `playheadOffset` | `Float` | *(Required)* | Beat offset within the active measure (e.g. `1.5f` represents 1.5 beats from measure start). |
| `activeMidiNotes` | `Map<Int, Boolean>` | *(Required)* | Map of MIDI note numbers to their currently triggered state (`true` = active/pressed). Highlights corresponding noteheads on the staff and fret numbers on the tab in real time. |
| `playingNote` | `Int?` | *(Required)* | Currently audible lead pitch or MIDI note number; receives an active glowing indicator. |
| `primaryColor` | `Color` | *(Required)* | Theme accent color used for playhead line, measure boundaries, active highlights, and musical accidentals (`♯`, `♭`). |
| `currentFormat` | `String` | *(Required)* | Layout rendering mode: `"Both"` (renders both standard notation and tablature), `"Staff"` (notation staff only), or `"Tab"` (guitar tablature only). |
| `onFormatChange` | `(String) -> Unit` | *(Required)* | Callback dispatched when user toggles format switcher pills (between `"Both"`, `"Staff"`, and `"Tab"`). |
| `showLetters` | `Boolean` | `true` | When `true`, displays pitch letter names (`C`, `D`, `E`, etc.) inside or above noteheads. |
| `showLyrics` | `Boolean` | `true` | When `true`, displays vocal lyrics and syllable texts below the lowest staff lines. |
| `isSplitStaff` | `Boolean` | `true` | When `true`, automatically renders a piano Grand Staff (Treble + Bass) using intelligent clef assignment. When `false`, uses a single staff. |
| `isVerticalScroll` | `Boolean` | `false` | When `true`, wraps systems vertically page by page; when `false`, renders an endless horizontal timeline. |
| `onVerticalScrollChange` | `((Boolean) -> Unit)?` | `null` | Optional callback when vertical layout mode is toggled by user interaction. |
| `leftHandColor` | `Color` | `#6C63FF` | Accent color for left-hand piano notes / fingerings. |
| `rightHandColor` | `Color` | `#4A90E2` | Accent color for right-hand piano notes / fingerings. |
| `isLeftHandHidden` | `Boolean` | `false` | When `true`, visually filters out left-hand notes for isolated hand practice. |
| `isRightHandHidden` | `Boolean` | `false` | When `true`, visually filters out right-hand notes for isolated hand practice. |
| `enabledTrackIndices` | `Map<Int, Boolean>` | `emptyMap()` | Multi-track filter. Keys are track indices; values indicate visibility. If empty, all tracks render. |
| `isPedalPressed` | `Boolean` | `false` | Damper sustain pedal status indicator. |
| `showScoreHeader` | `Boolean` | `true` | Controls whether title, artist, tempo, and time signature header banner is visible. |
| `compactStaffMode` | `Boolean` | `false` | Reduces staff spacing and margins for compact UI viewports. |
| `modifier` | `Modifier` | `Modifier` | Standard Compose modifier applied to root container. |
| `scrollState` | `ScrollState` | `rememberScrollState()` | Scroll state for synchronizing or saving programmatic scroll positions. |

---

## 2. Importer: `ScoreImporter` & `ScoreImportResult`

Package: `com.creativeblisslabs.notation.importer`

High-level asynchronous importer that ingests raw bytes, auto-detects file type, computes total duration in milliseconds, counts measures and notes, and returns a sealed result:

```kotlin
sealed class ScoreImportResult {
    data class Success(
        val score: Score,
        val fileName: String,
        val calculatedDurationMs: Long,
        val totalMeasures: Int,
        val totalNotes: Int
    ) : ScoreImportResult()

    data class Error(
        val fileName: String,
        val exception: Throwable,
        val message: String
    ) : ScoreImportResult()
}

object ScoreImporter {
    suspend fun importScore(
        bytes: ByteArray,
        fileName: String = "Imported Score",
        uriString: String? = null
    ): ScoreImportResult

    fun importScoreSync(
        bytes: ByteArray,
        fileName: String = "Imported Score",
        uriString: String? = null
    ): Score?
}
```

### Usage Example:
```kotlin
viewModelScope.launch {
    when (val result = ScoreImporter.importScore(byteArray, "chopin_nocturne.mid")) {
        is ScoreImportResult.Success -> {
            println("Score loaded: ${result.score.title}, Duration: ${result.calculatedDurationMs}ms, Measures: ${result.totalMeasures}")
            viewerScore = result.score
        }
        is ScoreImportResult.Error -> {
            println("Import failed: ${result.message}")
        }
    }
}
```

---

## 3. Parsers

Package: `com.creativeblisslabs.notation.parser`

### `UniversalScoreParser`
Auto-detects file format via header magic bytes or file extension:
```kotlin
object UniversalScoreParser {
    fun parse(bytes: ByteArray, fileName: String, uriString: String? = null): Score?
}
```

### `MusicXmlParser`
Direct parser for MusicXML 3.1 & 4.0 (partwise and timewise XML strings):
```kotlin
object MusicXmlParser {
    fun parse(xmlString: String, defaultTitle: String = "Imported MusicXML"): Score?
}
```

### `MidiScoreParser`
Parses Standard MIDI Files (`.mid`, `.midi`), quantizing human performance jitter into musical grids, detecting triplets, dotted durations, key signatures, and cross-bar ties:
```kotlin
object MidiScoreParser {
    fun parse(bytes: ByteArray, defaultTitle: String = "Imported Score"): Score?
}
```

### `MidiFileParser`
Polyglot fallback parser combining MIDI binary parsing and MusicXML stream parsing:
```kotlin
object MidiFileParser {
    fun parse(bytes: ByteArray, defaultTitle: String = "Imported Score"): Score?
}
```

---

## 4. Data Model: `Score`

Represents an entire musical work or composition:

```kotlin
@Serializable
data class Score(
    val title: String = "",
    val artist: String = "",
    val timeSignatureNumerator: Int = 4,
    val timeSignatureDenominator: Int = 4,
    val bpm: Int = 120,
    val measures: List<Measure> = emptyList(),
    val midiTracks: List<MidiTrackInfo> = emptyList(),
    val totalTicks: Long = 0L,
    val isMultiTrack: Boolean = false,
    val initialKeySignature: Int = 0,
    val tempoChanges: Map<Int, Int> = emptyMap(),
    val timeSignatureChanges: Map<Int, Pair<Int, Int>> = emptyMap(),
    val keySignatureChanges: Map<Int, Int> = emptyMap(),
    val isPercussion: Boolean = false,
    val isSingleStaff: Boolean = false,
    val trackClefs: Map<Int, Clef> = emptyMap(),
    val timeSignatureSymbol: TimeSignatureSymbol = TimeSignatureSymbol.NONE,
    val customTempoSymbols: Map<Int, String> = emptyMap(),
    val tempoChangeTypes: Map<Int, TempoChangeType> = emptyMap(),
    val tripletsBeamed: Boolean = true,
    val restsFollowTimeSignature: Boolean = true,
    val hideTiedAccidentals: Boolean = true,
    val showMeasureNumbers: Boolean = true,
    val systemBreakInterval: Int = 4,
    val measureWidthOverrides: Map<Int, Float> = emptyMap(),
    val defaultClef: Clef = Clef.TREBLE
)
```

---

## 5. Data Model: `Measure`

Represents an individual measure (bar) in musical time:

```kotlin
@Serializable
data class Measure(
    val number: Int,
    val notes: List<ScoreNote> = emptyList(),
    val timeSignatureNumerator: Int = 4,
    val timeSignatureDenominator: Int = 4,
    val bpm: Int = 120,
    val keySignature: Int = 0,  // Positive = Sharps, Negative = Flats (e.g. 2 = D Major / B Minor)
    val repeatStart: Boolean = false,
    val repeatEnd: Boolean = false,
    val repeatCount: Int = 0,
    val isFirstMeasure: Boolean = false,
    val isDoubleBar: Boolean = false,
    val isFinalBar: Boolean = false,
    val barres: List<BarreInfo> = emptyList(),
    val whammyBars: List<WhammyInfo> = emptyList(),
    val hairpins: List<HairpinSpan> = emptyList(),
    val pedalEvents: List<PedalEvent> = emptyList(),
    val rests: List<RestSymbol> = emptyList(),
    val timeSignatureSymbol: TimeSignatureSymbol = TimeSignatureSymbol.NONE,
    val tempoChangeType: TempoChangeType = TempoChangeType.NONE,
    val tempoText: String? = null,
    val width: Float = 0f
)
```

---

## 6. Data Model: `ScoreNote`

Encapsulates all attributes of a single musical note, chord member, or guitar fretting event:

```kotlin
@Serializable
data class ScoreNote(
    val midiNote: Int,                         // MIDI pitch number (e.g. 60 = Middle C, 40 = Low E)
    val duration: NoteDuration = NoteDuration.QUARTER,
    val position: Float = 0f,                  // Beat offset within the measure (0.0f = Beat 1)
    val stringNumber: Int = 0,                 // 1-based guitar string number (1 = High E, 6 = Low E)
    val fretNumber: Int = -1,                  // Fret index (0 = open, -1 = unassigned)
    val isTied: Boolean = false,               // Note tie curve to subsequent note
    val isDotted: Boolean = false,             // Augmentation dot (+50% duration)
    val isDoubleDotted: Boolean = false,       // Double augmentation dot (+75% duration)
    val isTriplet: Boolean = false,            // Triplet grouping
    val articulation: MusicalArticulation = MusicalArticulation.NONE,
    val tabArticulation: TabArticulation = TabArticulation.NONE,
    val bendInfo: BendInfo? = null,            // Pitch bend curve information
    val clef: Clef = Clef.TREBLE,              // Target staff clef
    val accidental: Accidental = Accidental.NONE,
    val ornament: OrnamentType = OrnamentType.NONE,
    val isRest: Boolean = false,               // Rest indicator
    val isGhostNote: Boolean = false,          // Rendered enclosed in parentheses: (5)
    val isDeadNote: Boolean = false,           // Rendered with an 'X' notehead
    val harmonicType: GuitarHarmonicType = GuitarHarmonicType.NONE,
    val harmonicFret: Float = 0f,
    val lyrics: String? = null,                // Lyric word or syllable attached to this note
    val pickDirection: PickDirection = PickDirection.NONE,
    val chordName: String? = null,             // Chord symbol banner (e.g. "Am7", "Cadd9")
    val octaveShift: OctaveShift = OctaveShift.NONE,
    val isLeftHand: Boolean = false,           // Piano left hand notation routing
    val isGraceNote: Boolean = false,
    val graceNoteType: String? = null,
    val finger: Int = 0,                       // Fingering number (1..5)
    val whammyDip: WhammyDipInfo? = null,
    val crossStaff: Boolean = false,
    val beamInfo: BeamInfo? = null,
    val arpeggio: ArpeggioDirection = ArpeggioDirection.NONE
)
```

---

## 7. Enums & Musical Qualifiers

### `NoteDuration`
Specifies standard duration values with beat fractions:
```kotlin
enum class NoteDuration(val beats: Float) {
    LONGA(16.0f),
    BREVE(8.0f),
    WHOLE(4.0f),
    HALF(2.0f),
    QUARTER(1.0f),
    EIGHTH(0.5f),
    SIXTEENTH(0.25f),
    THIRTY_SECOND(0.125f),
    SIXTY_FOURTH(0.0625f)
}
```

### `Clef`
Standard musical clefs:
```kotlin
enum class Clef {
    TREBLE, BASS, ALTO, TENOR
}
```

### `Accidental`
Key alterations and microtonal accidentals:
```kotlin
enum class Accidental {
    NONE, NATURAL, SHARP, FLAT, DOUBLE_SHARP, DOUBLE_FLAT, QUARTER_SHARP, THREE_QUARTER_SHARP, QUARTER_FLAT, THREE_QUARTER_FLAT
}
```

### `MusicalArticulation`
Expressive performance markings rendered above/below noteheads:
```kotlin
enum class MusicalArticulation {
    NONE, STACCATO, STACCATISSIMO, ACCENT, TENUTO, MARCATO
}
```

### `TabArticulation`
Guitar and fretted instrument performance techniques:
```kotlin
enum class TabArticulation {
    NONE,
    HAMMER_ON,
    PULL_OFF,
    SLIDE_UP,
    SLIDE_DOWN,
    LEGATO_SLIDE_UP,
    LEGATO_SLIDE_DOWN,
    TAP,
    RELEASE,
    DEAD_NOTE
}
```

### `BendType` & `BendInfo`
Guitar string bending specifications:
```kotlin
enum class BendType {
    BEND, PRE_BEND, BEND_RELEASE, PRE_BEND_RELEASE
}

data class BendInfo(
    val type: BendType = BendType.BEND,
    val toneValue: Float = 1.0f // 0.5 = Half step, 1.0 = Full step, 1.5 = 1 1/2 steps
)
```

### `GuitarHarmonicType`
Fretboard harmonic acoustic modes:
```kotlin
enum class GuitarHarmonicType(val label: String) {
    NONE(""),
    NATURAL("N.H."),
    ARTIFICIAL("A.H."),
    PINCH("P.H."),
    TAP("T.H.")
}
```

### `BarreInfo`
Guitar fretboard barre indicators:
```kotlin
data class BarreInfo(
    val startMeasure: Int,
    val endMeasure: Int,
    val fret: Int,
    val romanNumeral: String
)
```

### `BeamInfo`
Rhythmic beam grouping for eighth and sixteenth note groupings:
```kotlin
data class BeamInfo(
    val beamGroupId: Int,
    val beamIndex: Int,
    val totalBeams: Int,
    val isBeamStart: Boolean,
    val isBeamEnd: Boolean
)
```

### `WhammyInfo` & `WhammyDipInfo`
Tremolo arm articulation curves:
```kotlin
enum class WhammyType {
    DIVE, DIP, HOLD, RELEASE
}

data class WhammyInfo(
    val type: WhammyType,
    val depthSteps: Float,
    val startBeat: Float,
    val durationBeats: Float
)
```

### `HairpinSpan` & `HairpinType`
Dynamic crescendo and decrescendo hairpins:
```kotlin
enum class HairpinType {
    CRESCENDO, DECRESCENDO
}

data class HairpinSpan(
    val type: HairpinType,
    val startBeat: Float,
    val durationBeats: Float
)
```

---

## 8. Engine: `StaffAssignmentEngine`

Package: `com.creativeblisslabs.notation.engine`

Automatically computes optimal piano grand staff assignment (splitting between Bass Clef and Treble Clef):

```kotlin
object StaffAssignmentEngine {
    /**
     * Takes a list of ScoreNote and returns a Map indicating whether
     * each note belongs to the Treble Clef (true) or Bass Clef (false).
     */
    fun assign(notes: List<ScoreNote>): Map<ScoreNote, Boolean>
}
```

---

## 9. Publishing Guide to Maven Central

### Requirements:
1. Sonatype Central Portal Account (`central.sonatype.com`).
2. GPG Key Pair for artifact signing.
3. Your verified group domain namespace (e.g. `com.creativeblisslabs`).

### Local Staging Deployment:
```bash
./gradlew publishAllPublicationsToLocalStagingRepository
```

### Direct Release Deployment:
Set your credentials in `~/.gradle/gradle.properties`:
```properties
ossrhUsername=YOUR_SONATYPE_TOKEN_USERNAME
ossrhPassword=YOUR_SONATYPE_TOKEN_PASSWORD
signingKey=YOUR_GPG_PRIVATE_KEY_ARMORED
signingPassword=YOUR_GPG_PASSPHRASE
```
And execute:
```bash
./gradlew publishAllPublicationsToOSSRHRepository
```
