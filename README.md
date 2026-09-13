# ગુજરાત પોસ્ટ (Gujarat Post) — Native Android Application

Dedicated Native Android codebase for the **Gujarat Post** Gujarati news portal. Built with **Kotlin**, **Android Gradle**, **Material Design 3**, and **Retrofit 2** connecting to the `backend/` REST API.

---

## 📁 Project Architecture & Directory Structure

```
android/
├── build.gradle                             # Project-level Gradle build configuration
├── settings.gradle                          # Module registration (:app)
├── gradle.properties                        # AndroidX and JVM options
├── gradlew / gradlew.bat                    # Gradle command-line wrappers
└── app/
    ├── build.gradle                         # App module dependencies & SDK versions (minSdk 24, targetSdk 34)
    ├── proguard-rules.pro                   # Release obfuscation & model preservation
    └── src/
        └── main/
            ├── AndroidManifest.xml          # App permissions, activities & launcher declaration
            ├── java/com/gujaratpost/app/
            │   ├── GujaratPostApp.kt        # Application class (System night mode & singleton init)
            │   ├── data/
            │   │   ├── api/
            │   │   │   ├── ApiService.kt    # Retrofit endpoints (/api/public/articles, categories, breaking)
            │   │   │   └── RetrofitClient.kt# OkHttp logging & dynamic base URL configuration
            │   │   └── models/
            │   │       ├── Article.kt       # News Article & pagination data models
            │   │       ├── Category.kt      # News Category with multilingual helpers
            │   │       └── ApiResponse.kt   # Standard backend JSON response wrapper
            │   ├── ui/
            │   │   ├── MainActivity.kt      # Primary host activity with Bottom Navigation
            │   │   ├── home/
            │   │   │   ├── HomeFragment.kt  # Breaking banner, category chips, and live news feed
            │   │   │   └── ArticleAdapter.kt# News card RecyclerView adapter with Glide image caching
            │   │   ├── category/
            │   │   │   ├── CategoryFragment.kt # Category tabs and filtered feed
            │   │   │   └── CategoryAdapter.kt  # Horizontal category chip selector
            │   │   └── detail/
            │   │       └── ArticleDetailActivity.kt # Full article reader & native Android share sheet
            │   └── utils/
            │       ├── Constants.kt         # Base URL and intent bundle keys
            │       └── DateFormatter.kt     # Gujarati relative time formatting (e.g. "૫ મિનિટ પહેલા")
            └── res/
                ├── layout/                  # XML layout files (Material3, CardViews, SwipeRefresh)
                ├── menu/                    # Bottom navigation menu
                ├── values/                  # Strings (Gujarati & English), colors, light themes
                └── values-night/            # Dark mode theme specifications
```

---

## 🚀 How to Run in Android Studio

1. Open **Android Studio**.
2. Click **File > Open...** and select the folder:
   ```
   Gujarat-Post/Gujaratpost/android
   ```
3. Allow Android Studio to complete Gradle sync and download required SDKs.
4. Select an Android Emulator (or connect a physical Android phone with USB Debugging enabled).
5. Click **Run (`Shift + F10`)**.

---

## 🌐 Configuring the Backend API URL

Open `app/src/main/java/com/gujaratpost/app/utils/Constants.kt`:

- **Android Emulator**: Uses `http://10.0.2.2:5000/` by default (which maps to `localhost:5000` on your PC where the `backend/` server runs).
- **Physical Device over Wi-Fi**: Set to your PC's local IP address:
  ```kotlin
  const val DEFAULT_BASE_URL = "http://192.168.1.XX:5000/"
  ```
- **Production Server**: Set to your live hosted API URL:
  ```kotlin
  const val DEFAULT_BASE_URL = "https://your-api-domain.com/"
  ```

---

## 📦 Building for Release / Google Play Store

To build a release APK or Android App Bundle (AAB):

```bash
# In the android/ directory:
./gradlew bundleRelease    # Generates .aab for Google Play Console
./gradlew assembleRelease  # Generates release .apk
```
