package com.moshitech.me.advancedphotoeditingtool.ui.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

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

fun updateCropRect(
    rect: Rect?,
    dragAmount: Offset,
    handle: CropHandle,
    aspectRatio: AspectRatio,
    containerSize: IntSize
): Rect? {
    if (rect == null) return null

    var newLeft = rect.left
    var newTop = rect.top
    var newRight = rect.right
    var newBottom = rect.bottom

    when (handle) {
        CropHandle.TOP_LEFT -> {
            newLeft += dragAmount.x
            newTop += dragAmount.y
        }
        CropHandle.TOP_RIGHT -> {
            newRight += dragAmount.x
            newTop += dragAmount.y
        }
        CropHandle.BOTTOM_LEFT -> {
            newLeft += dragAmount.x
            newBottom += dragAmount.y
        }
        CropHandle.BOTTOM_RIGHT -> {
            newRight += dragAmount.x
            newBottom += dragAmount.y
        }
        CropHandle.LEFT -> newLeft += dragAmount.x
        CropHandle.TOP -> newTop += dragAmount.y
        CropHandle.RIGHT -> newRight += dragAmount.x
        CropHandle.BOTTOM -> newBottom += dragAmount.y
    }

    var newRect = Rect(newLeft, newTop, newRight, newBottom)

    if (aspectRatio != AspectRatio.FREE) {
        val targetAspectRatio = aspectRatio.width / aspectRatio.height

        val fixedPoint = when (handle) {
            CropHandle.TOP_LEFT -> newRect.bottomRight
            CropHandle.TOP_RIGHT -> newRect.bottomLeft
            CropHandle.BOTTOM_LEFT -> newRect.topRight
            CropHandle.BOTTOM_RIGHT -> newRect.topLeft
            CropHandle.LEFT -> newRect.centerRight
            CropHandle.TOP -> newRect.bottomCenter
            CropHandle.RIGHT -> newRect.centerLeft
            CropHandle.BOTTOM -> newRect.topCenter
        }

        var newWidth = newRect.width
        var newHeight = newRect.height

        // Prioritize maintaining aspect ratio based on the larger change or specific handle
        if (handle == CropHandle.LEFT || handle == CropHandle.RIGHT ||
            handle == CropHandle.TOP_LEFT || handle == CropHandle.TOP_RIGHT ||
            handle == CropHandle.BOTTOM_LEFT || handle == CropHandle.BOTTOM_RIGHT
        ) {
            // Adjust height based on new width
            newHeight = newWidth / targetAspectRatio
        } else if (handle == CropHandle.TOP || handle == CropHandle.BOTTOM) {
            // Adjust width based on new height
            newWidth = newHeight * targetAspectRatio
        }

        // Reconstruct newRect based on fixed point and calculated dimensions
        newRect = when (handle) {
            CropHandle.TOP_LEFT -> Rect(fixedPoint.x - newWidth, fixedPoint.y - newHeight, fixedPoint.x, fixedPoint.y)
            CropHandle.TOP_RIGHT -> Rect(fixedPoint.x, fixedPoint.y - newHeight, fixedPoint.x + newWidth, fixedPoint.y)
            CropHandle.BOTTOM_LEFT -> Rect(fixedPoint.x - newWidth, fixedPoint.y, fixedPoint.x, fixedPoint.y + newHeight)
            CropHandle.BOTTOM_RIGHT -> Rect(fixedPoint.x, fixedPoint.y, fixedPoint.x + newWidth, fixedPoint.y + newHeight)
            CropHandle.LEFT -> Rect(fixedPoint.x - newWidth, newRect.top, fixedPoint.x, newRect.bottom)
            CropHandle.TOP -> Rect(newRect.left, fixedPoint.y - newHeight, newRect.right, fixedPoint.y)
            CropHandle.RIGHT -> Rect(fixedPoint.x, newRect.top, fixedPoint.x + newWidth, newRect.bottom)
            CropHandle.BOTTOM -> Rect(newRect.left, fixedPoint.y, newRect.right, fixedPoint.y + newHeight)
        }
    }

    // Ensure cropRect stays within container bounds and has minimum size
    val minCropSize = 50f // pixels
    val constrainedRect = Rect(
        left = newRect.left.coerceIn(0f, containerSize.width.toFloat() - minCropSize),
        top = newRect.top.coerceIn(0f, containerSize.height.toFloat() - minCropSize),
        right = newRect.right.coerceIn(minCropSize, containerSize.width.toFloat()),
        bottom = newRect.bottom.coerceIn(minCropSize, containerSize.height.toFloat())
    )

    return if (constrainedRect.width >= minCropSize && constrainedRect.height >= minCropSize) {
        constrainedRect
    } else {
        rect // Revert to old rect if new one is too small
    }
}