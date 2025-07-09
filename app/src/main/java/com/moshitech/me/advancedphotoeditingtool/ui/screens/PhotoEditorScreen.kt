package com.moshitech.me.advancedphotoeditingtool.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.moshitech.me.advancedphotoeditingtool.applyTransformations
import com.moshitech.me.advancedphotoeditingtool.ui.theme.AdvancedPhotoEditingToolTheme
import com.moshitech.me.advancedphotoeditingtool.uriToBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.moshitech.me.advancedphotoeditingtool.cropBitmap

enum class PhotoEditorTool {
    AUTO, FILTERS, CROP, EFFECTS, TEXT, ROTATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    selectedImageUri: Uri?,
    capturedImageBitmap: Bitmap?,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCompare: () -> Unit,
    onSave: () -> Unit,
    onPickPhoto: () -> Unit,
    onCapturePhoto: () -> Unit,
    onExportPhoto: (Bitmap?) -> Unit,
    onCropApply: (Bitmap) -> Unit,
    onCropCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTool by remember { mutableStateOf(PhotoEditorTool.AUTO) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var currentImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var rotation by remember { mutableStateOf(0f) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }
    
    // Hoisted crop state
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var straightenAngle by remember { mutableStateOf(0f) }
    var selectedAspectRatio by remember { mutableStateOf(AspectRatio.FREE) }
    var showGoldenRatioGuide by remember { mutableStateOf(false) }
    var showDiagonalGuide by remember { mutableStateOf(false) }
    var showZoomSlider by remember { mutableStateOf(false) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }


    LaunchedEffect(selectedImageUri, capturedImageBitmap) {
        currentImageBitmap = capturedImageBitmap ?: selectedImageUri?.let { uriToBitmap(it, context) }
        currentTool = PhotoEditorTool.AUTO // Reset tool when a new image is loaded
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Editor") },
                navigationIcon = {
                    IconButton(onClick = onUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                },
                actions = {
                    IconButton(onClick = onRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }
                    IconButton(onClick = onCompare) {
                        Icon(Icons.Default.Compare, contentDescription = "Compare")
                    }
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentImageBitmap != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloatingActionButton(onClick = { onExportPhoto(currentImageBitmap) }) {
                        Icon(Icons.Default.SaveAlt, contentDescription = "Export Photo")
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolButton(tool = PhotoEditorTool.AUTO, currentTool = currentTool) {
                        currentTool = it
                        showBottomSheet = true
                    }
                    ToolButton(tool = PhotoEditorTool.FILTERS, currentTool = currentTool) {
                        currentTool = it
                        showBottomSheet = true
                    }
                    ToolButton(tool = PhotoEditorTool.CROP, currentTool = currentTool) {
                        currentTool = it
                        showBottomSheet = true
                    }
                    ToolButton(tool = PhotoEditorTool.EFFECTS, currentTool = currentTool) {
                        currentTool = it
                        showBottomSheet = true
                    }
                    ToolButton(tool = PhotoEditorTool.TEXT, currentTool = currentTool) {
                        currentTool = it
                        showBottomSheet = true
                    }
                    ToolButton(tool = PhotoEditorTool.ROTATE, currentTool = currentTool) {
                        currentTool = it
                        showBottomSheet = true
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central Image Preview
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                if (currentTool == PhotoEditorTool.CROP && currentImageBitmap != null) {
                    CropImageArea(
                        bitmap = currentImageBitmap!!,
                        cropRect = cropRect,
                        scale = scale,
                        rotation = rotation,
                        offset = offset,
                        straightenAngle = straightenAngle,
                        containerSize = containerSize,
                        onCropRectChange = { cropRect = it },
                        onScaleChange = { scale = it },
                        onRotationChange = { rotation = it },
                        onOffsetChange = { offset = it },
                        onContainerSizeChange = { containerSize = it },
                        aspectRatio = selectedAspectRatio,
                        showGoldenRatioGuide = showGoldenRatioGuide,
                        showDiagonalGuide = showDiagonalGuide
                    )
                } else {
                    currentImageBitmap?.let {
                        val imageBitmap by remember(it, rotation, flipHorizontal, flipVertical, straightenAngle) {
                            derivedStateOf {
                                applyTransformations(it, rotation.toInt(), flipHorizontal, flipVertical, straightenAngle).asImageBitmap()
                            }
                        }

                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Edited Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        Box(modifier = Modifier.fillMaxSize()) {
                            SmallFloatingActionButton(
                                onClick = onPickPhoto,
                                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Change Photo")
                                Text("Change Photo")
                            }
                        }
                    }
                }
                if (currentImageBitmap == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Button(
                            onClick = onCapturePhoto,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                            Spacer(Modifier.width(8.dp))
                            Text("Camera")
                        }
                        Button(
                            onClick = onPickPhoto,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Browse Gallery")
                            Spacer(Modifier.width(8.dp))
                            Text("Browse Gallery")
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                if (currentTool == PhotoEditorTool.CROP) {
                    CropControls(
                        scale = scale,
                        straightenAngle = straightenAngle,
                        selectedAspectRatio = selectedAspectRatio,
                        showGoldenRatioGuide = showGoldenRatioGuide,
                        showDiagonalGuide = showDiagonalGuide,
                        showZoomSlider = showZoomSlider,
                        onScaleChange = { scale = it },
                        onStraightenAngleChange = { straightenAngle = it },
                        onAspectRatioChange = { selectedAspectRatio = it },
                        onShowGoldenRatioGuideChange = { showGoldenRatioGuide = it },
                        onShowDiagonalGuideChange = { showDiagonalGuide = it },
                        onShowZoomSliderChange = { showZoomSlider = it },
                        onApply = {
                            currentImageBitmap?.let {
                                val croppedBitmap = cropBitmap(
                                    it,
                                    cropRect,
                                    scale,
                                    rotation + straightenAngle,
                                    offset,
                                    containerSize
                                )
                                onCropApply(croppedBitmap)
                            }
                            showBottomSheet = false
                            currentTool = PhotoEditorTool.AUTO
                        },
                        onCancel = {
                            onCropCancel()
                            showBottomSheet = false
                            currentTool = PhotoEditorTool.AUTO
                        },
                        onReset = {
                            scale = 1f
                            rotation = 0f
                            offset = Offset.Zero
                            cropRect = Rect.Zero
                            straightenAngle = 0f
                            selectedAspectRatio = AspectRatio.FREE
                            showGoldenRatioGuide = false
                            showDiagonalGuide = false
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .background(Color.LightGray, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.height(12.dp))
                        BottomSheetContent(
                            currentTool = currentTool,
                            currentImageBitmap = currentImageBitmap,
                            onCropApply = onCropApply,
                            onCropCancel = onCropCancel,
                            rotation = rotation,
                            onRotationChange = { rotation = it },
                            flipHorizontal = flipHorizontal,
                            onFlipHorizontalChange = { flipHorizontal = it },
                            flipVertical = flipVertical,
                            onFlipVerticalChange = { flipVertical = it },
                            straightenAngle = straightenAngle,
                            onStraightenAngleChange = { straightenAngle = it },
                            showBottomSheet = { showBottomSheet = it },
                            currentToolChange = { currentTool = it }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoEditorScreenPreview() {
    AdvancedPhotoEditingToolTheme {
        PhotoEditorScreen(
            selectedImageUri = null,
            capturedImageBitmap = null,
            onUndo = {},
            onRedo = {},
            onCompare = {},
            onSave = {},
            onPickPhoto = {},
            onCapturePhoto = {},
            onExportPhoto = { _ -> },
            onCropApply = { _ -> },
            onCropCancel = {}
        )
    }
}