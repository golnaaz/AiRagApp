# AiRagApp

A **Kotlin Multiplatform** app that lets you upload a PDF and ask questions about it using a RAG (Retrieval-Augmented Generation) AI backend powered by Google Gemini. Runs on Android, iOS, Desktop (JVM), and Web from a single shared codebase.

---

## Demo

### Android & iOS

<!-- Add screenshots: drag images onto this page in GitHub editor, then paste the URLs below -->

| Android | iOS |
|---------|-----|
| <img src="docs/screenshots/android.png" width="250" alt="Android screenshot"> | <img src="docs/screenshots/ios.png" width="250" alt="iOS screenshot"> |

### Desktop & Web

| Desktop | Web |
|---------|-----|
| <img src="docs/screenshots/desktop.png" width="400" alt="Desktop screenshot"> | <img src="docs/screenshots/web.png" width="400" alt="Web screenshot"> |

<!-- To add a demo video: upload your .mp4 or .mov by dragging it into a GitHub issue comment,
     copy the resulting URL, and paste it here as a bare URL — GitHub will embed it automatically. -->

---

## Features

- Upload any PDF directly from the app
- Ask questions in natural language
- Answers stream token-by-token via Server-Sent Events
- Powered by Google Gemini + Qdrant vector search
- Single Kotlin/Compose codebase for all platforms

## Platforms

| Platform | Target |
|----------|--------|
| Android | `androidApp` |
| iOS | `iosApp` |
| Desktop (JVM) | `desktopApp` |
| Web (WasmJs) | `webApp` |

## Tech Stack

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) + [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor](https://ktor.io/) — HTTP client (OkHttp / CIO / Darwin / JS per platform)
- [Koin](https://insert-koin.io/) — dependency injection
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) — JSON
- [androidx.lifecycle ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) — shared ViewModel across platforms

## Backend

The backend is a separate Python FastAPI service:
[golnaaz/ai-rag-backend](https://github.com/golnaaz/ai-rag-backend)

Start it locally before running any client app:

```bash
cd /path/to/ai-rag-backend
uvicorn main:app --host 0.0.0.0 --port 8000
```

## Running the Apps

### Android

```bash
# Forward port so the emulator can reach localhost:8000
adb reverse tcp:8000 tcp:8000

./gradlew :androidApp:installDebug
```

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode and run on a simulator or device.

> The backend URL for iOS simulator is `http://localhost:8000` (simulator shares the host network).

### Desktop

```bash
./gradlew :desktopApp:run
```

### Web

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
# Opens at http://localhost:8080
```

> The backend must be running on the same machine at `http://localhost:8000`.  
> CORS is enabled on the backend so the browser can reach it.

## Project Structure

```
shared/
  commonMain/        → shared UI (Compose), ViewModel, domain, data layer
  androidMain/       → Android platform implementations (FilePicker, BaseUrl)
  iosMain/           → iOS platform implementations
  jvmMain/           → Desktop platform implementations
  wasmJsMain/        → Web platform implementations
androidApp/          → Android entry point + Application class
iosApp/              → Xcode project
desktopApp/          → Desktop entry point
webApp/              → Web entry point (ComposeViewport)
```