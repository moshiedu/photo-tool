package com.moshitech.me.advancedphotoeditingtool

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

fun uriToBitmap(uri: Uri, context: Context): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }
}

fun applyTransformations(bitmap: Bitmap, rotation: Int, flipX: Boolean, flipY: Boolean, straightenAngle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotation.toFloat() + straightenAngle)
    matrix.postScale(if (flipX) -1f else 1f, if (flipY) -1f else 1f, bitmap.width / 2f, bitmap.height / 2f)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun cropBitmap(
    originalBitmap: Bitmap,
    cropRect: Rect, // This is in the container's coordinate system
    scale: Float,
    rotation: Float,
    offset: Offset,
    containerSize: IntSize // Add containerSize here
): Bitmap {
    val originalWidth = originalBitmap.width.toFloat()
    val originalHeight = originalBitmap.height.toFloat()

    // Calculate the scale factor from original bitmap to the initially displayed size (ContentScale.Fit)
    val scaleXFit = containerSize.width / originalWidth
    val scaleYFit = containerSize.height / originalHeight
    val initialScale = minOf(scaleXFit, scaleYFit)

    // Calculate the size of the bitmap after initial ContentScale.Fit
    val scaledBitmapWidth = originalWidth * initialScale
    val scaledBitmapHeight = originalHeight * initialScale

    // Calculate the top-left offset of the initially scaled and centered bitmap within the container
    val initialOffsetX = (containerSize.width - scaledBitmapWidth) / 2f
    val initialOffsetY = (containerSize.height - scaledBitmapHeight) / 2f

    // Create a matrix to represent the full transformation from original bitmap coordinates
    // to the coordinates within the container after all transformations (initial fit + user).
    val transformMatrix = Matrix()

    // 1. Apply initial scaling (ContentScale.Fit)
    transformMatrix.postScale(initialScale, initialScale)

    // 2. Apply initial translation to center the fitted bitmap
    transformMatrix.postTranslate(initialOffsetX, initialOffsetY)

    // 3. Apply user-defined scale (around the center of the *initially scaled* bitmap)
    //    Need to translate to center, apply scale, then translate back
    transformMatrix.postTranslate(-containerSize.width / 2f, -containerSize.height / 2f)
    transformMatrix.postScale(scale, scale)
    transformMatrix.postTranslate(containerSize.width / 2f, containerSize.height / 2f)

    // 4. Apply user-defined rotation (around the center of the *initially scaled and user-scaled* bitmap)
    //    Need to translate to center, apply rotation, then translate back
    transformMatrix.postTranslate(-containerSize.width / 2f, -containerSize.height / 2f)
    transformMatrix.postRotate(rotation)
    transformMatrix.postTranslate(containerSize.width / 2f, containerSize.height / 2f)

    // 5. Apply user-defined offset (translation of the top-left corner)
    transformMatrix.postTranslate(offset.x, offset.y)

    // Now, invert this matrix to map points from container coordinates (cropRect)
    // back to original bitmap coordinates.
    val inverseTransformMatrix = Matrix()
    transformMatrix.invert(inverseTransformMatrix)

    // Map the cropRect corners using the inverse matrix
    val mappedPoints = floatArrayOf(
        cropRect.left, cropRect.top,
        cropRect.right, cropRect.top,
        cropRect.right, cropRect.bottom,
        cropRect.left, cropRect.bottom
    )
    inverseTransformMatrix.mapPoints(mappedPoints)

    // Find the min/max x and y to get the bounding box of the transformed crop rectangle
    val minX = minOf(mappedPoints[0], mappedPoints[2], mappedPoints[4], mappedPoints[6])
    val minY = minOf(mappedPoints[1], mappedPoints[3], mappedPoints[5], mappedPoints[7])
    val maxX = maxOf(mappedPoints[0], mappedPoints[2], mappedPoints[4], mappedPoints[6])
    val maxY = maxOf(mappedPoints[1], mappedPoints[3], mappedPoints[5], mappedPoints[7])

    // Ensure the cropped area is within the original bitmap bounds
    val croppedLeft = minX.coerceIn(0f, originalWidth).roundToInt()
    val croppedTop = minY.coerceIn(0f, originalHeight).roundToInt()
    val croppedRight = maxX.coerceIn(0f, originalWidth).roundToInt()
    val croppedBottom = maxY.coerceIn(0f, originalHeight).roundToInt()

    val croppedWidth = croppedRight - croppedLeft
    val croppedHeight = croppedBottom - croppedTop

    if (croppedWidth <= 0 || croppedHeight <= 0) {
        return originalBitmap // Return original if crop is invalid
    }

    return Bitmap.createBitmap(
        originalBitmap,
        croppedLeft,
        croppedTop,
        croppedWidth,
        croppedHeight
    )
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String) {
    val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    var imageUri: Uri? = null

    try {
        imageUri = resolver.insert(imageCollection, contentValues)
        imageUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw Exception("Couldn't save bitmap")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        imageUri?.let { resolver.delete(it, null, null) }
        Toast.makeText(context, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun createImageUri(context: Context): Uri {
    val photoFile = File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context,
        "${context.packageName}.provider", photoFile)
}
