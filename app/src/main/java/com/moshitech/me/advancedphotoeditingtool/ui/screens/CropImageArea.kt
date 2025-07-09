package com.moshitech.me.advancedphotoeditingtool.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.moshitech.me.advancedphotoeditingtool.cropBitmap
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Size
import com.moshitech.me.advancedphotoeditingtool.ui.screens.CropHandle
import com.moshitech.me.advancedphotoeditingtool.ui.screens.getCropHandle
import com.moshitech.me.advancedphotoeditingtool.ui.screens.updateCropRect

@Composable
fun CropImageArea(
    bitmap: Bitmap,
    cropRect: Rect,
    scale: Float,
    rotation: Float,
    offset: Offset,
    straightenAngle: Float,
    containerSize: IntSize,
    onCropRectChange: (Rect) -> Unit,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onContainerSizeChange: (IntSize) -> Unit,
    aspectRatio: AspectRatio,
    showGoldenRatioGuide: Boolean,
    showDiagonalGuide: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { onContainerSizeChange(it) }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, gestureRotation ->
                    onScaleChange(scale * zoom)
                    onRotationChange(rotation + gestureRotation)
                    onOffsetChange(offset + pan)
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

        var draggingHandle by remember { mutableStateOf<Int?>(null) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Faded outside area
            drawRect(color = Color.Black.copy(alpha = 0.5f), size = size)
            drawRect(
                color = Color.Transparent,
                topLeft = cropRect.topLeft,
                size = cropRect.size,
                blendMode = androidx.compose.ui.graphics.BlendMode.SrcOut
            )

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

                drawLine(
                    color = Color.Yellow,
                    start = Offset(cropRect.left + h1, cropRect.top),
                    end = Offset(cropRect.left + h1, cropRect.bottom),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.Yellow,
                    start = Offset(cropRect.left + h2, cropRect.top),
                    end = Offset(cropRect.left + h2, cropRect.bottom),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.Yellow,
                    start = Offset(cropRect.left, cropRect.top + v1),
                    end = Offset(cropRect.right, cropRect.top + v1),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.Yellow,
                    start = Offset(cropRect.left, cropRect.top + v2),
                    end = Offset(cropRect.right, cropRect.top + v2),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (showDiagonalGuide) {
                // Implement Diagonal guides
                drawLine(
                    color = Color.Cyan,
                    start = cropRect.topLeft,
                    end = cropRect.bottomRight,
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.Cyan,
                    start = cropRect.topRight,
                    end = cropRect.bottomLeft,
                    strokeWidth = 1.dp.toPx()
                )
            }

            val handleSize = 24.dp.toPx()

            // Draw corner handles
            drawRect(
                color = if (draggingHandle == CropHandle.TOP_LEFT.ordinal) Color.Red else Color.Blue,
                topLeft = cropRect.topLeft - Offset(handleSize / 2, handleSize / 2),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = if (draggingHandle == CropHandle.TOP_RIGHT.ordinal) Color.Red else Color.Blue,
                topLeft = cropRect.topRight - Offset(handleSize / 2, handleSize / 2),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = if (draggingHandle == CropHandle.BOTTOM_LEFT.ordinal) Color.Red else Color.Blue,
                topLeft = cropRect.bottomLeft - Offset(handleSize / 2, handleSize / 2),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = if (draggingHandle == CropHandle.BOTTOM_RIGHT.ordinal) Color.Red else Color.Blue,
                topLeft = cropRect.bottomRight - Offset(handleSize / 2, handleSize / 2),
                size = Size(handleSize, handleSize)
            )

            // Draw edge handles
            drawRect(
                color = if (draggingHandle == CropHandle.LEFT.ordinal) Color.Red else Color.Blue,
                topLeft = cropRect.centerLeft - Offset(handleSize / 2, handleSize / 2),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = if (draggingHandle == CropHandle.TOP.ordinal) Color.Red else Color.Blue,
                topLeft = cropRect.topCenter - Offset(handleSize / 2, handleSize / 2),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = if (draggingHandle == CropHandle.RIGHT.ordinal) Color.Red else Color.Blue,
                topLeft = cropRect.centerRight - Offset(handleSize / 2, handleSize / 2),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = if (draggingHandle == CropHandle.BOTTOM.ordinal) Color.Red else Color.Blue,
                topLeft = cropRect.bottomCenter - Offset(handleSize / 2, handleSize / 2),
                size = Size(handleSize, handleSize)
            )
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
                    ) { change, dragAmount ->
                        change.consume()
                        if (draggingHandle != null) {
                            onCropRectChange(
                                updateCropRect(
                                    cropRect,
                                    dragAmount,
                                    CropHandle.values()[draggingHandle!!],
                                    aspectRatio,
                                    containerSize
                                ) ?: cropRect
                            )
                        } else {
                            onOffsetChange(offset + dragAmount)
                        }
                    }
                }
        )
    }
}