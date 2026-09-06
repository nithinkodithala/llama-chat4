# Llama Chat - Android App

A simple, fast AI chat app that runs llama.cpp models locally on your Android device.

## Features

✅ **Local Inference** - Run models entirely on your device, no internet needed  
✅ **Simple Chat Interface** - Clean, minimal UI for chatting with AI  
✅ **HuggingFace Integration** - Download any GGUF model from HuggingFace  
✅ **Streaming Generation** - See tokens generated in real-time  
✅ **Customizable** - Adjust temperature, top-P, and context size  
✅ **Private** - Your data never leaves your phone  

## Requirements

- **Android Device:** Android 9+ (API 28+)
- **RAM:** 4GB minimum (6GB+ recommended)
- **Storage:** 5GB+ free space (for model)
- **Computer:** For building the APK

## Building the APK

### Prerequisites

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Run the installer

2. **Install Build Tools**
   - Open Android Studio → Tools → SDK Manager
   - Install:
     - Android SDK 34
     - Android NDK 26.1+
     - CMake 3.22+

3. **Clone llama.cpp**
   ```bash
   cd LlamaChatApp
   git clone https://github.com/ggml-org/llama.cpp.git
   ```

### Build Steps

1. **Open in Android Studio**
   ```bash
   # In Android Studio:
   File → Open → Select this folder
   ```

2. **Gradle Sync**
   - Wait for Gradle to sync (5-10 minutes)
   - You should see "Build Successful"

3. **Build Release APK**
   ```
   Build → Generate Signed Bundle / APK
   Select: APK
   Click: Create new (for keystore)
   Fill in info and create keystore
   Select Release variant
   Click: Finish
   ```

4. **Find Your APK**
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

## Installation

### On Phone via USB

```bash
# Connect phone via USB
adb devices

# Install APK
adb install app/build/outputs/apk/release/app-release.apk
```

### Direct Install

1. Copy `app-release.apk` to your phone
2. Open file manager
3. Tap the APK file
4. Tap "Install"

## First Run

1. **Open the app**
   - Tap "Llama Chat" in your app drawer

2. **Download a model**
   - Tap "Download Model" button
   - Paste a HuggingFace URL (see below)
   - Tap "Download"

3. **Start chatting**
   - Type your message
   - Tap "Send"
   - Watch tokens generate in real-time!

## Recommended Models

These models work great on mobile:

### Fast Models (2GB)
- Phi-2: https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf
- Mistral-7B-Q2: https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.1-GGUF/resolve/main/mistral-7b-instruct-v0.1.Q2_K.gguf

### Balanced Models (4GB)
- Mistral-7B-Q4: https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.1-GGUF/resolve/main/mistral-7b-instruct-v0.1.Q4_K_M.gguf
- Llama-2-7B-Q4: https://huggingface.co/TheBloke/Llama-2-7B-Chat-GGUF/resolve/main/llama-2-7b-chat.Q4_K_M.gguf

### Quality Models (5-6GB)
- Mistral-7B-Q5: https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.1-GGUF/resolve/main/mistral-7b-instruct-v0.1.Q5_K_M.gguf
- Llama-2-13B-Q4: https://huggingface.co/TheBloke/Llama-2-13B-Chat-GGUF/resolve/main/llama-2-13b-chat.Q4_K_M.gguf

**Pro Tip:** Start with Phi-2 or Mistral-Q4 for best balance of speed and quality.

## Project Structure

```
LlamaChatApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/llamachat/app/
│   │   │   │   ├── ui/          (Activities - UI)
│   │   │   │   ├── ml/          (LlamaModel JNI)
│   │   │   │   └── utils/       (Utilities)
│   │   │   ├── cpp/             (C++ JNI binding)
│   │   │   ├── res/             (Resources)
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── build.gradle             (App dependencies)
│   └── CMakeLists.txt           (Native build)
├── build.gradle                 (Project config)
├── settings.gradle              (Gradle settings)
├── llama.cpp/                   (Git submodule)
└── README.md                    (This file)
```

## Architecture

- **Frontend:** Kotlin + Android XML layouts
- **Backend:** llama.cpp C++ library via JNI
- **Model Download:** OkHttp + coroutines
- **Inference:** Native llama.cpp with multi-threaded support

## Key Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Chat interface and user interaction |
| `ModelDownloadActivity.kt` | Model download UI |
| `LlamaModel.kt` | JNI interface to C++ |
| `ModelDownloader.kt` | HuggingFace download handling |
| `FileManager.kt` | File storage management |
| `llama_jni.cpp` | C++ JNI bindings |

## Troubleshooting

### Build Issues

**"NDK not found"**
- Go to Tools → SDK Manager → SDK Tools
- Install NDK 26.1+

**"CMake not found"**
- Go to Tools → SDK Manager → SDK Tools
- Install CMake 3.22+

**"Gradle build failed"**
- Close Android Studio
- Delete: `.gradle/` and `.idea/` folders
- Reopen project

### Runtime Issues

**"Model not loading"**
- Verify model is valid GGUF format
- Check file size > 100MB
- Try downloading again

**"Out of memory"**
- Close other apps
- Use smaller model (Q4 instead of Q5)
- Reduce context size

**"Generation is very slow"**
- Use Q4 quantization, not Q8
- Try smaller model (Phi-2 or Mistral)
- Close background apps

## Performance Tips

### For Maximum Speed
```
Use: Phi-2 Q4 model
Context: 2048 tokens
Max output: 128 tokens
```

### For Better Quality
```
Use: Mistral-7B or Llama-2-13B Q4 model
Context: 4096 tokens
Max output: 256-512 tokens
```

## Customization

### Change App Name
```
File: app/src/main/res/values/strings.xml
Change: <string name="app_name">My AI Chat</string>
```

### Change Color Scheme
```
File: app/src/main/res/values/colors.xml
Modify color definitions and rebuild
```

### Adjust Inference Parameters
```
File: MainActivity.kt
Modify:
- maxTokens: 256 → 512 (generate more)
- temperature: 0.7 → 1.0 (more creative)
- topP: 0.9 → 0.95 (more diverse)
```

## License

- **This app:** MIT License
- **llama.cpp:** MIT License (https://github.com/ggml-org/llama.cpp)
- **Models:** Respective model licenses (check HuggingFace)

## Resources

- **llama.cpp:** https://github.com/ggml-org/llama.cpp
- **HuggingFace:** https://huggingface.co
- **TheBloke:** https://huggingface.co/TheBloke (best GGUF collection)
- **Android Dev:** https://developer.android.com

## Support

- Check troubleshooting section above
- Search GitHub Issues: https://github.com/ggml-org/llama.cpp/issues
- Ask on Reddit: r/androiddev, r/llm

## Contributing

Found a bug? Want to improve? Contributions welcome!

- Report issues
- Submit pull requests
- Improve documentation
- Add features

---

**Happy chatting!** 🚀

Run powerful AI models on your Android device, completely offline.
