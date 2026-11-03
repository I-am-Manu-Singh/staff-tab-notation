# QUICK START GUIDE


```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.creativeblisslabs.notation.importer.ScoreImporter
import com.creativeblisslabs.notation.importer.ScoreImportResult
import com.creativeblisslabs.notation.model.Score
import com.creativeblisslabs.notation.ui.StaffAndTabNotationViewer
@Composable
fun ScoreApp(fileBytes: ByteArray, fileName: String) {
    var score by remember { mutableStateOf<Score?>(null) }
    var currentFormat by remember { mutableStateOf("Both") } // "Both", "Staff", or "Tab"
    // 1. One-line Asynchronous Import
    LaunchedEffect(fileBytes, fileName) {
        when (val result = ScoreImporter.importScore(fileBytes, fileName)) {
            is ScoreImportResult.Success -> score = result.score
            is ScoreImportResult.Error -> println("Import error: ${result.message}")
        }
    }
    // 2. Render Score Viewer
    StaffAndTabNotationViewer(
        score = score,
        playheadMeasure = 0,
        playheadOffset = 0f,
        activeMidiNotes = mapOf(60 to true),
        playingNote = 60,
        primaryColor = Color(0xFF6C63FF),
        currentFormat = currentFormat,
        onFormatChange = { currentFormat = it },
        showLetters = true,
        showLyrics = true,
        isSplitStaff = true,
        modifier = Modifier.fillMaxSize()
    )
}
```