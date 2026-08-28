package com.example.engine

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import com.example.data.BoundingBox
import com.example.data.DetectedCharacter
import com.example.data.NushuGlyph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min

/**
 * Metadata and status of the loaded ONNX model.
 */
data class OnnxModelInfo(
    val isLoaded: Boolean = false,
    val modelName: String = "best.onnx",
    val inputName: String = "images",
    val inputWidth: Int = 640,
    val inputHeight: Int = 640,
    val inputChannels: Int = 3,
    val outputInfo: String = "Pending initialization",
    val statusMessage: String = "Initializing ONNX Runtime..."
)

/**
 * Production-ready ONNX Runtime (ORT) inference engine for Nüshu historical character detection.
 *
 * Fully loads 'best.onnx' using Microsoft ONNX Runtime for Android, performs letterboxing
 * and Float32 tensor preprocessing, runs high-performance native inference on real device frames,
 * decodes output bounding boxes, and applies strict 25% overlap suppression.
 */
class OnnxNushuEngine(private val context: Context) {

    companion object {
        private const val TAG = "OnnxNushuEngine"
        const val DEFAULT_MODEL_NAME = "best.onnx"
        private const val DEFAULT_INPUT_SIZE = 640
    }

    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    private var inputTensorName: String = "images"
    private var inputWidth: Int = DEFAULT_INPUT_SIZE
    private var inputHeight: Int = DEFAULT_INPUT_SIZE
    private var inputChannels: Int = 3

    var modelInfo: OnnxModelInfo = OnnxModelInfo()
        private set

    init {
        initializeEngine()
    }

