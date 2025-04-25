package com.core.gscore.utils.extensions

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException

fun AssetManager.readTextAsset(fileName: String): String? {
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

fun AssetManager.getBitmapFromAsset(fileName: String): Bitmap? {
    return try {
        // Open the asset as an InputStream
        open(fileName).use { inputStream ->
            // Decode the InputStream into a Bitmap
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: IOException) {
        // Handle exceptions (e.g., file not found)
        e.printStackTrace()
        null
    }
}
