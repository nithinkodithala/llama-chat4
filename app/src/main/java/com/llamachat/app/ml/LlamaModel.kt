package com.llamachat.app.ml

/**
 * JNI Interface to llama.cpp library
 * Handles model loading and text generation
 */
class LlamaModel(modelPath: String) {
    
    private var nativeHandle: Long = 0

    init {
        nativeHandle = loadModel(modelPath)
        if (nativeHandle == 0L) {
            throw RuntimeException("Failed to load model from $modelPath")
        }
    }

    /**
     * Generate text from a prompt
     * @param prompt The input prompt
     * @param maxTokens Maximum tokens to generate
     * @param temperature Sampling temperature (0.0-2.0)
     * @param topP Top-P sampling parameter
     * @param onToken Callback called for each generated token
     * @return The complete generated text
     */
    fun generate(
        prompt: String,
        maxTokens: Int = 256,
        temperature: Float = 0.7f,
        topP: Float = 0.9f,
        onToken: (String) -> Unit = {}
    ): String {
        if (nativeHandle == 0L) {
            throw RuntimeException("Model not loaded")
        }
        
        val tokens = mutableListOf<String>()
        
        // Call native inference function
        val success = nativeInference(
            nativeHandle,
            prompt,
            maxTokens,
            temperature,
            topP,
            { token ->
                tokens.add(token)
                onToken(token)
            }
        )
        
        if (!success) {
            throw RuntimeException("Inference failed")
        }
        
        return tokens.joinToString("")
    }

    /**
     * Release native resources
     */
    fun release() {
        if (nativeHandle != 0L) {
            unloadModel(nativeHandle)
            nativeHandle = 0L
        }
    }

    companion object {
        init {
            try {
                System.loadLibrary("llama")
            } catch (e: UnsatisfiedLinkError) {
                throw RuntimeException("Failed to load native llama library", e)
            }
        }

        /**
         * Load model from file
         * @return Native handle (0 if failed)
         */
        @JvmStatic
        private external fun loadModel(modelPath: String): Long

        /**
         * Unload model and free resources
         */
        @JvmStatic
        private external fun unloadModel(nativeHandle: Long)

        /**
         * Run inference on the model
         * @param tokenCallback Called for each generated token
         * @return true if successful, false otherwise
         */
        @JvmStatic
        private external fun nativeInference(
            nativeHandle: Long,
            prompt: String,
            maxTokens: Int,
            temperature: Float,
            topP: Float,
            tokenCallback: (String) -> Unit
        ): Boolean
    }
}
