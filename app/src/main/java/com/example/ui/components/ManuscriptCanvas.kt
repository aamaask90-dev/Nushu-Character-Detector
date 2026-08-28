package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DetectedCharacter
import com.example.ui.theme.BambooCard
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenDim

/**
 * Interactive canvas for viewing bamboo manuscripts with real-time Neon Green bounding box overlays.
 *
 * Strict Visual Constraints:
 * - Line Thickness = 1 (very fine, thin line)
 * - Color = Vibrant Neon Fluorescent Green (BGR: 0, 255, 0 / Hex: #00FF00)
 */
@Composable
fun ManuscriptCanvas(
    bitmap: Bitmap?,
    characters: List<DetectedCharacter>,
    selectedCharacter: DetectedCharacter?,
    showBoxes: Boolean,
    showIndices: Boolean,
    showColumnGuides: Boolean,
    onCharacterSelected: (DetectedCharacter?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bitmap == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(16.dp))
                .background(BambooCard)
                .border(1.dp, Color(0xFF2E382D), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a manuscript to begin detection",
                color = Color(0xFFA5B2A3),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        val maxOffset = 500f * (scale - 1f)
        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxOffset, maxOffset),
            y = (offset.y + offsetChange.y).coerceIn(-maxOffset, maxOffset)
        )
    }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val textMeasurer = rememberTextMeasurer()

    val bitmapWidth = bitmap.width.toFloat()
    val bitmapHeight = bitmap.height.toFloat()
    val aspectRatio = if (bitmapHeight > 0) bitmapWidth / bitmapHeight else 0.75f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio.coerceIn(0.5f, 1.8f))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A0D0A))
            .border(1.dp, Color(0xFF2E382D), RoundedCornerShape(16.dp))
            .clipToBounds()
            .testTag("manuscript_canvas")
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = transformState)
                .pointerInput(characters, containerWidth, containerHeight) {
                    detectTapGestures { tapOffset ->
                        val scaleX = containerWidth / bitmapWidth
                        val scaleY = containerHeight / bitmapHeight

                        // Convert tap position to bitmap coordinates
                        val bmpTapX = tapOffset.x / scaleX
                        val bmpTapY = tapOffset.y / scaleY

                        // Find tapped character box
                        val hit = characters.firstOrNull { char ->
                            val box = char.box
                            bmpTapX >= box.left && bmpTapX <= box.right &&
                                    bmpTapY >= box.top && bmpTapY <= box.bottom
                        }
                        onCharacterSelected(hit)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw original bamboo manuscript image scaled to fill canvas
                drawImage(
                    image = imageBitmap,
                    dstSize = androidx.compose.ui.unit.IntSize(
                        size.width.toInt(),
                        size.height.toInt()
                    )
                )

                val scaleX = size.width / bitmapWidth
                val scaleY = size.height / bitmapHeight

                // 2. Draw subtle column guides if enabled
                if (showColumnGuides) {
                    val cols = 4
                    val colWidth = size.width / cols
                    for (i in 1 until cols) {
                        drawLine(
                            color = Color(0x44DFB15B),
                            start = Offset(i * colWidth, 0f),
                            end = Offset(i * colWidth, size.height),
                            strokeWidth = 1f
                        )
                    }
                }

                // 3. Draw Neon Green Bounding Boxes (Thickness = 1px)
                if (showBoxes) {
                    for (char in characters) {
                        val isSelected = selectedCharacter?.id == char.id
                        val left = char.box.left * scaleX
                        val top = char.box.top * scaleY
                        val right = char.box.right * scaleX
                        val bottom = char.box.bottom * scaleY
                        val boxWidth = (right - left).coerceAtLeast(1f)
                        val boxHeight = (bottom - top).coerceAtLeast(1f)

                        // Strict Requirement: Neon Fluorescent Green (0xFF00FF00) and Thickness = 1
                        val boxStroke = Stroke(width = if (isSelected) 2.5f else 1.0f)
                        val boxColor = if (isSelected) NeonGreen else NeonGreen

                        if (isSelected) {
                            // Highlight fill for selected character
                            drawRect(
                                color = NeonGreenDim,
                                topLeft = Offset(left, top),
                                size = Size(boxWidth, boxHeight)
                            )
                        }

                        drawRect(
                            color = boxColor,
                            topLeft = Offset(left, top),
                            size = Size(boxWidth, boxHeight),
                            style = boxStroke
                        )

                        // Draw character index badge
                        if (showIndices) {
                            val tagText = "#${char.id} ${char.glyph.nameZh}"
                            val textLayout = textMeasurer.measure(
                                text = tagText,
                                style = TextStyle(
                                    color = NeonGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            val badgeTop = (top - textLayout.size.height - 2f).coerceAtLeast(0f)
                            val badgeWidth = textLayout.size.width + 6f
                            val badgeHeight = textLayout.size.height + 4f

                            drawRect(
                                color = Color(0xCC000000),
                                topLeft = Offset(left, badgeTop),
                                size = Size(badgeWidth, badgeHeight)
                            )

                            drawText(
                                textMeasurer = textMeasurer,
                                text = tagText,
                                topLeft = Offset(left + 3f, badgeTop + 2f),
                                style = TextStyle(
                                    color = NeonGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
