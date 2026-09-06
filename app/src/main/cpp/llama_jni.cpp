#include <jni.h>
#include <cstring>
#include <vector>
#include <sstream>
#include "../../../../../../../llama.cpp/llama.h"

struct LlamaContext {
    llama_context* ctx;
    llama_model* model;
};

extern "C" {

/**
 * Load a model from file
 * Returns a handle (cast from LlamaContext*) or 0 on failure
 */
JNIEXPORT jlong JNICALL
Java_com_llamachat_app_ml_LlamaModel_00024Companion_loadModel(
    JNIEnv* env,
    jobject /* this */,
    jstring model_path_j
) {
    const char* model_path = env->GetStringUTFChars(model_path_j, nullptr);
    
    try {
        // Initialize llama backend
        llama_backend_init();

        // Load model
        llama_model_params model_params = llama_model_default_params();
        llama_model* model = llama_load_model_from_file(model_path, model_params);
        
        if (!model) {
            env->ReleaseStringUTFChars(model_path_j, model_path);
            return 0;
        }

        // Create context
        llama_context_params ctx_params = llama_context_default_params();
        ctx_params.n_ctx = 2048;   // Context size
        ctx_params.n_threads = 4;  // Use 4 threads
        ctx_params.n_threads_batch = 4;
        
        llama_context* ctx = llama_new_context_with_model(model, ctx_params);
        
        if (!ctx) {
            llama_free_model(model);
            env->ReleaseStringUTFChars(model_path_j, model_path);
            return 0;
        }

        // Create context wrapper and return handle
        LlamaContext* wrapper = new LlamaContext{ctx, model};
        env->ReleaseStringUTFChars(model_path_j, model_path);
        return reinterpret_cast<jlong>(wrapper);
    } catch (const std::exception& e) {
        env->ReleaseStringUTFChars(model_path_j, model_path);
        return 0;
    }
}

/**
 * Unload model and free resources
 */
JNIEXPORT void JNICALL
Java_com_llamachat_app_ml_LlamaModel_00024Companion_unloadModel(
    JNIEnv* env,
    jobject /* this */,
    jlong native_handle
) {
    LlamaContext* wrapper = reinterpret_cast<LlamaContext*>(native_handle);
    if (wrapper) {
        if (wrapper->ctx) {
            llama_free(wrapper->ctx);
        }
        if (wrapper->model) {
            llama_free_model(wrapper->model);
        }
        delete wrapper;
        llama_backend_free();
    }
}

/**
 * Run inference and generate text
 */
JNIEXPORT jboolean JNICALL
Java_com_llamachat_app_ml_LlamaModel_00024Companion_nativeInference(
    JNIEnv* env,
    jobject /* this */,
    jlong native_handle,
    jstring prompt_j,
    jint max_tokens,
    jfloat temperature,
    jfloat top_p,
    jobject token_callback
) {
    LlamaContext* wrapper = reinterpret_cast<LlamaContext*>(native_handle);
    if (!wrapper || !wrapper->ctx) {
        return false;
    }

    const char* prompt_str = env->GetStringUTFChars(prompt_j, nullptr);
    
    try {
        // Tokenize prompt
        std::vector<llama_token> tokens = llama_tokenize(
            wrapper->model,
            prompt_str,
            true
        );

        // Clear context
        llama_kv_cache_clear(wrapper->ctx);

        // Evaluate initial tokens
        for (int i = 0; i < (int)tokens.size(); i += 32) {
            int n_eval = std::min(32, (int)tokens.size() - i);
            if (llama_decode(wrapper->ctx, llama_batch_get_one(&tokens[i], n_eval, 0, 0)) != 0) {
                env->ReleaseStringUTFChars(prompt_j, prompt_str);
                return false;
            }
        }

        // Get callback method
        jclass callback_class = env->GetObjectClass(token_callback);
        jmethodID on_token_method = env->GetMethodID(
            callback_class,
            "invoke",
            "(Ljava/lang/Object;)V"
        );

        // Generate new tokens
        for (int i = 0; i < max_tokens; i++) {
            // Sample next token
            llama_token next_token = llama_sampler_sample_and_accept(
                nullptr,
                wrapper->ctx,
                nullptr
            );

            if (next_token == llama_token_eos(wrapper->model)) {
                break;
            }

            // Convert token to string
            char token_str[128];
            int n = llama_token_to_piece(wrapper->model, next_token, token_str, sizeof(token_str), 0, true);
            
            if (n > 0) {
                // Call Java callback with token
                jstring token_java = env->NewStringUTF(std::string(token_str, n).c_str());
                env->CallVoidMethod(token_callback, on_token_method, token_java);
                env->DeleteLocalRef(token_java);
            }

            // Prepare for next token
            if (llama_decode(wrapper->ctx, llama_batch_get_one(&next_token, 1, tokens.size() + i, 0)) != 0) {
                break;
            }
        }

        env->ReleaseStringUTFChars(prompt_j, prompt_str);
        return true;
    } catch (const std::exception& e) {
        env->ReleaseStringUTFChars(prompt_j, prompt_str);
        return false;
    }
}

} // extern "C"
