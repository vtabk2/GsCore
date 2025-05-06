package com.core.gscore.utils.extensions

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import java.io.IOException

private fun AssetManager.readTextAsset(fileName: String): String? {
    return try {
        open(fileName).use { it.bufferedReader().readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun AssetManager.getTextFromAsset(fileName: String): String? {
    return readTextAsset(fileName)
}

fun AssetManager.getBitmapFromAsset(
    fileName: String,
    targetWidth: Int? = null,
    targetHeight: Int? = null,
    inPreferredConfig: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap? {
    return try {
        // Open the asset as an InputStream
        open(fileName).use { inputStream ->
            // Bước 1: Đọc kích thước gốc và tính inSampleSize
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = inPreferredConfig

            // Bước 2: Decode với inSampleSize
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return null

            // Bước 3: Scale (nếu cần)
            when {
                targetWidth != null && targetHeight != null -> scaleBitmap(bitmap, targetWidth, targetHeight)
                targetWidth != null -> scaleToWidth(bitmap, targetWidth)
                targetHeight != null -> scaleToHeight(bitmap, targetHeight)
                else -> bitmap
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

// Hàm scale theo chiều rộng (giữ tỉ lệ)
fun scaleToWidth(bitmap: Bitmap, targetWidth: Int): Bitmap {
    val aspectRatio = bitmap.height.toFloat() / bitmap.width
    val targetHeight = (targetWidth * aspectRatio).toInt()
    return bitmap.scale(targetWidth, targetHeight, false)
}

// Hàm scale theo chiều cao (giữ tỉ lệ)
fun scaleToHeight(bitmap: Bitmap, targetHeight: Int): Bitmap {
    val aspectRatio = bitmap.width.toFloat() / bitmap.height
    val targetWidth = (targetHeight * aspectRatio).toInt()
    return bitmap.scale(targetWidth, targetHeight, false)
}

// Hàm scale chính xác kích thước
fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    return bitmap.scale(width, height, false)
}

// Hàm tính inSampleSize
private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int?,
    reqHeight: Int?
): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (reqWidth != null && reqHeight != null) {
        while (height / inSampleSize > reqHeight || width / inSampleSize > reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}