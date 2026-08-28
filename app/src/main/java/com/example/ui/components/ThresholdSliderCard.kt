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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BambooBorder
import com.example.ui.theme.BambooCard
import com.example.ui.theme.BambooCardElevated
import com.example.ui.theme.BambooGold
import com.example.ui.theme.BambooTextPrimary
import com.example.ui.theme.BambooTextSecondary
import com.example.ui.theme.NeonGreen

/**
 * Control panel for dynamic confidence threshold (conf_thresh = 0.3),
 * anti-overlap NMS settings, and visual overlay filters.
 */
@Composable
fun ThresholdSliderCard(
    confidenceThreshold: Float,
    overlapThreshold: Float,
    strokeThickness: Float,
    showBoxes: Boolean,
    showIndices: Boolean,
    showColumns: Boolean,
    onConfidenceChanged: (Float) -> Unit,
    onToggleBoxes: (Boolean) -> Unit,
    onToggleIndices: (Boolean) -> Unit,
    onToggleColumns: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BambooBorder, RoundedCornerShape(16.dp))
            .testTag("threshold_control_card"),
        color = BambooCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detection Parameters & NMS",
                        color = BambooTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Thickness = ${strokeThickness.toInt()}px",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Confidence Threshold Slider (default 0.3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Confidence Threshold (conf_thresh)",
                        color = BambooTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Captures faint, weathered historical brush strokes",
                        color = BambooTextSecondary,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "%.2f".format(confidenceThreshold),
                    color = NeonGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Slider(
                value = confidenceThreshold,
                onValueChange = onConfidenceChanged,
                valueRange = 0.15f..0.85f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = NeonGreen,
                    activeTrackColor = NeonGreen,
                    inactiveTrackColor = BambooCardElevated
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confidence_slider")
            )

            // Overlap suppression stat & description (strictly 25%)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BambooCardElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Anti-Overlap Threshold (IoU / Area Ratio)",
                    color = BambooTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "25% (0.25)",
                    color = BambooGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick toggle chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showBoxes,
                    onClick = { onToggleBoxes(!showBoxes) },
                    label = { Text("Neon Boxes", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x3300FF00),
                        selectedLabelColor = NeonGreen,
                        containerColor = BambooCardElevated,
                        labelColor = BambooTextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = showIndices,
                    onClick = { onToggleIndices(!showIndices) },
                    label = { Text("Labels", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x3300FF00),
                        selectedLabelColor = NeonGreen,
                        containerColor = BambooCardElevated,
                        labelColor = BambooTextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = showColumns,
                    onClick = { onToggleColumns(!showColumns) },
                    label = { Text("Slats Guide", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x33DFB15B),
                        selectedLabelColor = BambooGold,
                        containerColor = BambooCardElevated,
                        labelColor = BambooTextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
