package com.moshitech.me.advancedphotoeditingtool.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment

@Composable
fun ToolButton(tool: PhotoEditorTool, currentTool: PhotoEditorTool, onClick: (PhotoEditorTool) -> Unit) {
    val icon = when (tool) {
        PhotoEditorTool.AUTO -> Icons.Default.AutoFixHigh
        PhotoEditorTool.FILTERS -> Icons.Default.FilterVintage
        PhotoEditorTool.CROP -> Icons.Default.Crop
        PhotoEditorTool.EFFECTS -> Icons.Default.Stars
        PhotoEditorTool.TEXT -> Icons.Default.TextFields
    }
    val tint = if (tool == currentTool) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        IconButton(onClick = { onClick(tool) }) {
            Icon(icon, contentDescription = tool.name, tint = tint)
        }
        Text(tool.name, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

import com.moshitech.me.advancedphotoeditingtool.applyTransformations

@Composable
fun BottomSheetContent(
    currentTool: PhotoEditorTool,
    currentImageBitmap: Bitmap?,
    onCropApply: (Bitmap) -> Unit,
    onCropCancel: () -> Unit,
    rotation: Float,
    onRotationChange: (Float) -> Unit,
    flipHorizontal: Boolean,
    onFlipHorizontalChange: (Boolean) -> Unit,
    flipVertical: Boolean,
    onFlipVerticalChange: (Boolean) -> Unit,
    straightenAngle: Float,
    onStraightenAngleChange: (Float) -> Unit,
    showBottomSheet: (Boolean) -> Unit,
    currentToolChange: (PhotoEditorTool) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Options for ${currentTool.name}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        // Dynamic content based on currentTool
        when (currentTool) {
            PhotoEditorTool.AUTO -> {
                Text("Auto-enhance options will go here.")
                // Add sliders, buttons for auto adjustments
            }
            PhotoEditorTool.FILTERS -> {
                Text("Filter thumbnails will go here.")
                // Add filter previews
            }
            
            PhotoEditorTool.EFFECTS -> {
                Text("Effect options will go here.")
                // Add effect controls
            }
            PhotoEditorTool.TEXT -> {
                Text("Text editing options will go here.")
                // Add text input, font, color pickers
            }
            else -> {
                Text("Options for ${currentTool.name} will go here.")
            }
        }
        Spacer(Modifier.height(32.dp)) // Space for bottom sheet handle
    }
}