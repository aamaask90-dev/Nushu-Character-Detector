package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.CharacterDetailSheet
import com.example.ui.components.CharacterGridSection
import com.example.ui.components.DetectionSummaryCard
import com.example.ui.components.ManuscriptCanvas
import com.example.ui.components.PresetSelectorSection
import com.example.ui.components.ScanHistorySheet
import com.example.ui.components.ThresholdSliderCard
import com.example.ui.theme.BambooAmber
import com.example.ui.theme.BambooBackground
import com.example.ui.theme.BambooBorder
import com.example.ui.theme.BambooCard
import com.example.ui.theme.BambooDarkSurface
import com.example.ui.theme.BambooGold
import com.example.ui.theme.BambooTextPrimary
import com.example.ui.theme.BambooTextSecondary
import com.example.ui.theme.NeonGreen
import com.example.ui.viewmodel.DetectorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectorScreen(
    viewModel: DetectorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadFromUri(uri)
        }
    }

    // ONNX Model File Picker launcher
    val onnxFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadCustomOnnxModelUri(uri)
        }
    }

    // Camera Capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.loadFromCameraBitmap(bitmap)
        }
    }

    // Camera Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission needed to scan manuscripts", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleCameraClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            cameraLauncher.launch(null)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BambooBackground),
        containerColor = BambooBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Nüshu Detector",
                                color = BambooTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ORT Engine • best.onnx",
                                color = BambooTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Reload / Load ONNX Model button
                    IconButton(
                        onClick = {
                            viewModel.reloadOnnxModel("best.onnx")
                            scope.launch {
                                snackbarHostState.showSnackbar("Reloading best.onnx ORT session...")
                            }
                        },
                        modifier = Modifier.testTag("reload_onnx_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Reload ONNX Model",
                            tint = if (uiState.onnxModelInfo.isLoaded) NeonGreen else BambooGold
                        )
                    }

                    // Save to history button
                    IconButton(
                        onClick = {
                            viewModel.saveCurrentScan()
                            scope.launch {
                                snackbarHostState.showSnackbar("Scan saved to offline database")
                            }
                        },
                        enabled = uiState.detectionResult != null,
                        modifier = Modifier.testTag("save_scan_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save scan",
                            tint = if (uiState.isSaved) NeonGreen else BambooTextSecondary
                        )
                    }

                    // View History button
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Scan History",
                            tint = BambooGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BambooDarkSurface,
                    titleContentColor = BambooTextPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Curated Historical Presets, Camera Capture, and Gallery Upload
                PresetSelectorSection(
                    presets = viewModel.presets,
                    activePresetId = uiState.currentPresetId,
                    onPresetSelected = { viewModel.loadPreset(it) },
                    onCameraClick = { handleCameraClick() },
                    onGalleryClick = { galleryLauncher.launch("image/*") }
                )

                // 2. Interactive Bamboo Manuscript Canvas with Neon Fluorescent Green 1px Bounding Boxes
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ManuscriptCanvas(
                        bitmap = uiState.originalBitmap,
                        characters = uiState.detectionResult?.filteredCharacters ?: emptyList(),
                        selectedCharacter = uiState.selectedCharacter,
                        showBoxes = uiState.showBoundingBoxes,
                        showIndices = uiState.showIndices,
                        showColumnGuides = uiState.showColumnGuides,
                        onCharacterSelected = { viewModel.selectCharacter(it) }
                    )

                    if (uiState.isLoading) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp)),
                            color = Color(0xDD0C0F0C)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = NeonGreen,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = uiState.statusMessage ?: "Running ONNX inference...",
                                    color = BambooTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 3. Accurate Character Count & ONNX Inference Summary Card
                DetectionSummaryCard(
                    result = uiState.detectionResult,
                    modelInfo = uiState.onnxModelInfo
                )

                // 4. Dynamic Confidence Threshold (0.3) & Anti-Overlap NMS Controls
                ThresholdSliderCard(
                    confidenceThreshold = uiState.confidenceThreshold,
                    overlapThreshold = uiState.overlapThreshold,
                    strokeThickness = uiState.strokeThicknessPx,
                    showBoxes = uiState.showBoundingBoxes,
                    showIndices = uiState.showIndices,
                    showColumns = uiState.showColumnGuides,
                    onConfidenceChanged = { viewModel.updateConfidenceThreshold(it) },
                    onToggleBoxes = { viewModel.toggleBoundingBoxes(it) },
                    onToggleIndices = { viewModel.toggleIndices(it) },
                    onToggleColumns = { viewModel.toggleColumnGuides(it) }
                )

                // 5. Scrollable Character Identification List
                uiState.detectionResult?.let { res ->
                    CharacterGridSection(
                        characters = res.filteredCharacters,
                        selectedCharacter = uiState.selectedCharacter,
                        onCharacterClick = { viewModel.selectCharacter(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom Sheet for Inspecting Selected Character
        if (uiState.selectedCharacter != null) {
            CharacterDetailSheet(
                character = uiState.selectedCharacter,
                onDismiss = { viewModel.selectCharacter(null) }
            )
        }

        // Bottom Sheet for Scan History
        if (showHistorySheet) {
            ScanHistorySheet(
                records = scanHistory,
                onDeleteRecord = { viewModel.deleteScan(it) },
                onClearAll = { viewModel.clearHistory() },
                onDismiss = { showHistorySheet = false }
            )
        }
    }
}
