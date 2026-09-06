package com.llamachat.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.llamachat.app.R
import com.llamachat.app.utils.FileManager
import com.llamachat.app.utils.ModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModelDownloadActivity : AppCompatActivity() {

    private lateinit var huggingFaceInput: EditText
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var statusText: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        // Initialize views
        huggingFaceInput = findViewById(R.id.huggingface_url)
        downloadButton = findViewById(R.id.download_button)
        progressBar = findViewById(R.id.progress_bar)
        progressText = findViewById(R.id.progress_text)
        statusText = findViewById(R.id.status_text)
        backButton = findViewById(R.id.back_button)

        // Set example URL
        huggingFaceInput.hint = "https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.1-GGUF/resolve/main/mistral-7b-instruct-v0.1.Q4_K_M.gguf"

        // Button listeners
        downloadButton.setOnClickListener { startDownload() }
        backButton.setOnClickListener { finish() }

        statusText.text = "Paste a HuggingFace GGUF model URL and tap Download"
    }

    private fun startDownload() {
        val url = huggingFaceInput.text.toString().trim()
        if (url.isEmpty()) {
            statusText.text = "Please enter a valid URL"
            return
        }

        if (!url.contains(".gguf")) {
            statusText.text = "URL must point to a .gguf file"
            return
        }

        downloadButton.isEnabled = false
        huggingFaceInput.isEnabled = false
        progressBar.visibility = ProgressBar.VISIBLE
        statusText.text = "Starting download..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ModelDownloader.downloadModel(
                    context = this@ModelDownloadActivity,
                    url = url,
                    onProgress = { current, total ->
                        val percent = (current * 100) / total
                        val totalMB = total / (1024 * 1024)
                        val currentMB = current / (1024 * 1024)
                        withContext(Dispatchers.Main) {
                            progressBar.progress = percent.toInt()
                            progressText.text = "Downloading: $currentMB MB / $totalMB MB ($percent%)"
                        }
                    },
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            statusText.text = "Download complete! Returning to chat..."
                            progressBar.visibility = ProgressBar.GONE
                            Thread.sleep(2000)
                            finish()
                        }
                    },
                    onError = { error ->
                        withContext(Dispatchers.Main) {
                            statusText.text = "Error: $error"
                            downloadButton.isEnabled = true
                            huggingFaceInput.isEnabled = true
                            progressBar.visibility = ProgressBar.GONE
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Error: ${e.message}"
                    downloadButton.isEnabled = true
                    huggingFaceInput.isEnabled = true
                    progressBar.visibility = ProgressBar.GONE
                }
            }
        }
    }
}
