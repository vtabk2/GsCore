package com.core.gscore.utils.extensions

import android.content.res.AssetManager
import kotlin.io.bufferedReader
import kotlin.io.readText
import kotlin.io.use

fun AssetManager.readTextAsset(fileName: String): String? {
    return try {
        open(fileName).use { it.bufferedReader().readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}