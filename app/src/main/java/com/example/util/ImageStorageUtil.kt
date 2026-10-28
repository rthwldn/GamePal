package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageUtil {
    /**
     * Copies selected image from Android Photo Picker (Uri) to the app's internal persistent storage
     * so it never expires and requires no external storage permissions.
     * Returns the absolute file path (e.g. /data/user/0/.../covers/cover_123.jpg).
     */
    fun saveCoverImageFromUri(context: Context, uri: Uri, prefix: String = "cover"): String? {
        return try {
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
            val outputFile = File(coversDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
