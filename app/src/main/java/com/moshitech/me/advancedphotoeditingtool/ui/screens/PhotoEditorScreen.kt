package com.moshitech.me.advancedphotoeditingtool.ui.screens













import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.moshitech.me.advancedphotoeditingtool.ui.theme.AdvancedPhotoEditingToolTheme
import com.moshitech.me.advancedphotoeditingtool.uriToBitmap
import com.moshitech.me.advancedphotoeditingtool.applyTransformations






enum class PhotoEditorTool {
    AUTO, FILTERS, CROP, EFFECTS, TEXT
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
    var straightenAngle by remember { mutableStateOf(0f) }
    
    
    

    

    LaunchedEffect(selectedImageUri, capturedImageBitmap) {
        currentImageBitmap = capturedImageBitmap ?: selectedImageUri?.let { uriToBitmap(it, context) }
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
                    CropScreen(
                        bitmap = currentImageBitmap!!,
                        onApplyCrop = { croppedBitmap ->
                            onCropApply(croppedBitmap)
                            currentTool = PhotoEditorTool.AUTO
                        },
                        onCancelCrop = {
                            onCropCancel()
                            currentTool = PhotoEditorTool.AUTO
                        }
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
                sheetState = sheetState
            ) {
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