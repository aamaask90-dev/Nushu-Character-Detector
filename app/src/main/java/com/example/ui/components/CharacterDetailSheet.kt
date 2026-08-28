package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DetectedCharacter
import com.example.ui.theme.BambooAmber
import com.example.ui.theme.BambooBorder
import com.example.ui.theme.BambooCard
import com.example.ui.theme.BambooCardElevated
import com.example.ui.theme.BambooDarkSurface
import com.example.ui.theme.BambooGold
import com.example.ui.theme.BambooTextPrimary
import com.example.ui.theme.BambooTextSecondary
import com.example.ui.theme.BambooTextTertiary
import com.example.ui.theme.NeonGreen

/**
 * Inspection sheet displaying detailed historical, phonetic, and stroke analysis of a detected Nüshu character.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailSheet(
    character: DetectedCharacter?,
    onDismiss: () -> Unit
) {
    if (character == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BambooDarkSurface,
        scrimColor = Color(0x99000000),
        modifier = Modifier.testTag("character_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0x3300FF00),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "GLYPH #${character.id}",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bamboo Column ${character.columnIndex}, Row ${character.rowIndex}",
                        color = BambooTextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BambooTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Crop & Core Identification Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cropped original bitmap preview with neon border
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF000000))
                        .border(1.5.dp, NeonGreen, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (character.croppedBitmap != null) {
                        Image(
                            bitmap = character.croppedBitmap.asImageBitmap(),
                            contentDescription = "Cropped Glyph #${character.id}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        )
                    } else {
                        Text(
                            text = character.glyph.unicodeChar,
                            fontSize = 38.sp,
                            color = NeonGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Chinese / English / Pinyin identification
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.glyph.nameZh,
                            color = BambooTextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "[${character.glyph.pinyin}]",
                            color = BambooGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = character.glyph.englishMeaning,
                        color = NeonGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Category: ${character.glyph.category}",
                        color = BambooTextTertiary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics row: Confidence score & Stroke count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BambooCard, RoundedCornerShape(12.dp))
                    .border(1.dp, BambooBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn(
                    label = "CONFIDENCE",
                    value = "${(character.confidence * 100).toInt()}%",
                    color = NeonGreen
                )
                MetricColumn(
                    label = "STROKES",
                    value = "${character.glyph.strokeCount}",
                    color = BambooGold
                )
                MetricColumn(
                    label = "INK DENSITY",
                    value = "%.2f".format(character.strokeDensity),
                    color = BambooAmber
                )
                MetricColumn(
                    label = "UNICODE",
                    value = character.glyph.unicodeChar,
                    color = BambooTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Historical context & linguistic commentary
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BambooBorder, RoundedCornerShape(12.dp)),
                color = BambooCardElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = BambooGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Historical & Cultural Context",
                            color = BambooGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = character.glyph.historicalContext,
                        color = BambooTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = BambooTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}