    /**
     * Initializes the ONNX Runtime environment and attempts to load best.onnx.
     */
    fun initializeEngine(): Boolean {
        return try {
            ortEnvironment = OrtEnvironment.getEnvironment()
            loadModelFromAssetsOrStorage(DEFAULT_MODEL_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ONNX Runtime: ${e.message}", e)
            modelInfo = modelInfo.copy(
                isLoaded = false,
                statusMessage = "ORT init error: ${e.localizedMessage}"
            )
            false
        }
    }

    /**
     * Loads the ONNX model from Android assets or internal storage.
     */
    fun loadModelFromAssetsOrStorage(modelName: String = DEFAULT_MODEL_NAME): Boolean {
        val env = ortEnvironment ?: try {
            OrtEnvironment.getEnvironment().also { ortEnvironment = it }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get OrtEnvironment", e)
            return false
        }

        try {
            // Close any existing session
            ortSession?.close()
            ortSession = null

            // 1. Try to locate model in assets
            var inputStream: InputStream? = null
            try {
                inputStream = context.assets.open(modelName)
                Log.i(TAG, "Found $modelName in app assets")
            } catch (e: Exception) {
                Log.d(TAG, "$modelName not found in assets, checking cache/files...")
            }

            // 2. Check local files / cache directory if not in assets
            if (inputStream == null) {
                val candidateFiles = listOf(
                    File(context.filesDir, modelName),
                    File(context.cacheDir, modelName),
                    File("/assets/$modelName"),
                    File("./assets/$modelName")
                )
                for (file in candidateFiles) {
                    if (file.exists() && file.length() > 0) {
                        inputStream = file.inputStream()
                        Log.i(TAG, "Found model file at: ${file.absolutePath}")
                        break
                    }
                }
            }

            if (inputStream == null) {
                Log.w(TAG, "Model '$modelName' not found yet in assets or storage. Engine is ready for uploaded weights.")
                modelInfo = OnnxModelInfo(
                    isLoaded = false,
                    modelName = modelName,
                    statusMessage = "Waiting for $modelName (Ready for asset or custom file upload)"
                )
                return false
            }

            // Read model bytes and create ORT session
            val modelBytes = inputStream.use { it.readBytes() }
            val sessionOptions = OrtSession.SessionOptions().apply {
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                setIntraOpNumThreads(Runtime.getRuntime().availableProcessors().coerceIn(2, 4))
            }

            val session = env.createSession(modelBytes, sessionOptions)
            ortSession = session

            // Inspect input tensor metadata
            val inputNames = session.inputNames
            if (inputNames.isNotEmpty()) {
                inputTensorName = inputNames.iterator().next()
                val inputInfo = session.inputInfo[inputTensorName]?.info as? TensorInfo
                val shape = inputInfo?.shape

                if (shape != null && shape.size >= 4) {
                    inputChannels = if (shape[1] > 0) shape[1].toInt() else 3
                    inputHeight = if (shape[2] > 0) shape[2].toInt() else DEFAULT_INPUT_SIZE
                    inputWidth = if (shape[3] > 0) shape[3].toInt() else DEFAULT_INPUT_SIZE
                }
            }

            val outputNames = session.outputNames.joinToString(", ")

            modelInfo = OnnxModelInfo(
                isLoaded = true,
                modelName = modelName,
                inputName = inputTensorName,
                inputWidth = inputWidth,
                inputHeight = inputHeight,
                inputChannels = inputChannels,
                outputInfo = "Outputs: [$outputNames]",
                statusMessage = "ONNX Model '$modelName' loaded & active"
            )
            Log.i(TAG, "Successfully loaded ONNX model: $modelInfo")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error loading ONNX model $modelName: ${e.message}", e)
            modelInfo = OnnxModelInfo(
                isLoaded = false,
                modelName = modelName,
                statusMessage = "Load failed: ${e.localizedMessage}"
            )
            return false
        }
    }

    /**
     * Loads a custom ONNX model from a content Uri (e.g. picked from device storage).
     */
    fun loadModelFromUri(uri: Uri): Boolean {
        return try {
            val destFile = File(context.filesDir, "custom_nushu.onnx")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            loadModelFromAssetsOrStorage("custom_nushu.onnx")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model from URI: ${e.message}", e)
            false
        }
    }

    /**
     * High-performance image preprocessing:
     * 1. Letterboxes the source bitmap to preserve aspect ratio (target model dimensions e.g. 640x640).
     * 2. Extracts RGB channels into planar NCHW format [1, 3, H, W].
     * 3. Normalizes pixel values from [0..255] to [0.0f..1.0f].
     * 4. Encapsulates into an ONNX direct float tensor.
     */
    fun preprocessImage(sourceBitmap: Bitmap): PreprocessingResult {
        val srcWidth = sourceBitmap.width
        val srcHeight = sourceBitmap.height

        // Calculate aspect ratio scale and letterboxing padding
        val scale = min(inputWidth.toFloat() / srcWidth, inputHeight.toFloat() / srcHeight)
        val scaledWidth = (srcWidth * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (srcHeight * scale).toInt().coerceAtLeast(1)
        val padLeft = (inputWidth - scaledWidth) / 2
        val padTop = (inputHeight - scaledHeight) / 2

        // Create letterboxed bitmap
        val letterboxedBitmap = Bitmap.createBitmap(inputWidth, inputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(letterboxedBitmap)
        // Fill canvas with standard neutral letterbox gray (114, 114, 114)
        canvas.drawColor(Color.rgb(114, 114, 114))

        val scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, scaledWidth, scaledHeight, true)
        canvas.drawBitmap(scaledBitmap, padLeft.toFloat(), padTop.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
        if (scaledBitmap != sourceBitmap) {
            scaledBitmap.recycle()
        }

        // Extract pixels into normalized direct FloatBuffer in NCHW format
        val totalPixels = inputWidth * inputHeight
        val intValues = IntArray(totalPixels)
        letterboxedBitmap.getPixels(intValues, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        val floatBuffer = ByteBuffer.allocateDirect(1 * 3 * totalPixels * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        val rOffset = 0
        val gOffset = totalPixels
        val bOffset = 2 * totalPixels

        val floatArray = FloatArray(3 * totalPixels)

        for (i in 0 until totalPixels) {
            val pixel = intValues[i]
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            floatArray[rOffset + i] = r
            floatArray[gOffset + i] = g
            floatArray[bOffset + i] = b
        }

        floatBuffer.put(floatArray)
        floatBuffer.rewind()

        val env = ortEnvironment ?: OrtEnvironment.getEnvironment()
        val tensorShape = longArrayOf(1, 3, inputHeight.toLong(), inputWidth.toLong())
        val inputTensor = OnnxTensor.createTensor(env, floatBuffer, tensorShape)

        letterboxedBitmap.recycle()

        return PreprocessingResult(
            tensor = inputTensor,
            scale = scale,
            padLeft = padLeft,
            padTop = padTop,
            originalWidth = srcWidth,
            originalHeight = srcHeight
        )
    }

    /**
     * Executes real ONNX Runtime inference on the input bitmap and decodes detected Nüshu characters.
     */
    suspend fun runInference(
        bitmap: Bitmap,
        confidenceThreshold: Float = 0.30f,
        overlapThreshold: Float = 0.25f
    ): List<DetectedCharacter> = withContext(Dispatchers.Default) {
        val session = ortSession ?: return@withContext emptyList()

        var preprocessed: PreprocessingResult? = null
        var rawResult: OrtSession.Result? = null

        try {
            // 1. Preprocess image into normalized Float32 tensor
            preprocessed = preprocessImage(bitmap)

            // 2. Run ONNX Session
            val inputs = mapOf(inputTensorName to preprocessed.tensor)
            rawResult = session.run(inputs)

            // 3. Decode output bounding boxes
            val rawCandidates = decodeOutputTensor(
                rawResult = rawResult,
                prep = preprocessed,
                confidenceThreshold = confidenceThreshold
            )

            // 4. Apply strict Anti-Overlap NMS (25% overlap suppression)
            NmsEngine.suppressOverlaps(
                candidates = rawCandidates,
                confidenceThreshold = confidenceThreshold,
                overlapThreshold = overlapThreshold
            )
        } catch (e: Exception) {
            Log.e(TAG, "Inference execution error: ${e.message}", e)
            emptyList()
        } finally {
            preprocessed?.tensor?.close()
            rawResult?.close()
        }
    }

    /**
     * Decodes the raw output tensor from YOLOv8, YOLOv5, or custom ONNX detection heads.
     */
    private fun decodeOutputTensor(
        rawResult: OrtSession.Result,
        prep: PreprocessingResult,
        confidenceThreshold: Float
    ): List<DetectedCharacter> {
        val candidates = mutableListOf<DetectedCharacter>()
        val numGlyphs = NushuDictionary.GLYPHS.size

        if (rawResult.size() == 0) return emptyList()

        val outputValue = rawResult[0].value

        when (outputValue) {
            // Case 1: 3D Array [1, 4+C, N] or [1, N, 4+C] (YOLOv8 / YOLOv11 format)
            is Array<*> -> {
                val batch0 = outputValue[0]
                if (batch0 is Array<*>) {
                    val firstElem = batch0[0]
                    if (firstElem is FloatArray) {
                        // Shape is [1, num_channels, num_anchors] e.g. [1, 84, 8400]
                        val numChannels = batch0.size
                        val numAnchors = firstElem.size

                        val isTransposed = numChannels > numAnchors // if [1, 8400, 84]

                        if (!isTransposed) {
                            val numClasses = max(1, numChannels - 4)
                            var candidateId = 1

                            for (anchorIdx in 0 until numAnchors) {
                                // Extract bounding box in model letterbox coordinates
                                val cx = (batch0[0] as FloatArray)[anchorIdx]
                                val cy = (batch0[1] as FloatArray)[anchorIdx]
                                val w = (batch0[2] as FloatArray)[anchorIdx]
                                val h = (batch0[3] as FloatArray)[anchorIdx]

                                // Find max class probability
                                var maxScore = 0f
                                var bestClassId = 0

                                if (numClasses == 1) {
                                    maxScore = (batch0[4] as FloatArray)[anchorIdx]
                                    bestClassId = 0
                                } else {
                                    for (c in 0 until numClasses) {
                                        val score = (batch0[4 + c] as FloatArray)[anchorIdx]
                                        if (score > maxScore) {
                                            maxScore = score
                                            bestClassId = c
                                        }
                                    }
                                }

                                if (maxScore >= confidenceThreshold) {
                                    // Convert from model letterbox space back to original image coordinates
                                    val left = ((cx - w / 2f - prep.padLeft) / prep.scale).coerceIn(0f, prep.originalWidth.toFloat())
                                    val top = ((cy - h / 2f - prep.padTop) / prep.scale).coerceIn(0f, prep.originalHeight.toFloat())
                                    val right = ((cx + w / 2f - prep.padLeft) / prep.scale).coerceIn(0f, prep.originalWidth.toFloat())
                                    val bottom = ((cy + h / 2f - prep.padTop) / prep.scale).coerceIn(0f, prep.originalHeight.toFloat())

                                    if (right > left && bottom > top) {
                                        val glyph = NushuDictionary.GLYPHS[bestClassId % numGlyphs]
                                        candidates.add(
                                            DetectedCharacter(
                                                id = candidateId++,
                                                box = BoundingBox(left, top, right, bottom),
                                                confidence = maxScore,
                                                glyph = glyph,
                                                columnIndex = ((left / prep.originalWidth) * 4).toInt() + 1,
                                                rowIndex = ((top / prep.originalHeight) * 8).toInt() + 1,
                                                strokeDensity = maxScore
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            // Shape is [1, num_anchors, 4 + num_classes] (e.g. YOLOv5 or transposed v8)
                            var candidateId = 1
                            for (anchorIdx in 0 until numChannels) {
                                val anchorRow = batch0[anchorIdx] as FloatArray
                                if (anchorRow.size >= 5) {
                                    val cx = anchorRow[0]
                                    val cy = anchorRow[1]
                                    val w = anchorRow[2]
                                    val h = anchorRow[3]
                                    val conf = anchorRow[4]

                                    var classScore = conf
                                    var bestClassId = 0

                                    if (anchorRow.size > 5) {
                                        var maxCls = 0f
                                        for (c in 5 until anchorRow.size) {
                                            if (anchorRow[c] > maxCls) {
                                                maxCls = anchorRow[c]
                                                bestClassId = c - 5
                                            }
                                        }
                                        classScore = conf * maxCls
                                    }

                                    if (classScore >= confidenceThreshold) {
                                        val left = ((cx - w / 2f - prep.padLeft) / prep.scale).coerceIn(0f, prep.originalWidth.toFloat())
                                        val top = ((cy - h / 2f - prep.padTop) / prep.scale).coerceIn(0f, prep.originalHeight.toFloat())
                                        val right = ((cx + w / 2f - prep.padLeft) / prep.scale).coerceIn(0f, prep.originalWidth.toFloat())
                                        val bottom = ((cy + h / 2f - prep.padTop) / prep.scale).coerceIn(0f, prep.originalHeight.toFloat())

                                        if (right > left && bottom > top) {
                                            val glyph = NushuDictionary.GLYPHS[bestClassId % numGlyphs]
                                            candidates.add(
                                                DetectedCharacter(
                                                    id = candidateId++,
                                                    box = BoundingBox(left, top, right, bottom),
                                                    confidence = classScore,
                                                    glyph = glyph,
                                                    columnIndex = ((left / prep.originalWidth) * 4).toInt() + 1,
                                                    rowIndex = ((top / prep.originalHeight) * 8).toInt() + 1,
                                                    strokeDensity = classScore
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Case 2: Flattened FloatBuffer / FloatArray
            is FloatArray -> {
                Log.d(TAG, "Decoding 1D output float array of size ${outputValue.size}")
            }
        }

        return candidates
    }

    fun isModelLoaded(): Boolean = ortSession != null

    fun release() {
        try {
            ortSession?.close()
            ortSession = null
            ortEnvironment?.close()
            ortEnvironment = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ORT session", e)
        }
    }

    data class PreprocessingResult(
        val tensor: OnnxTensor,
        val scale: Float,
        val padLeft: Int,
        val padTop: Int,
        val originalWidth: Int,
        val originalHeight: Int
    )
}
