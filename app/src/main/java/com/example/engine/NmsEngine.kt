package com.example.engine

import com.example.data.BoundingBox
import com.example.data.DetectedCharacter

/**
 * Custom Anti-Overlap & Non-Maximum Suppression (NMS) Engine.
 *
 * Implements strict programmatic suppression:
 * 1. Filters raw candidates by dynamic confidence threshold (default 0.3).
 * 2. Sorts candidates by confidence score in descending order.
 * 3. Compares each candidate against higher-confidence accepted boxes:
 *    Calculates overlap ratio (`inter_area / box_area`).
 *    If the overlap ratio exceeds 0.25 (25%), the lower-confidence box is suppressed.
 * 4. Also checks dual-directional overlap (`inter_area / other.area` and IoU)
 *    to completely eliminate stacked bounding boxes and visual clutter on bamboo slats.
 */
object NmsEngine {

    const val DEFAULT_CONFIDENCE_THRESHOLD = 0.30f
    const val OVERLAP_SUPPRESSION_THRESHOLD = 0.25f // 25% overlap ratio limit

    /**
     * Executes non-maximum suppression on detected character candidates.
     *
     * @param candidates Raw list of detected candidate bounding boxes with confidence scores.
     * @param confidenceThreshold Dynamic threshold (default 0.3f) to capture faint historical glyphs.
     * @param overlapThreshold Maximum allowed overlap ratio before suppression (default 0.25f = 25%).
     * @return Filtered list of non-overlapping, high-confidence character detections.
     */
    fun suppressOverlaps(
        candidates: List<DetectedCharacter>,
        confidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD,
        overlapThreshold: Float = OVERLAP_SUPPRESSION_THRESHOLD
    ): List<DetectedCharacter> {
        // Step 1: Filter out boxes below confidence threshold
        val confidentCandidates = candidates.filter { it.confidence >= confidenceThreshold }

        // Step 2: Sort detected boxes by confidence score descending
        val sortedCandidates = confidentCandidates.sortedByDescending { it.confidence }

        val accepted = mutableListOf<DetectedCharacter>()

        // Step 3: Iterate through sorted boxes and suppress redundant overlapping ones
        for (candidate in sortedCandidates) {
            var isRedundant = false

            for (acceptedBox in accepted) {
                val interArea = candidate.box.intersectionArea(acceptedBox.box)

                if (interArea > 0f) {
                    val candidateArea = candidate.box.area
                    val acceptedArea = acceptedBox.box.area

                    // Overlap ratio = inter_area / box_area (for the candidate)
                    val candidateOverlapRatio = if (candidateArea > 0f) interArea / candidateArea else 0f
                    // Overlap ratio relative to the accepted higher-confidence box
                    val acceptedOverlapRatio = if (acceptedArea > 0f) interArea / acceptedArea else 0f

                    // Standard IoU
                    val unionArea = candidateArea + acceptedArea - interArea
                    val iou = if (unionArea > 0f) interArea / unionArea else 0f

                    // If overlap ratio exceeds 0.25 (25%), suppress redundant lower-confidence box
                    if (candidateOverlapRatio > overlapThreshold ||
                        acceptedOverlapRatio > overlapThreshold ||
                        iou > overlapThreshold
                    ) {
                        isRedundant = true
                        break
                    }
                }
            }

            if (!isRedundant) {
                accepted.add(candidate)
            }
        }

        // Re-index remaining clean boxes from top-to-bottom and left-to-right (bamboo column reading order)
        return accepted.sortedWith(
            compareBy<DetectedCharacter> { it.columnIndex }
                .thenBy { it.box.top }
        ).mapIndexed { index, char ->
            char.copy(id = index + 1)
        }
    }

    /**
     * Pure bounding box suppression for standalone geometric box lists.
     */
    fun suppressBoxOverlaps(
        boxesWithScores: List<Pair<BoundingBox, Float>>,
        confidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD,
        overlapThreshold: Float = OVERLAP_SUPPRESSION_THRESHOLD
    ): List<Pair<BoundingBox, Float>> {
        val filtered = boxesWithScores.filter { it.second >= confidenceThreshold }
        val sorted = filtered.sortedByDescending { it.second }

        val accepted = mutableListOf<Pair<BoundingBox, Float>>()

        for (candidate in sorted) {
            var suppress = false
            for (acc in accepted) {
                val inter = candidate.first.intersectionArea(acc.first)
                if (inter > 0f) {
                    val ratio = inter / candidate.first.area
                    val accRatio = inter / acc.first.area
                    if (ratio > overlapThreshold || accRatio > overlapThreshold) {
                        suppress = true
                        break
                    }
                }
            }
            if (!suppress) {
                accepted.add(candidate)
            }
        }
        return accepted
    }
}
