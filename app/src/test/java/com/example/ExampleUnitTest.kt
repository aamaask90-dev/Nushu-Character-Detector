package com.example

import com.example.data.BoundingBox
import com.example.data.DetectedCharacter
import com.example.engine.NmsEngine
import com.example.engine.NushuDictionary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testNmsAntiOverlapSuppression_threshold25Percent() {
        val glyph = NushuDictionary.GLYPHS[0]

        // Box 1: high confidence (0.85) at (100, 100) -> (200, 200), area = 10000
        val box1 = DetectedCharacter(
            id = 1,
            box = BoundingBox(100f, 100f, 200f, 200f),
            confidence = 0.85f,
            glyph = glyph,
            columnIndex = 1,
            rowIndex = 1,
            strokeDensity = 0.6f
        )

        // Box 2: lower confidence (0.60) overlapping heavily (110, 110) -> (210, 210)
        // Inter: (110, 110) to (200, 200) = 90x90 = 8100. Ratio = 8100 / 10000 = 81% (> 25%)
        val box2 = DetectedCharacter(
            id = 2,
            box = BoundingBox(110f, 110f, 210f, 210f),
            confidence = 0.60f,
            glyph = glyph,
            columnIndex = 1,
            rowIndex = 2,
            strokeDensity = 0.5f
        )

        // Box 3: non-overlapping character further down the bamboo slat (100, 250) -> (200, 350)
        val box3 = DetectedCharacter(
            id = 3,
            box = BoundingBox(100f, 250f, 200f, 350f),
            confidence = 0.75f,
            glyph = glyph,
            columnIndex = 1,
            rowIndex = 3,
            strokeDensity = 0.55f
        )

        val candidates = listOf(box1, box2, box3)
        val result = NmsEngine.suppressOverlaps(
            candidates = candidates,
            confidenceThreshold = 0.30f,
            overlapThreshold = 0.25f
        )

        // Box 2 must be suppressed because overlap ratio exceeds 0.25 (25%)
        assertEquals(2, result.size)
        // Highest confidence box1 should be preserved
        assertTrue(result.any { it.confidence == 0.85f })
        // Non-overlapping box3 should be preserved
        assertTrue(result.any { it.confidence == 0.75f })
        // Redundant box2 should be suppressed
        assertTrue(result.none { it.confidence == 0.60f })
    }

    @Test
    fun testConfidenceThreshold_filtersFaintNoiseBelowThreshold() {
        val glyph = NushuDictionary.GLYPHS[0]

        val validBox = DetectedCharacter(
            id = 1,
            box = BoundingBox(10f, 10f, 50f, 50f),
            confidence = 0.35f, // >= 0.30
            glyph = glyph,
            columnIndex = 1,
            rowIndex = 1,
            strokeDensity = 0.4f
        )

        val noiseBox = DetectedCharacter(
            id = 2,
            box = BoundingBox(60f, 60f, 100f, 100f),
            confidence = 0.22f, // < 0.30
            glyph = glyph,
            columnIndex = 1,
            rowIndex = 2,
            strokeDensity = 0.2f
        )

        val result = NmsEngine.suppressOverlaps(
            candidates = listOf(validBox, noiseBox),
            confidenceThreshold = 0.30f,
            overlapThreshold = 0.25f
        )

        assertEquals(1, result.size)
        assertEquals(0.35f, result[0].confidence, 0.001f)
    }
}
