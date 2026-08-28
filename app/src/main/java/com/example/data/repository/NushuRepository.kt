package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.R
import com.example.data.DetectionResult
import com.example.data.ManuscriptPreset
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ScanRecordEntity
import com.example.engine.ImageUtils
import com.example.engine.NmsEngine
import com.example.engine.NushuDetector
import com.example.engine.OnnxModelInfo
import kotlinx.coroutines.flow.Flow

class NushuRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val scanDao = db.scanDao()
    val detector = NushuDetector(context)

    val scanHistory: Flow<List<ScanRecordEntity>> = scanDao.getAllScans()

    /**
     * Curated historical bamboo strip manuscript presets for instant offline testing.
     */
    val samplePresets = listOf(
        ManuscriptPreset(
            id = "preset_1",
            title = "Jiangyong Bamboo Slat Ballad",
            subtitle = "Authentic Heptasyllabic Nüshu Inscription",
            era = "Qing Dynasty Artifact",
            drawableResId = R.drawable.bamboo_manuscript_sample1_1787843127313,
            description = "Preserved vertical bamboo slat manuscript recording a traditional sisterhood lament with diamond-formed vertical glyphs."
        ),
        ManuscriptPreset(
            id = "preset_2",
            title = "Sworn Sisterhood Sandaoshu Slip",
            subtitle = "Vertical Slat Epistolary Verse",
            era = "Late 19th Century Hunan",
            drawableResId = R.drawable.bamboo_manuscript_sample2_1787843138715,
            description = "Three-day letter manuscript carved and inked on aged bamboo strips, detailing lifelong vows of friendship and mutual solace."
        )
    )

    fun getOnnxModelInfo(): OnnxModelInfo = detector.onnxEngine.modelInfo

    fun reloadOnnxModel(name: String = "best.onnx"): Boolean {
        return detector.onnxEngine.loadModelFromAssetsOrStorage(name)
    }

    fun loadOnnxModelFromUri(uri: Uri): Boolean {
        return detector.onnxEngine.loadModelFromUri(uri)
    }

    suspend fun runDetection(
        bitmap: Bitmap,
        confidenceThreshold: Float = NmsEngine.DEFAULT_CONFIDENCE_THRESHOLD,
        overlapThreshold: Float = NmsEngine.OVERLAP_SUPPRESSION_THRESHOLD,
        strokeThickness: Float = 1.0f
    ): DetectionResult {
        return detector.detectManuscript(
            sourceBitmap = bitmap,
            confidenceThreshold = confidenceThreshold,
            overlapThreshold = overlapThreshold,
            strokeThicknessPx = strokeThickness
        )
    }

    fun loadBitmapFromResource(resId: Int): Bitmap? {
        return ImageUtils.loadBitmapFromResource(context, resId)
    }

    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return ImageUtils.loadBitmapFromUri(context, uri)
    }

    suspend fun saveScanRecord(
        title: String,
        result: DetectionResult,
        samplePresetId: String? = null
    ): Long {
        val avgConf = if (result.filteredCharacters.isNotEmpty()) {
            result.filteredCharacters.map { it.confidence }.average().toFloat()
        } else {
            0f
        }

        val summary = result.filteredCharacters.take(8).joinToString(separator = " • ") {
            "${it.glyph.unicodeChar} ${it.glyph.nameZh} (${(it.confidence * 100).toInt()}%)"
        }

        val entity = ScanRecordEntity(
            title = title,
            totalCharacters = result.filteredCharacters.size,
            avgConfidence = avgConf,
            rawDetections = result.rawDetectionsCount,
            bambooColumns = result.bambooColumnsCount,
            confidenceThreshold = result.confidenceThreshold,
            overlapThreshold = result.overlapThreshold,
            transcriptionSummary = summary,
            samplePresetId = samplePresetId
        )
        return scanDao.insertScan(entity)
    }

    suspend fun deleteScanRecord(record: ScanRecordEntity) {
        scanDao.deleteScan(record)
    }

    suspend fun clearHistory() {
        scanDao.clearAll()
    }
}
