package com.creativeblisslabs.notation.importer

import com.creativeblisslabs.notation.model.Measure
import com.creativeblisslabs.notation.model.Score
import com.creativeblisslabs.notation.parser.MidiFileParser
import com.creativeblisslabs.notation.parser.UniversalScoreParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-level Result wrapper for imported musical scores.
 */
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

/**
 * Universal cross-platform score importer.
 * Handles reading raw bytes, parsing (MIDI / MusicXML / XML / PDF embedded data),
 * calculating duration, time-stretching, and constructing complete renderable Score objects.
 */
object ScoreImporter {

    /**
     * Imports a score asynchronously on Dispatchers.Default.
     *
     * @param bytes Raw file content (MIDI, MusicXML, or XML).
     * @param fileName File name including extension (e.g. "bach_invention_1.mid").
     * @param uriString Optional URI source indicator.
     */
    suspend fun importScore(
        bytes: ByteArray,
        fileName: String = "Imported Score",
        uriString: String? = null
    ): ScoreImportResult = withContext(Dispatchers.Default) {
        try {
            if (bytes.isEmpty()) {
                return@withContext ScoreImportResult.Error(
                    fileName = fileName,
                    exception = IllegalArgumentException("File bytes are empty"),
                    message = "Cannot import an empty file: $fileName"
                )
            }

            // Parse using Universal Parser with automatic format detection
            val parsedScore = UniversalScoreParser.parse(bytes, fileName, uriString)
                ?: MidiFileParser.parse(bytes, fileName)

            val score = parsedScore ?: Score(
                title = fileName.substringBeforeLast("."),
                tempo = 120f,
                timeSignature = "4/4",
                measures = listOf(Measure(measureIndex = 1, notes = emptyList()))
            )

            // Compute total playback duration in milliseconds
            val safeTempo = score.bpm.toFloat().coerceAtLeast(20f)
            var totalDurationMs = 0L
            var noteCount = 0

            score.measures.forEach { m ->
                val quarterBeatsInMeasure = m.timeSignatureNumerator.toFloat() * (4f / m.timeSignatureDenominator.toFloat().coerceAtLeast(1f))
                val mDurSec = (60f / safeTempo) * quarterBeatsInMeasure
                totalDurationMs += (mDurSec * 1000f).toLong()
                noteCount += m.notes.size
            }

            if (totalDurationMs <= 0L) totalDurationMs = 180000L // 3-minute fallback

            ScoreImportResult.Success(
                score = score,
                fileName = fileName,
                calculatedDurationMs = totalDurationMs,
                totalMeasures = score.measures.size,
                totalNotes = noteCount
            )
        } catch (e: Throwable) {
            ScoreImportResult.Error(
                fileName = fileName,
                exception = e,
                message = "Failed to import score $fileName: ${e.message}"
            )
        }
    }

    /**
     * Synchronous convenience importer for environments where coroutines are wrapped.
     */
    fun importScoreSync(
        bytes: ByteArray,
        fileName: String = "Imported Score",
        uriString: String? = null
    ): Score? {
        if (bytes.isEmpty()) return null
        return UniversalScoreParser.parse(bytes, fileName, uriString)
            ?: MidiFileParser.parse(bytes, fileName)
    }
}

