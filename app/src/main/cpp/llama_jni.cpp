#include <jni.h>
#include <cstring>
#include <vector>
#include <sstream>
#include "llama.h"

struct LlamaContext {
    llama_context* ctx;
    llama_model* model;
    llama_sampler* smpl;
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
        llama_model* model = llama_model_load_from_file(model_path, model_params);
        
        if (!model) {
            env->ReleaseStringUTFChars(model_path_j, model_path);
            return 0;
        }

        // Create context
        llama_context_params ctx_params = llama_context_default_params();
        ctx_params.n_ctx = 2048;   // Context size
        ctx_params.n_threads = 4;  // Use 4 threads
        ctx_params.n_threads_batch = 4;
        
        llama_context* ctx = llama_init_from_model(model, ctx_params);
        
        if (!ctx) {
            llama_model_free(model);
            env->ReleaseStringUTFChars(model_path_j, model_path);
            return 0;
        }

        // Create a default sampler chain (will be reconfigured per-inference)
        auto sparams = llama_sampler_chain_default_params();
        llama_sampler* smpl = llama_sampler_chain_init(sparams);
        llama_sampler_chain_add(smpl, llama_sampler_init_dist(0));

        // Create context wrapper and return handle
        LlamaContext* wrapper = new LlamaContext{ctx, model, smpl};
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
        if (wrapper->smpl) {
            llama_sampler_free(wrapper->smpl);
        }
        if (wrapper->ctx) {
            llama_free(wrapper->ctx);
        }
        if (wrapper->model) {
            llama_model_free(wrapper->model);
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
        // Get the vocabulary from the model
        const llama_vocab* vocab = llama_model_get_vocab(wrapper->model);

        // Tokenize prompt using the new 7-arg API
        // First call with nullptr to get the required token count
        int n_tokens = llama_tokenize(vocab, prompt_str, strlen(prompt_str), nullptr, 0, true, true);
        // n_tokens is negative, indicating the required buffer size
        std::vector<llama_token> tokens(std::abs(n_tokens));
        n_tokens = llama_tokenize(vocab, prompt_str, strlen(prompt_str), tokens.data(), tokens.size(), true, true);
        if (n_tokens < 0) {
            env->ReleaseStringUTFChars(prompt_j, prompt_str);
            return false;
        }
        tokens.resize(n_tokens);

        // Clear KV cache
        llama_kv_self_clear(wrapper->ctx);

        // Evaluate initial tokens in batches
        for (int i = 0; i < (int)tokens.size(); i += 32) {
            int n_eval = std::min(32, (int)tokens.size() - i);
            if (llama_decode(wrapper->ctx, llama_batch_get_one(&tokens[i], n_eval)) != 0) {
                env->ReleaseStringUTFChars(prompt_j, prompt_str);
                return false;
            }
        }

        // Reconfigure the sampler chain with the requested parameters
        llama_sampler_free(wrapper->smpl);
        auto sparams = llama_sampler_chain_default_params();
        wrapper->smpl = llama_sampler_chain_init(sparams);
        if (temperature > 0.0f) {
            llama_sampler_chain_add(wrapper->smpl, llama_sampler_init_top_p(top_p, 1));
            llama_sampler_chain_add(wrapper->smpl, llama_sampler_init_temp(temperature));
            llama_sampler_chain_add(wrapper->smpl, llama_sampler_init_dist(0));
        } else {
            llama_sampler_chain_add(wrapper->smpl, llama_sampler_init_greedy());
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
            // Sample next token using the sampler chain
            llama_token next_token = llama_sampler_sample(wrapper->smpl, wrapper->ctx, -1);

            // Check for end of generation
            if (llama_vocab_is_eog(vocab, next_token)) {
                break;
            }

            // Convert token to string
            char token_str[128];
            int n = llama_token_to_piece(vocab, next_token, token_str, sizeof(token_str), 0, true);
            
            if (n > 0) {
                // Call Java callback with token
                jstring token_java = env->NewStringUTF(std::string(token_str, n).c_str());
                env->CallVoidMethod(token_callback, on_token_method, token_java);
                env->DeleteLocalRef(token_java);
            }

            // Prepare for next token
            if (llama_decode(wrapper->ctx, llama_batch_get_one(&next_token, 1)) != 0) {
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
