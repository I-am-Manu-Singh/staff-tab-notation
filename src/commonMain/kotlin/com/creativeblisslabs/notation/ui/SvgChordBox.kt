package com.creativeblisslabs.notation.ui

import com.creativeblisslabs.notation.model.RealChordPosition
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * High fidelity SVG-style chord diagram inswred by tombatossals/react-chords and optilude/guitar-practice.
 * Renders vertical string neck grids, nut vs fret labels, barre chords with pills, muted strings ('✕'),
 * open strings ('○'), finger indicators, and tuning notes.
 */
@Composable
fun SvgChordBox(

    chord: RealChordPosition,
    chordName: String = "",
    instrument: String = "guitar", // "guitar" or "ukulele"
    strings: Int = if (instrument.lowercase().contains("uke")) 4 else 6,
    fretsOnChord: Int = 4,
    accentColor: Color = Color(0xFFFF9E1B),
    cardBackground: Color = Color(0xFF1E222D),
    width: Dp = 130.dp,
    height: Dp = 160.dp,
    lite: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val tuning = remember(strings) {
        if (strings == 4) listOf("G", "C", "E", "A") else listOf("E", "A", "D", "G", "B", "e")
    }

    Surface(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = cardBackground,
        border = BorderStroke(1.dp, Color(0xFF2C3242))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (chordName.isNotBlank()) {
                Text(
                    text = chordName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }

            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val canvasW = size.width
                val canvasH = size.height

                // Grid bounding box
                val left = 24f
                val right = canvasW - 24f
                val top = 22f
                val bottom = canvasH - 20f

                val gridW = right - left
                val gridH = bottom - top

                val stringSpacing = gridW / (strings - 1).coerceAtLeast(1)
                val fretSpacing = gridH / fretsOnChord.coerceAtLeast(1)

                val baseFret = chord.baseFret.coerceAtLeast(1)
                val isNut = baseFret == 1

                // 1. Draw Nut or Base Fret label
                if (isNut) {
                    drawLine(
                        color = Color(0xFFECEFF4),
                        start = Offset(left, top),
                        end = Offset(right, top),
                        strokeWidth = 3.5f.dp.toPx()
                    )
                } else {
                    // Draw base fret label to the left of fret 1 (e.g. "3fr")
                    val baseLayout = textMeasurer.measure(
                        "${baseFret}fr",
                        style = TextStyle(color = Color(0xFFC8CDD7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    )
                    drawText(
                        textLayoutResult = baseLayout,
                        topLeft = Offset(left - 4f - baseLayout.size.width.toFloat(), top + fretSpacing * 0.7f - baseLayout.size.height.toFloat() / 2f)
                    )
                    // Regular top fret wire
                    drawLine(
                        color = Color(0xFF70788C),
                        start = Offset(left, top),
                        end = Offset(right, top),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 2. Horizontal Fret Lines
                for (f in 1..fretsOnChord) {
                    val y = top + f * fretSpacing
                    drawLine(
                        color = Color(0xFF52596C),
                        start = Offset(left, y),
                        end = Offset(right, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 3. Vertical String Lines
                for (s in 0 until strings) {
                    val x = left + s * stringSpacing
                    val gauge = 0.8f + (strings - 1 - s) * 0.25f
                    drawLine(
                        color = Color(0xFF949EB2),
                        start = Offset(x, top),
                        end = Offset(x, bottom),
                        strokeWidth = gauge.dp.toPx()
                    )

                    // Tuning note indicator below each string
                    if (!lite && s < tuning.size) {
                        val tuneLayout = textMeasurer.measure(
                            tuning[s],
                            style = TextStyle(color = Color(0xFFA0B4C6), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textLayoutResult = tuneLayout,
                            topLeft = Offset(x - tuneLayout.size.width.toFloat() / 2f, bottom + 14f - tuneLayout.size.height.toFloat() / 2f)
                        )
                    }
                }

                // 4. Open / Muted string symbols at headstock (top)
                chord.frets.forEachIndexed { sIdx, fret ->
                    if (sIdx < strings) {
                        val x = left + sIdx * stringSpacing
                        if (fret == -1) {
                            // Muted String '✕'
                            val muteLayout = textMeasurer.measure(
                                "✕",
                                style = TextStyle(color = Color(0xFFEF5350), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                            drawText(
                                textLayoutResult = muteLayout,
                                topLeft = Offset(x - muteLayout.size.width.toFloat() / 2f, top - 7f - muteLayout.size.height.toFloat() / 2f)
                            )
                        } else if (fret == 0) {
                            // Open String Ring '○'
                            drawCircle(
                                color = Color.White,
                                radius = 3.5f.dp.toPx(),
                                center = Offset(x, top - 9f),
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                        }
                    }
                }

                // 5. Barre Chord Pill
                if (chord.barres.isNotEmpty()) {
                    chord.barres.forEach { barreFret ->
                        val matchingIndices = chord.frets.indices.filter { chord.frets[it] == barreFret }
                        if (matchingIndices.isNotEmpty() && barreFret in 1..fretsOnChord) {
                            val minStr = matchingIndices.minOrNull() ?: 0
                            val maxStr = matchingIndices.maxOrNull() ?: (strings - 1)
                            val startX = left + minStr * stringSpacing
                            val endX = left + maxStr * stringSpacing
                            val barreY = top + (barreFret - 0.5f) * fretSpacing

                            val pillH = 14f.dp.toPx()
                            drawRoundRect(
                                color = accentColor,
                                topLeft = Offset(startX - 6.dp.toPx(), barreY - pillH / 2f),
                                size = Size((endX - startX) + 12.dp.toPx(), pillH),
                                cornerRadius = CornerRadius(pillH / 2f)
                            )

                            // Barre finger text if present
                            val firstFinger = chord.fingers.getOrNull(minStr) ?: 1
                            if (!lite && firstFinger > 0) {
                                val barreLayout = textMeasurer.measure(
                                    "$firstFinger",
                                    style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                )
                                drawText(
                                    textLayoutResult = barreLayout,
                                    topLeft = Offset((startX + endX) / 2f - barreLayout.size.width.toFloat() / 2f, barreY - barreLayout.size.height.toFloat() / 2f)
                                )
                            }
                        }
                    }
                }

                // 6. Fretted Note Dots (excluding barres)
                chord.frets.forEachIndexed { sIdx, fret ->
                    if (sIdx < strings && fret > 0 && fret !in chord.barres && fret <= fretsOnChord) {
                        val cx = left + sIdx * stringSpacing
                        val cy = top + (fret - 0.5f) * fretSpacing
                        val dotRadius = 6.5f.dp.toPx()

                        // Warm glow
                        drawCircle(
                            color = accentColor.copy(alpha = 0.35f),
                            radius = dotRadius + 2.dp.toPx(),
                            center = Offset(cx, cy)
                        )

                        // Main filled dot
                        drawCircle(
                            color = accentColor,
                            radius = dotRadius,
                            center = Offset(cx, cy)
                        )

                        // Finger number inside dot
                        val finger = chord.fingers.getOrNull(sIdx) ?: 0
                        if (!lite && finger > 0) {
                            val fingerLayout = textMeasurer.measure(
                                "$finger",
                                style = TextStyle(color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            )
                            drawText(
                                textLayoutResult = fingerLayout,
                                topLeft = Offset(cx - fingerLayout.size.width.toFloat() / 2f, cy - fingerLayout.size.height.toFloat() / 2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-fidelity SVG-style Piano Chord Diagram.
 * Renders miniature two-octave (C4-B5) or dynamic keyboard keys with authentic ivory/ebony rendering,
 * highlighted chord tones, and inversion labels.
 */
@Composable
fun SvgPianoChordBox(
    midiNotes: List<Int>,
    chordName: String = "",
    inversionLabel: String = "Root Pos",
    accentColor: Color = Color(0xFFFF9E1B),
    cardBackground: Color = Color(0xFF1E222D),
    width: Dp = 145.dp,
    height: Dp = 160.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeMidiSet = remember(midiNotes) { midiNotes.toSet() }
    val minNote = remember(midiNotes) { midiNotes.minOrNull() ?: 60 }

    // Align start note to the closest C below minNote, span 14 white keys (~2 octaves)
    val startC = remember(minNote) { ((minNote / 12) * 12).coerceAtLeast(36) }
    val totalWhiteKeys = 14
    val whiteNotes = remember(startC) {
        val list = mutableListOf<Int>()
        var n = startC
        while (list.size < totalWhiteKeys) {
            if ((n % 12) !in listOf(1, 3, 6, 8, 10)) {
                list.add(n)
            }
            n++
        }
        list
    }

    Surface(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = cardBackground,
        border = BorderStroke(1.dp, Color(0xFF2C3242))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (chordName.isNotBlank()) {
                Text(
                    text = chordName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
            if (inversionLabel.isNotBlank()) {
                Text(
                    text = inversionLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9EABB8),
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(4.dp))

            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val canvasW = size.width
                val canvasH = size.height

                val leftPad = 4f
                val rightPad = 4f
                val topPad = 8f
                val bottomPad = 12f

                val usableW = canvasW - leftPad - rightPad
                val usableH = canvasH - topPad - bottomPad

                val whiteKeyW = usableW / totalWhiteKeys.toFloat()
                val whiteKeyH = usableH
                val blackKeyW = whiteKeyW * 0.65f
                val blackKeyH = whiteKeyH * 0.60f

                // 1. Draw White Keys
                whiteNotes.forEachIndexed { idx, whiteMidi ->
                    val x = leftPad + idx * whiteKeyW
                    val isActive = whiteMidi in activeMidiSet

                    // White key background
                    drawRoundRect(
                        color = if (isActive) accentColor.copy(alpha = 0.25f) else Color(0xFFECEFF4),
                        topLeft = Offset(x + 1f, topPad),
                        size = Size(whiteKeyW - 2f, whiteKeyH),
                        cornerRadius = CornerRadius(3.dp.toPx())
                    )

                    // Border
                    drawRoundRect(
                        color = if (isActive) accentColor else Color(0xFF2B303C),
                        topLeft = Offset(x + 1f, topPad),
                        size = Size(whiteKeyW - 2f, whiteKeyH),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                        style = Stroke(width = if (isActive) 1.5.dp.toPx() else 1.dp.toPx())
                    )

                    // Active dot on White key
                    if (isActive) {
                        val dotY = topPad + whiteKeyH - 12.dp.toPx()
                        drawCircle(
                            color = accentColor,
                            radius = 4.5.dp.toPx(),
                            center = Offset(x + whiteKeyW / 2f, dotY)
                        )
                    }
                }

                // 2. Draw Black Keys
                whiteNotes.forEachIndexed { idx, whiteMidi ->
                    val semi = whiteMidi % 12
                    // Black keys appear after C, D, F, G, A
                    if (semi in listOf(0, 2, 5, 7, 9) && idx < totalWhiteKeys - 1) {
                        val blackMidi = whiteMidi + 1
                        val isActive = blackMidi in activeMidiSet
                        val x = leftPad + (idx + 1) * whiteKeyW - (blackKeyW / 2f)

                        drawRoundRect(
                            color = if (isActive) accentColor else Color(0xFF1E222B),
                            topLeft = Offset(x, topPad),
                            size = Size(blackKeyW, blackKeyH),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                        drawRoundRect(
                            color = if (isActive) Color.White else Color(0xFF0F1218),
                            topLeft = Offset(x, topPad),
                            size = Size(blackKeyW, blackKeyH),
                            cornerRadius = CornerRadius(2.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Active dot on Black key
                        if (isActive) {
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = Offset(x + blackKeyW / 2f, topPad + blackKeyH - 7.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}
