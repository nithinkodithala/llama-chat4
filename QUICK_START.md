# ⚡ Quick Start - Build in 3 Hours

Follow these exact steps to build and run your Llama Chat app.

## Prerequisites (30 minutes)

### 1. Download Android Studio
- Go to: https://developer.android.com/studio
- Click "Download Android Studio"
- Run the installer
- Wait for first launch (5-10 minutes)

### 2. Install Required Tools (in Android Studio)
```
Tools → SDK Manager

SDK Platforms tab:
  ✓ Android 14 (latest)
  ✓ Android 13
  ✓ Android 9+

SDK Tools tab:
  ✓ Android SDK Build-Tools 34.0.0
  ✓ Android NDK 26.1+ ← IMPORTANT!
  ✓ CMake 3.22.1+

Click "Apply" and wait for downloads (5-10 minutes)
```

### 3. Download llama.cpp
In your terminal:
```bash
cd LlamaChatApp
git clone https://github.com/ggml-org/llama.cpp.git
```

(If git not installed, download ZIP from https://github.com/ggml-org/llama.cpp and extract)

## Build the App (45 minutes)

### Step 1: Open in Android Studio
```
File → Open
Select: LlamaChatApp folder
Click: Open
```

### Step 2: Wait for Gradle Sync
Android Studio shows progress at bottom:
```
"Gradle build in progress..."
(Wait 5-10 minutes)
"Gradle build finished"
```

### Step 3: Build the Project
```
Build → Make Project
(Wait 20-30 minutes for compilation)
(Shows "BUILD SUCCESSFUL")
```

### Step 4: Create Release APK
```
Build → Generate Signed Bundle / APK

Select: APK
Click: Next

Click: Create new... (keystore)

Fill in form:
  Key store path: ~/my-key.jks
  Password: (create strong password)
  Key alias: mykey
  Validity: 25 years
  Certificate info: (your info)
  
Click: OK
Click: Next

Select: Release
Click: Finish

Wait 5 minutes for signing...
"APK(s) generated successfully"
```

### Step 5: Find Your APK
```
app/build/outputs/apk/release/app-release.apk

This is your final app! (~45-50 MB)
```

## Install on Phone (10 minutes)

### Via USB (Recommended)

```bash
# Connect phone via USB
# Enable USB Debugging on phone:
#   Settings → About Phone → tap Build Number 7 times
#   Back to Settings → Developer Options → USB Debugging ✓

adb install app/build/outputs/apk/release/app-release.apk

# Wait for "Success"
# App appears in app drawer!
```

### Via Direct Install

1. Copy `app-release.apk` to your phone (Google Drive, email, etc)
2. Open file manager on phone
3. Tap the APK file
4. Tap "Install"
5. Tap "Open" when done

## First Use (5 minutes)

### 1. Open the App
- Tap "Llama Chat" in app drawer

### 2. Download a Model
- Tap "Download Model" button
- Paste this URL:
  ```
  https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.1-GGUF/resolve/main/mistral-7b-instruct-v0.1.Q4_K_M.gguf
  ```
- Tap "Download"
- Wait ~10-20 minutes (depends on internet, model is ~4GB)

### 3. Start Chatting!
- Type: "Hello, how are you?"
- Tap "Send"
- Watch tokens generate!

## Total Time

| Step | Time |
|------|------|
| Download/Install Android Studio | 15 min |
| Install Build Tools | 15 min |
| Download llama.cpp | 5 min |
| Open & Sync Gradle | 15 min |
| Build App | 30 min |
| Create Release APK | 10 min |
| Install on Phone | 10 min |
| Download Model | 10-20 min |
| **TOTAL** | **~2 hours** |

## What to Do If Something Fails

### "NDK not found"
```
→ Go to Tools → SDK Manager
→ Click SDK Tools tab
→ Install "Android NDK (Side by side)"
→ Close Android Studio
→ Reopen and try building again
```

### "Gradle build failed"
```
→ Build → Clean Project
→ Build → Make Project
→ If still fails:
  - Close Android Studio
  - Delete: .gradle/ folder
  - Reopen project
```

### "Can't install APK"
```bash
→ First uninstall old version:
adb uninstall com.llamachat.app

→ Then install:
adb install app-release.apk
```

### "Model won't download"
```
→ Check internet connection
→ Try a different model URL:
   https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf
→ Try again - download can resume if interrupted
```

### "App crashes on startup"
```
→ Try downloading and installing again
→ Check: Settings → Apps → App permissions → Storage
→ Grant "Allow access to photos, media, and files"
```

## Recommended Models

### Fastest (Most Responsive)
```
Phi-2: https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf
Size: ~2GB | Speed: Very fast | Quality: Good
```

### Balanced (Recommended for Most)
```
Mistral-7B-Q4: https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.1-GGUF/resolve/main/mistral-7b-instruct-v0.1.Q4_K_M.gguf
Size: ~4GB | Speed: Good | Quality: Excellent
```

### Best Quality
```
Llama-2-13B-Q4: https://huggingface.co/TheBloke/Llama-2-13B-Chat-GGUF/resolve/main/llama-2-13b-chat.Q4_K_M.gguf
Size: ~8GB | Speed: Slower | Quality: Very High
```

**TIP:** Start with Phi-2 if you're impatient, switch to Mistral-7B when you want better quality.

## After Building

### Share Your App
```bash
# Upload APK to GitHub
# Share link with friends
# They can install directly!

Or publish to Google Play Store for millions of users
(See GOOGLE_PLAY_PUBLISHING.md for instructions)
```

### Update the App
```bash
# To make changes:
1. Edit code in Android Studio
2. Increase versionCode in app/build.gradle
3. Build → Generate Signed Bundle/APK
4. Use SAME keystore when signing
5. Install new APK

IMPORTANT: Never create new keystore!
          You must use same keystore for updates!
```

### Customize the App
```
Change app name: app/src/main/res/values/strings.xml
Change colors: app/src/main/res/values/colors.xml
Change UI: app/src/main/res/layout/activity_main.xml
```

---

## ✅ Checklist

- [ ] Android Studio installed
- [ ] Build tools installed (NDK, CMake)
- [ ] llama.cpp downloaded
- [ ] Project opens in Android Studio
- [ ] Gradle sync completes
- [ ] Project builds successfully
- [ ] Release APK created
- [ ] APK installed on phone
- [ ] Model downloaded
- [ ] Chat working!

---

## 🎉 You Did It!

You now have a working AI chat app running locally on your Android device!

**Next:** 
- Try different models
- Customize the app
- Share with friends
- Publish to Google Play Store (see README.md)

**Questions?**
- Check README.md for detailed info
- See troubleshooting section above
- Check GitHub: https://github.com/ggml-org/llama.cpp

Enjoy! 🚀
