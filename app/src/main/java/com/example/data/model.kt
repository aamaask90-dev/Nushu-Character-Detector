package com.example.data

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * Normalized or pixel-space bounding box for a detected character.
 * [left], [top], [right], [bottom] in pixels or normalized 0..1 range.
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val area: Float get() = width * height
    val centerX: Float get() = left + width / 2f
    val centerY: Float get() = top + height / 2f

    fun toRectF(): RectF = RectF(left, top, right, bottom)

    /**
     * Computes intersection area with another bounding box.
     */
    fun intersectionArea(other: BoundingBox): Float {
        val interLeft = maxOf(left, other.left)
        val interTop = maxOf(top, other.top)
        val interRight = minOf(right, other.right)
        val interBottom = minOf(bottom, other.bottom)

        val interWidth = (interRight - interLeft).coerceAtLeast(0f)
        val interHeight = (interBottom - interTop).coerceAtLeast(0f)
        return interWidth * interHeight
    }

    /**
     * Computes overlap ratio defined as intersection_area / this.area.
     */
    fun overlapRatioWith(other: BoundingBox): Float {
        val inter = intersectionArea(other)
        if (area <= 0f) return 0f
        return inter / area
    }

    /**
     * Standard Intersection over Union (IoU).
     */
    fun iou(other: BoundingBox): Float {
        val inter = intersectionArea(other)
        val union = area + other.area - inter
        if (union <= 0f) return 0f
        return inter / union
    }
}

/**
 * Historical Nüshu glyph dictionary item.
 */
data class NushuGlyph(
    val id: String,
    val unicodeChar: String,
    val nameZh: String,
    val pinyin: String,
    val englishMeaning: String,
    val historicalContext: String,
    val strokeCount: Int,
    val category: String
)

/**
 * Single detected character on the bamboo manuscript.
 */
data class DetectedCharacter(
    val id: Int,
    val box: BoundingBox,
    val confidence: Float,
    val glyph: NushuGlyph,
    val columnIndex: Int,
    val rowIndex: Int,
    val strokeDensity: Float,
    val croppedBitmap: Bitmap? = null
)

/**
 * Complete detection result for an image.
 */
data class DetectionResult(
    val originalBitmap: Bitmap,
    val processedBitmap: Bitmap,
    val rawDetectionsCount: Int,
    val filteredCharacters: List<DetectedCharacter>,
    val confidenceThreshold: Float,
    val overlapThreshold: Float,
    val inferenceTimeMs: Long,
    val bambooColumnsCount: Int
)

/**
 * Preset sample manuscript.
 */
data class ManuscriptPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val era: String,
    val drawableResId: Int,
    val description: String
)
