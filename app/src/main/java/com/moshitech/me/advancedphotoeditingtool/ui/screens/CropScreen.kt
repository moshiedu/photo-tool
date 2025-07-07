package com.moshitech.me.advancedphotoeditingtool.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toRect
import com.moshitech.me.advancedphotoeditingtool.cropBitmap
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

enum class AspectRatio(val width: Float, val height: Float) {
    FREE(0f, 0f),
    SQUARE(1f, 1f),
    THREE_FOUR(3f, 4f),
    FOUR_THREE(4f, 3f),
    SIXTEEN_NINE(16f, 9f),
    NINE_SIXTEEN(9f, 16f)
}

enum class CropHandle {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, TOP, RIGHT, BOTTOM
}

fun getCropHandle(offset: Offset, rect: Rect?, handleSize: Float): CropHandle? {
    if (rect == null) return null
    val handles = mapOf(
        CropHandle.TOP_LEFT to Rect(rect.topLeft, handleSize),
        CropHandle.TOP_RIGHT to Rect(rect.topRight, handleSize),
        CropHandle.BOTTOM_LEFT to Rect(rect.bottomLeft, handleSize),
        CropHandle.BOTTOM_RIGHT to Rect(rect.bottomRight, handleSize),
        CropHandle.LEFT to Rect(rect.centerLeft, handleSize),
        CropHandle.TOP to Rect(rect.topCenter, handleSize),
        CropHandle.RIGHT to Rect(rect.centerRight, handleSize),
        CropHandle.BOTTOM to Rect(rect.bottomCenter, handleSize)
    )

    return handles.entries.find { it.value.contains(offset) }?.key
}

fun updateCropRect(rect: Rect?, dragAmount: Offset, handle: CropHandle, aspectRatio: AspectRatio, containerSize: IntSize): Rect? {
    if (rect == null) return null

    var newRect = rect
    when (handle) {
        CropHandle.TOP_LEFT -> newRect = rect.copy(left = rect.left + dragAmount.x, top = rect.top + dragAmount.y)
        CropHandle.TOP_RIGHT -> newRect = rect.copy(right = rect.right + dragAmount.x, top = rect.top + dragAmount.y)
        CropHandle.BOTTOM_LEFT -> newRect = rect.copy(left = rect.left + dragAmount.x, bottom = rect.bottom + dragAmount.y)
        CropHandle.BOTTOM_RIGHT -> newRect = rect.copy(right = rect.right + dragAmount.x, bottom = rect.bottom + dragAmount.y)
        CropHandle.LEFT -> newRect = rect.copy(left = rect.left + dragAmount.x)
        CropHandle.TOP -> newRect = rect.copy(top = rect.top + dragAmount.y)
        CropHandle.RIGHT -> newRect = rect.copy(right = rect.right + dragAmount.x)
        CropHandle.BOTTOM -> newRect = rect.copy(bottom = rect.bottom + dragAmount.y)
    }

    if (aspectRatio != AspectRatio.FREE) {
        val currentAspectRatio = newRect.width / newRect.height
        val targetAspectRatio = aspectRatio.width / aspectRatio.height

        if (currentAspectRatio > targetAspectRatio) {
            // Width is too large, adjust width based on height
            val newWidth = newRect.height * targetAspectRatio
            when (handle) {
                CropHandle.TOP_LEFT, CropHandle.BOTTOM_LEFT, CropHandle.LEFT -> newRect = newRect.copy(left = newRect.right - newWidth)
                CropHandle.TOP_RIGHT, CropHandle.BOTTOM_RIGHT, CropHandle.RIGHT -> newRect = newRect.copy(right = newRect.left + newWidth)
                else -> {}
            }
        } else if (currentAspectRatio < targetAspectRatio) {
            // Height is too large, adjust height based on width
            val newHeight = newRect.width / targetAspectRatio
            when (handle) {
                CropHandle.TOP_LEFT, CropHandle.TOP_RIGHT, CropHandle.TOP -> newRect = newRect.copy(top = newRect.bottom - newHeight)
                CropHandle.BOTTOM_LEFT, CropHandle.BOTTOM_RIGHT, CropHandle.BOTTOM -> newRect = newRect.copy(bottom = newRect.top + newHeight)
                else -> {}
            }
        }
    }

    // Ensure cropRect stays within container bounds and has minimum size
    val minCropSize = 50f // pixels
    val constrainedRect = androidx.compose.ui.geometry.Rect(
        left = newRect.left.coerceIn(0f, containerSize.width - newRect.width),
        top = newRect.top.coerceIn(0f, containerSize.height - newRect.height),
        right = newRect.right.coerceIn(newRect.width, containerSize.width.toFloat()),
        bottom = newRect.bottom.coerceIn(newRect.height, containerSize.height.toFloat())
    )

    return if (constrainedRect.width >= minCropSize && constrainedRect.height >= minCropSize) {
        constrainedRect
    } else {
        rect // Revert to old rect if new one is too small
    }
}

