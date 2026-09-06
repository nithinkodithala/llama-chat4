package com.llamachat.app.utils

import android.content.Context
import java.io.File

object FileManager {

    private const val MODEL_DIR_NAME = "models"
    private const val MODEL_FILE_NAME = "model.gguf"

    /**
     * Get the directory where models are stored
     */
    fun getModelDirectory(context: Context): File {
        val modelDir = File(context.getExternalFilesDir(null), MODEL_DIR_NAME)
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        return modelDir
    }

    /**
     * Get the current model file
     */
    fun getModelFile(context: Context): File {
        return File(getModelDirectory(context), MODEL_FILE_NAME)
    }

    /**
     * Get available disk space in app directory
     */
    fun getAvailableSpace(context: Context): Long {
        return context.getExternalFilesDir(null)?.freeSpace ?: 0L
    }

    /**
     * Check if model exists and is valid
     */
    fun isModelValid(context: Context): Boolean {
        val modelFile = getModelFile(context)
        // Check if file exists and is at least 100MB (minimum GGUF size)
        return modelFile.exists() && modelFile.length() > 100_000_000L
    }

    /**
     * Delete current model
     */
    fun deleteModel(context: Context) {
        val modelFile = getModelFile(context)
        if (modelFile.exists()) {
            modelFile.delete()
        }
    }

    /**
     * Get formatted size string
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
