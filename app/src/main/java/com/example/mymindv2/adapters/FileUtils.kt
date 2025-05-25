package com.example.mymindv2.adapters

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    fun getFileFromUri(context: Context, uri: Uri, fileName: String = "temp_image.jpg"): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)

            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