@Composable
fun CropScreen(
    bitmap: Bitmap,
    onApplyCrop: (Bitmap) -> Unit,
    onCancelCrop: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var cropRect by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var straightenAngle by remember { mutableStateOf(0f) }
    var selectedAspectRatio by remember { mutableStateOf(AspectRatio.FREE) }
    var showGoldenRatioGuide by remember { mutableStateOf(false) }
    var showDiagonalGuide by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged {
                    containerSize = it
                    if (cropRect == androidx.compose.ui.geometry.Rect.Zero) {
                        // Initialize crop rect to a default size, e.g., 80% of container
                        val cropWidth = it.width * 0.8f
                        val cropHeight = it.height * 0.8f
                        val cropLeft = (it.width - cropWidth) / 2f
                        val cropTop = (it.height - cropHeight) / 2f
                        cropRect = androidx.compose.ui.geometry.Rect(cropLeft, cropTop, cropLeft + cropWidth, cropTop + cropHeight)
                    }
                }
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, gestureRotation ->
                        scale *= zoom
                        rotation += gestureRotation
                        offset += pan
                    }
                }
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offset.x
                        translationY = offset.y
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation + straightenAngle
                    }
            )

            // Crop overlay
            var draggingHandle by remember { mutableStateOf<Int?>(null) }

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Faded outside area
                drawRect(color = Color.Black.copy(alpha = 0.6f), size = size)
                drawRect(color = Color.Transparent, topLeft = cropRect.topLeft, size = cropRect.size, blendMode = androidx.compose.ui.graphics.BlendMode.SrcOut)

                // Crop rectangle
                drawRect(
                    color = Color.White,
                    topLeft = cropRect.topLeft,
                    size = cropRect.size,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Rule of thirds overlay
                val numLines = 3
                val hStep = cropRect.width / numLines
                val vStep = cropRect.height / numLines

                for (i in 1 until numLines) {
                    drawLine(
                        color = Color.White,
                        start = Offset(cropRect.left + i * hStep, cropRect.top),
                        end = Offset(cropRect.left + i * hStep, cropRect.bottom),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(cropRect.left, cropRect.top + i * vStep),
                        end = Offset(cropRect.right, cropRect.top + i * vStep),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Optional Golden Ratio/Diagonal Guides
                if (showGoldenRatioGuide) {
                    // Implement Golden Ratio guides
                    val phi = 1.618f
                    val h1 = cropRect.width / phi
                    val h2 = cropRect.width - h1
                    val v1 = cropRect.height / phi
                    val v2 = cropRect.height - v1

                    drawLine(color = Color.Yellow, start = Offset(cropRect.left + h1, cropRect.top), end = Offset(cropRect.left + h1, cropRect.bottom), strokeWidth = 1.dp.toPx())
                    drawLine(color = Color.Yellow, start = Offset(cropRect.left + h2, cropRect.top), end = Offset(cropRect.left + h2, cropRect.bottom), strokeWidth = 1.dp.toPx())
                    drawLine(color = Color.Yellow, start = Offset(cropRect.left, cropRect.top + v1), end = Offset(cropRect.right, cropRect.top + v1), strokeWidth = 1.dp.toPx())
                    drawLine(color = Color.Yellow, start = Offset(cropRect.left, cropRect.top + v2), end = Offset(cropRect.right, cropRect.top + v2), strokeWidth = 1.dp.toPx())
                }

                if (showDiagonalGuide) {
                    // Implement Diagonal guides
                    drawLine(color = Color.Cyan, start = cropRect.topLeft, end = cropRect.bottomRight, strokeWidth = 1.dp.toPx())
                    drawLine(color = Color.Cyan, start = cropRect.topRight, end = cropRect.bottomLeft, strokeWidth = 1.dp.toPx())
                }

                val handleSize = 24.dp.toPx()

                // Draw corner handles
                drawRect(color = if (draggingHandle == CropHandle.TOP_LEFT.ordinal) Color.Red else Color.Blue, topLeft = cropRect.topLeft - Offset(handleSize/2, handleSize/2), size = Size(handleSize, handleSize))
                drawRect(color = if (draggingHandle == CropHandle.TOP_RIGHT.ordinal) Color.Red else Color.Blue, topLeft = cropRect.topRight - Offset(handleSize/2, handleSize/2), size = Size(handleSize, handleSize))
                drawRect(color = if (draggingHandle == CropHandle.BOTTOM_LEFT.ordinal) Color.Red else Color.Blue, topLeft = cropRect.bottomLeft - Offset(handleSize/2, handleSize/2), size = Size(handleSize, handleSize))
                drawRect(color = if (draggingHandle == CropHandle.BOTTOM_RIGHT.ordinal) Color.Red else Color.Blue, topLeft = cropRect.bottomRight - Offset(handleSize/2, handleSize/2), size = Size(handleSize, handleSize))

                // Draw edge handles
                drawRect(color = if (draggingHandle == CropHandle.LEFT.ordinal) Color.Red else Color.Blue, topLeft = cropRect.centerLeft - Offset(handleSize/2, handleSize/2), size = Size(handleSize, handleSize))
                drawRect(color = if (draggingHandle == CropHandle.TOP.ordinal) Color.Red else Color.Blue, topLeft = cropRect.topCenter - Offset(handleSize/2, handleSize/2), size = Size(handleSize, handleSize))
                drawRect(color = if (draggingHandle == CropHandle.RIGHT.ordinal) Color.Red else Color.Blue, topLeft = cropRect.centerRight - Offset(handleSize/2, handleSize/2), size = Size(handleSize, handleSize))
                drawRect(color = if (draggingHandle == CropHandle.BOTTOM.ordinal) Color.Red else Color.Blue, topLeft = cropRect.bottomCenter - Offset(handleSize/2, handleSize/2), size = Size(handleSize, handleSize))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                val handle = getCropHandle(it, cropRect, 24.dp.toPx())
                                if (handle != null) {
                                    draggingHandle = handle.ordinal
                                }
                            },
                            onDragEnd = { draggingHandle = null }
                        ) {
                            change, dragAmount ->
                            change.consume()
                            if (draggingHandle != null) {
                                cropRect = updateCropRect(cropRect, dragAmount, CropHandle.values()[draggingHandle!!], selectedAspectRatio, containerSize) ?: cropRect
                            } else {
                                // Pan image inside crop
                                offset += dragAmount
                            }
                        }
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Crop Options",
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Straighten Slider
            Text(text = "Straighten", color = Color.White)
            Slider(
                value = straightenAngle,
                onValueChange = { straightenAngle = it },
                valueRange = -45f..45f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Aspect Ratio controls
            Text(text = "Aspect Ratio", color = Color.White)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AspectRatio.values().forEach { ratio ->
                    Button(onClick = { selectedAspectRatio = ratio }) {
                        Text(ratio.name)
                    }
                }
            }

            // Guide toggles
            Text(text = "Guides", color = Color.White)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { showGoldenRatioGuide = !showGoldenRatioGuide }) {
                    Text("Golden Ratio")
                }
                Button(onClick = { showDiagonalGuide = !showDiagonalGuide }) {
                    Text("Diagonal Guides")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = onCancelCrop) {
                    Text("Cancel")
                }
                Button(onClick = { /* Reset all crop-related states */
                    scale = 1f
                    rotation = 0f
                    offset = Offset.Zero
                    cropRect = androidx.compose.ui.geometry.Rect.Zero
                    straightenAngle = 0f
                    selectedAspectRatio = AspectRatio.FREE
                    showGoldenRatioGuide = false
                    showDiagonalGuide = false
                }) {
                    Text("Reset")
                }
                Button(onClick = {
                    val croppedBitmap = cropBitmap(bitmap, cropRect, scale, rotation + straightenAngle, offset, containerSize)
                    onApplyCrop(croppedBitmap)
                }) {
                    Text("Apply")
                }
            }
        }
    }
}
