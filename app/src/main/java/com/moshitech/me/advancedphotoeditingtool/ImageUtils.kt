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
    cropRect: Rect,
    scale: Float,
    rotation: Float,
    offset: Offset
): Bitmap {
    val matrix = Matrix()
    matrix.postScale(scale, scale)
    matrix.postRotate(rotation)
    matrix.postTranslate(offset.x, offset.y)

    val inverseMatrix = Matrix()
    matrix.invert(inverseMatrix)

    val mappedPoints = floatArrayOf(
        cropRect.left, cropRect.top,
        cropRect.right, cropRect.top,
        cropRect.right, cropRect.bottom,
        cropRect.left, cropRect.bottom
    )
    inverseMatrix.mapPoints(mappedPoints)

    val croppedLeft = mappedPoints[0].coerceAtLeast(0f).toInt()
    val croppedTop = mappedPoints[1].coerceAtLeast(0f).toInt()
    val croppedRight = mappedPoints[4].coerceAtMost(originalBitmap.width.toFloat()).toInt()
    val croppedBottom = mappedPoints[5].coerceAtMost(originalBitmap.height.toFloat()).toInt()

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
