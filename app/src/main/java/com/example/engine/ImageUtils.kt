package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import com.example.data.BoundingBox
import com.example.data.DetectedCharacter
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Image processing utilities for Nüshu historical manuscript analysis.
 *
 * Strict Visual Styling Constraints:
 * - Line Thickness = 1 (very fine, thin line)
 * - Color = Vibrant Neon Fluorescent Green (BGR: 0, 255, 0 / Hex: #00FF00 / RGB: 0, 255, 0)
 */
object ImageUtils {

    // Neon Fluorescent Green: #00FF00 (ARGB: 0xFF00FF00, BGR: 0, 255, 0)
    const val NEON_GREEN_COLOR_INT = 0xFF00FF00.toInt()
    val NEON_GREEN_COMPOSE = androidx.compose.ui.graphics.Color(0xFF00FF00)

    /**
     * Creates an annotated bitmap with Neon Fluorescent Green bounding boxes (Thickness = 1).
     */
    fun drawBoundingBoxesOnBitmap(
        source: Bitmap,
        characters: List<DetectedCharacter>,
        strokeThicknessPx: Float = 1.0f,
        showIndices: Boolean = true
    ): Bitmap {
        val workingBitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)

        val boxPaint = Paint().apply {
            color = NEON_GREEN_COLOR_INT
            style = Paint.Style.STROKE
            strokeWidth = strokeThicknessPx
            isAntiAlias = true
        }

        val textBgPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = NEON_GREEN_COLOR_INT
            textSize = max(10f, min(workingBitmap.width, workingBitmap.height) * 0.016f)
            isFakeBoldText = true
            isAntiAlias = true
        }

        for (char in characters) {
            val rect = char.box.toRectF()
            // Draw thin Neon Fluorescent Green 1px stroke bounding box
            canvas.drawRect(rect, boxPaint)

            if (showIndices) {
                val tagText = "#${char.id} ${char.glyph.nameZh} ${(char.confidence * 100).toInt()}%"
                val textBounds = Rect()
                textPaint.getTextBounds(tagText, 0, tagText.length, textBounds)

                val tagLeft = rect.left
                val tagBottom = rect.top.coerceAtLeast(textBounds.height().toFloat() + 4f)
                val tagTop = tagBottom - textBounds.height() - 4f
                val tagRight = tagLeft + textBounds.width() + 8f

                canvas.drawRect(RectF(tagLeft, tagTop, tagRight, tagBottom), textBgPaint)
                canvas.drawText(tagText, tagLeft + 4f, tagBottom - 3f, textPaint)
            }
        }

        return workingBitmap
    }

    /**
     * Crops a character region safely from the source bitmap.
     */
    fun cropCharacter(source: Bitmap, box: BoundingBox, paddingPercent: Float = 0.08f): Bitmap? {
        val padX = box.width * paddingPercent
        val padY = box.height * paddingPercent

        val left = max(0, (box.left - padX).toInt())
        val top = max(0, (box.top - padY).toInt())
        val right = min(source.width, (box.right + padX).toInt())
        val bottom = min(source.height, (box.bottom + padY).toInt())

        val cropWidth = right - left
        val cropHeight = bottom - top

        if (cropWidth <= 0 || cropHeight <= 0) return null

        return try {
            Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Loads and resizes a bitmap efficiently from a resource or Uri.
     */
    fun loadBitmapFromResource(context: Context, resId: Int, maxDim: Int = 1600): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId, options)

            var inSampleSize = 1
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeResource(context.resources, resId, decodeOptions)
        } catch (e: Exception) {
            null
        }
    }

    fun loadBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1600): Bitmap? {
        return try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var inSampleSize = 1
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                    inSampleSize *= 2
                }
            }

            inputStream = context.contentResolver.openInputStream(uri)
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
