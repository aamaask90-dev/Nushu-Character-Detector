package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalCharacters: Int,
    val avgConfidence: Float,
    val rawDetections: Int,
    val bambooColumns: Int,
    val confidenceThreshold: Float,
    val overlapThreshold: Float,
    val transcriptionSummary: String,
    val samplePresetId: String? = null
)
