package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DetectedCharacter
import com.example.data.DetectionResult
import com.example.data.ManuscriptPreset
import com.example.data.local.entity.ScanRecordEntity
import com.example.data.repository.NushuRepository
import com.example.engine.NmsEngine
import com.example.engine.OnnxModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetectorUiState(
    val originalBitmap: Bitmap? = null,
    val detectionResult: DetectionResult? = null,
    val selectedCharacter: DetectedCharacter? = null,
    val confidenceThreshold: Float = NmsEngine.DEFAULT_CONFIDENCE_THRESHOLD, // Default 0.30
    val overlapThreshold: Float = NmsEngine.OVERLAP_SUPPRESSION_THRESHOLD,   // Strictly 0.25 (25%)
    val strokeThicknessPx: Float = 1.0f,                                    // Strictly 1px
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val currentPresetId: String? = null,
    val showBoundingBoxes: Boolean = true,
    val showIndices: Boolean = true,
    val showColumnGuides: Boolean = false,
    val currentTitle: String = "Bamboo Manuscript Scan",
    val isSaved: Boolean = false,
    val onnxModelInfo: OnnxModelInfo = OnnxModelInfo()
)

class DetectorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NushuRepository(application)

    private val _uiState = MutableStateFlow(
        DetectorUiState(onnxModelInfo = repository.getOnnxModelInfo())
    )
    val uiState: StateFlow<DetectorUiState> = _uiState.asStateFlow()

    val presets: List<ManuscriptPreset> = repository.samplePresets

    val scanHistory: StateFlow<List<ScanRecordEntity>> = repository.scanHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load initial preset automatically so user immediately has an authentic sample ready
        presets.firstOrNull()?.let { initialPreset ->
            loadPreset(initialPreset)
        }
    }

    fun refreshModelStatus() {
        _uiState.value = _uiState.value.copy(
            onnxModelInfo = repository.getOnnxModelInfo()
        )
    }

    fun reloadOnnxModel(modelName: String = "best.onnx") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Loading ONNX model ($modelName)...")
            val success = withContext(Dispatchers.IO) {
                repository.reloadOnnxModel(modelName)
            }
            val modelInfo = repository.getOnnxModelInfo()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                onnxModelInfo = modelInfo,
                statusMessage = if (success) "ORT: ${modelInfo.modelName} loaded successfully" else modelInfo.statusMessage
            )
            reprocessWithCurrentSettings()
        }
    }

    fun loadCustomOnnxModelUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Importing custom ONNX model...")
            val success = withContext(Dispatchers.IO) {
                repository.loadOnnxModelFromUri(uri)
            }
            val modelInfo = repository.getOnnxModelInfo()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                onnxModelInfo = modelInfo,
                statusMessage = if (success) "Custom ONNX loaded successfully" else "Failed to load ONNX model"
            )
            reprocessWithCurrentSettings()
        }
    }

    fun loadPreset(preset: ManuscriptPreset) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                statusMessage = "Loading ${preset.title}...",
                currentPresetId = preset.id,
                currentTitle = preset.title,
                selectedCharacter = null,
                isSaved = false,
                onnxModelInfo = repository.getOnnxModelInfo()
            )

            val bitmap = withContext(Dispatchers.IO) {
                repository.loadBitmapFromResource(preset.drawableResId)
            }

            if (bitmap != null) {
                runInferenceOnBitmap(bitmap, preset.title, preset.id)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Failed to load preset manuscript image"
                )
            }
        }
    }

    fun loadFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                statusMessage = "Loading image from storage...",
                currentPresetId = null,
                currentTitle = "Custom Manuscript Upload",
                selectedCharacter = null,
                isSaved = false,
                onnxModelInfo = repository.getOnnxModelInfo()
            )

            val bitmap = withContext(Dispatchers.IO) {
                repository.loadBitmapFromUri(uri)
            }

            if (bitmap != null) {
                runInferenceOnBitmap(bitmap, "Custom Manuscript Upload", null)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Failed to load selected image"
                )
            }
        }
    }

    fun loadFromCameraBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                statusMessage = "Processing camera frame via ONNX Runtime...",
                currentPresetId = null,
                currentTitle = "Camera Capture Scan",
                selectedCharacter = null,
                isSaved = false,
                onnxModelInfo = repository.getOnnxModelInfo()
            )

            runInferenceOnBitmap(bitmap, "Camera Capture Scan", null)
        }
    }

    private suspend fun runInferenceOnBitmap(
        bitmap: Bitmap,
        title: String,
        presetId: String?
    ) {
        try {
            val currentState = _uiState.value
            val result = repository.runDetection(
                bitmap = bitmap,
                confidenceThreshold = currentState.confidenceThreshold,
                overlapThreshold = currentState.overlapThreshold,
                strokeThickness = currentState.strokeThicknessPx
            )

            _uiState.value = _uiState.value.copy(
                originalBitmap = bitmap,
                detectionResult = result,
                isLoading = false,
                statusMessage = "Detected ${result.filteredCharacters.size} characters in ${result.inferenceTimeMs}ms",
                currentTitle = title,
                currentPresetId = presetId,
                onnxModelInfo = repository.getOnnxModelInfo()
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = "Inference error: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    fun updateConfidenceThreshold(newThreshold: Float) {
        val clamped = newThreshold.coerceIn(0.10f, 0.90f)
        _uiState.value = _uiState.value.copy(confidenceThreshold = clamped)
        reprocessWithCurrentSettings()
    }

    fun updateOverlapThreshold(newThreshold: Float) {
        val clamped = newThreshold.coerceIn(0.10f, 0.80f)
        _uiState.value = _uiState.value.copy(overlapThreshold = clamped)
        reprocessWithCurrentSettings()
    }

    fun toggleBoundingBoxes(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBoundingBoxes = show)
    }

    fun toggleIndices(show: Boolean) {
        _uiState.value = _uiState.value.copy(showIndices = show)
        reprocessWithCurrentSettings()
    }

    fun toggleColumnGuides(show: Boolean) {
        _uiState.value = _uiState.value.copy(showColumnGuides = show)
    }

    fun selectCharacter(character: DetectedCharacter?) {
        _uiState.value = _uiState.value.copy(selectedCharacter = character)
    }

    private fun reprocessWithCurrentSettings() {
        val bitmap = _uiState.value.originalBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val currentState = _uiState.value
            val result = repository.runDetection(
                bitmap = bitmap,
                confidenceThreshold = currentState.confidenceThreshold,
                overlapThreshold = currentState.overlapThreshold,
                strokeThickness = currentState.strokeThicknessPx
            )
            _uiState.value = _uiState.value.copy(
                detectionResult = result,
                isLoading = false,
                statusMessage = "Updated: ${result.filteredCharacters.size} characters detected"
            )
        }
    }

    fun saveCurrentScan() {
        val result = _uiState.value.detectionResult ?: return
        viewModelScope.launch {
            try {
                repository.saveScanRecord(
                    title = _uiState.value.currentTitle,
                    result = result,
                    samplePresetId = _uiState.value.currentPresetId
                )
                _uiState.value = _uiState.value.copy(
                    isSaved = true,
                    statusMessage = "Scan record saved to local database"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Failed to save: ${e.localizedMessage}"
                )
            }
        }
    }

    fun deleteScan(record: ScanRecordEntity) {
        viewModelScope.launch {
            repository.deleteScanRecord(record)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
