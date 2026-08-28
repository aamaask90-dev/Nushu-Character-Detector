package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.theme.BambooBorder
import com.example.ui.theme.BambooCard
import com.example.ui.theme.BambooCardElevated
import com.example.ui.theme.BambooGold
import com.example.ui.theme.BambooTextPrimary
import com.example.ui.theme.BambooTextSecondary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenDim

/**
 * Horizontal scrollable gallery of detected character thumbnails with confidence badges.
 */
@Composable
fun CharacterGridSection(
    characters: List<DetectedCharacter>,
    selectedCharacter: DetectedCharacter?,
    onCharacterClick: (DetectedCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    if (characters.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("character_grid_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Identified Glyphs (${characters.size})",
                    color = BambooTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Tap glyph to inspect",
                color = BambooTextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(characters, key = { it.id }) { char ->
                CharacterThumbCard(
                    character = char,
                    isSelected = selectedCharacter?.id == char.id,
                    onClick = { onCharacterClick(char) }
                )
            }
        }
    }
}

@Composable
private fun CharacterThumbCard(
    character: DetectedCharacter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) NeonGreen else BambooBorder
    val bgColor = if (isSelected) NeonGreenDim else BambooCard

    Surface(
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("character_thumb_${character.id}"),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Index & Confidence Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#${character.id}",
                    color = NeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(character.confidence * 100).toInt()}%",
                    color = BambooGold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Glyph Crop Image
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF000000))
                    .border(0.5.dp, Color(0xFF222B21), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (character.croppedBitmap != null) {
                    Image(
                        bitmap = character.croppedBitmap.asImageBitmap(),
                        contentDescription = "Glyph #${character.id}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp)
                    )
                } else {
                    Text(
                        text = character.glyph.unicodeChar,
                        fontSize = 24.sp,
                        color = NeonGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Name
            Text(
                text = character.glyph.nameZh,
                color = BambooTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = character.glyph.pinyin,
                color = BambooTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
