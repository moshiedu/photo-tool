package com.moshitech.me.advancedphotoeditingtool

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.moshitech.me.advancedphotoeditingtool.ui.screens.PhotoEditorScreen
import com.moshitech.me.advancedphotoeditingtool.ui.theme.AdvancedPhotoEditingToolTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdvancedPhotoEditingToolTheme {
                var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
                var capturedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
                

                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        selectedImageUri = uri
                        capturedImageBitmap = null
                    }
                )

                val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
                var photoUri by remember { mutableStateOf<Uri?>(null) }
                val cameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicture(),
                    onResult = { success ->
                        if (success) {
                            selectedImageUri = photoUri
                            capturedImageBitmap = null
                            
                        } else {
                            Toast.makeText(context, "Image capture failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                PhotoEditorScreen(
                    selectedImageUri = selectedImageUri,
                    capturedImageBitmap = capturedImageBitmap,
                    onUndo = { /* TODO: Implement Undo */ },
                    onRedo = { /* TODO: Implement Redo */ },
                    onCompare = { /* TODO: Implement Compare */ },
                    onSave = { /* TODO: Implement Save */ },
                    onPickPhoto = {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCapturePhoto = {
                        scope.launch {
                            if (cameraPermissionState.status.isGranted) {
                                val newPhotoUri = createImageUri(context)
                            photoUri = newPhotoUri
                            cameraLauncher.launch(newPhotoUri)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    },
                    onExportPhoto = { bitmapToSave ->
                        bitmapToSave?.let {
                            saveBitmapToGallery(context, it, "EditedPhoto")
                        }
                    },
                    onCropApply = { croppedBitmap ->
                        // No longer needed here, CropScreen handles it
                    },
                    onCropCancel = {
                        // No longer needed here, CropScreen handles it
                    }
                )
            }
        }
    }
}




