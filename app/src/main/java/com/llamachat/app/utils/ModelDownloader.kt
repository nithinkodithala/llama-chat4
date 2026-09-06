package com.llamachat.app.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object ModelDownloader {

    private val httpClient = OkHttpClient()

    /**
     * Download model from HuggingFace
     * @param context Android context
     * @param url Direct download URL to .gguf file
     * @param onProgress Callback with (currentBytes, totalBytes)
     * @param onSuccess Callback when download completes
     * @param onError Callback with error message
     */
    suspend fun downloadModel(
        context: Context,
        url: String,
        onProgress: suspend (Long, Long) -> Unit,
        onSuccess: suspend () -> Unit,
        onError: suspend (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // Check available space
            val availableSpace = FileManager.getAvailableSpace(context)
            if (availableSpace < 1024 * 1024 * 1024) { // Less than 1GB
                onError("Not enough storage space. Need at least 1GB free")
                return@withContext
            }

            // Create request
            val request = Request.Builder()
                .url(url)
                .build()

            // Execute request
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                onError("Download failed: HTTP ${response.code}")
                return@withContext
            }

            val body = response.body ?: run {
                onError("Empty response body")
                return@withContext
            }

            val totalBytes = body.contentLength()
            if (totalBytes <= 0) {
                onError("Cannot determine file size")
                return@withContext
            }

            // Get output file
            val modelFile = FileManager.getModelFile(context)
            val tempFile = File(modelFile.absolutePath + ".tmp")

            // Download with progress
            FileOutputStream(tempFile).use { fileOut ->
                body.byteStream().use { inputStream ->
                    var downloadedBytes = 0L
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fileOut.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        onProgress(downloadedBytes, totalBytes)
                    }
                }
            }

            // Verify download
            if (tempFile.length() < 100_000_000) { // Less than 100MB
                tempFile.delete()
                onError("Downloaded file is too small. Download may be incomplete.")
                return@withContext
            }

            // Move temp file to final location
            tempFile.renameTo(modelFile)
            
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error during download")
        }
    }

    /**
     * Get file size from URL without downloading
     */
    suspend fun getFileSize(url: String): Long = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            
            val response = httpClient.newCall(request).execute()
            return@withContext response.header("Content-Length")?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            return@withContext 0L
        }
    }
}
