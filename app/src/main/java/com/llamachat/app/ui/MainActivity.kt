package com.llamachat.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.llamachat.app.R
import com.llamachat.app.ml.LlamaModel
import com.llamachat.app.utils.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var chatDisplay: TextView
    private lateinit var userInput: EditText
    private lateinit var sendButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var statusText: TextView
    private lateinit var downloadButton: Button

    private var llamaModel: LlamaModel? = null
    private var isGenerating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        chatDisplay = findViewById(R.id.chat_display)
        userInput = findViewById(R.id.user_input)
        sendButton = findViewById(R.id.send_button)
        progressBar = findViewById(R.id.progress_bar)
        scrollView = findViewById(R.id.scroll_view)
        statusText = findViewById(R.id.status_text)
        downloadButton = findViewById(R.id.download_model_button)

        // Set scrolling for chat display
        chatDisplay.movementMethod = ScrollingMovementMethod()

        // Button listeners
        sendButton.setOnClickListener { sendMessage() }
        downloadButton.setOnClickListener { startModelDownload() }

        // Check if model exists
        checkModelStatus()
    }

    private fun checkModelStatus() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val modelFile = FileManager.getModelFile(this@MainActivity)
                withContext(Dispatchers.Main) {
                    if (modelFile.exists() && modelFile.length() > 100_000_000) {
                        // Model exists and is at least 100MB
                        statusText.text = "Model ready. Initializing..."
                        initializeModel(modelFile)
                    } else {
                        statusText.text = "Model not found. Download a model to start."
                        sendButton.isEnabled = false
                        userInput.isEnabled = false
                    }
                }
            }
        }
    }

    private fun initializeModel(modelFile: File) {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                llamaModel = LlamaModel(modelFile.absolutePath)
                withContext(Dispatchers.Main) {
                    statusText.text = "Ready to chat! (${modelFile.length() / 1_000_000_000}GB model)"
                    sendButton.isEnabled = true
                    userInput.isEnabled = true
                    userInput.requestFocus()
                    chatDisplay.text = "Welcome to Llama Chat!\n\nEnter your prompt and press Send.\n\n"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Error loading model: ${e.message}"
                    chatDisplay.text = "Error: ${e.localizedMessage}"
                }
            }
        }
    }

    private fun startModelDownload() {
        startActivity(Intent(this, ModelDownloadActivity::class.java))
    }

    private fun sendMessage() {
        if (isGenerating || llamaModel == null) return

        val prompt = userInput.text.toString().trim()
        if (prompt.isEmpty()) return

        // Add user message to chat
        val currentText = chatDisplay.text.toString()
        chatDisplay.text = "$currentText\nYou: $prompt\n"
        userInput.text.clear()

        // Scroll to bottom
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }

        isGenerating = true
        sendButton.isEnabled = false
        progressBar.visibility = ProgressBar.VISIBLE
        statusText.text = "Generating response..."

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val response = llamaModel!!.generate(
                    prompt = prompt,
                    maxTokens = 256,
                    temperature = 0.7f,
                    topP = 0.9f,
                    onToken = { token ->
                        // Called for each generated token
                        lifecycleScope.launch(Dispatchers.Main) {
                            val currentChat = chatDisplay.text.toString()
                            chatDisplay.text = currentChat + token
                            scrollView.post {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                            }
                        }
                    }
                )

                withContext(Dispatchers.Main) {
                    val finalText = chatDisplay.text.toString()
                    chatDisplay.text = "$finalText\n\n"
                    scrollView.post {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorText = chatDisplay.text.toString()
                    chatDisplay.text = "$errorText\n[Error: ${e.message}]\n\n"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isGenerating = false
                    sendButton.isEnabled = true
                    progressBar.visibility = ProgressBar.GONE
                    statusText.text = "Ready"
                    userInput.requestFocus()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        llamaModel?.release()
    }
}
