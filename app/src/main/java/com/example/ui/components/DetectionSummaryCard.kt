package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DetectionResult
import com.example.engine.OnnxModelInfo
import com.example.ui.theme.BambooAmber
import com.example.ui.theme.BambooBorder
import com.example.ui.theme.BambooCard
import com.example.ui.theme.BambooCardElevated
import com.example.ui.theme.BambooGold
import com.example.ui.theme.BambooTextPrimary
import com.example.ui.theme.BambooTextSecondary
import com.example.ui.theme.NeonGreen

/**
 * Clean summary card displaying total character count, inference performance,
 * ONNX Runtime engine status, and Anti-Overlap NMS suppression statistics.
 */
@Composable
fun DetectionSummaryCard(
    result: DetectionResult?,
    modelInfo: OnnxModelInfo = OnnxModelInfo(),
    modifier: Modifier = Modifier
) {
    if (result == null) return

    val totalCount = result.filteredCharacters.size
    val suppressedCount = (result.rawDetectionsCount - totalCount).coerceAtLeast(0)
    val avgConfidence = if (totalCount > 0) {
        result.filteredCharacters.map { it.confidence }.average().toFloat()
    } else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BambooBorder, RoundedCornerShape(16.dp))
            .testTag("detection_summary_card"),
        color = BambooCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with ONNX Runtime & Inference Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BoxBadge(
                        text = if (modelInfo.isLoaded) "ORT: ${modelInfo.modelName}" else "ONNX RUNTIME",
                        color = if (modelInfo.isLoaded) NeonGreen else BambooGold,
                        textColor = Color(0xFF00390A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BoxBadge(
                        text = "INPUT: [1, 3, ${modelInfo.inputHeight}, ${modelInfo.inputWidth}]",
                        color = BambooCardElevated,
                        textColor = BambooGold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Inference time",
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${result.inferenceTimeMs} ms",
                        color = BambooTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Hero Metric: Total Detected Characters
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "DETECTED CHARACTERS",
                        color = BambooTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$totalCount",
                            color = NeonGreen,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = " glyphs",
                            color = BambooTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }
                }

                // Average confidence badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AVG CONFIDENCE",
                        color = BambooTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${(avgConfidence * 100).toInt()}%",
                        color = BambooGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-metrics row: Anti-Overlap NMS & Bamboo Columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BambooCardElevated, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatPill(
                    icon = Icons.Default.FilterAlt,
                    label = "25% NMS Suppressed",
                    value = "-$suppressedCount clutter boxes",
                    tint = NeonGreen
                )

                StatPill(
                    icon = Icons.Default.CheckCircle,
                    label = "Bamboo Slats",
                    value = "${result.bambooColumnsCount} columns",
                    tint = BambooAmber
                )
            }
        }
    }
}

@Composable
private fun BoxBadge(
    text: String,
    color: Color,
    textColor: Color
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                color = BambooTextSecondary,
                fontSize = 10.sp
            )
            Text(
                text = value,
                color = BambooTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
