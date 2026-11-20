# AiRagApp

A **Kotlin Multiplatform** app that lets you upload a PDF and ask questions about it using a RAG (Retrieval-Augmented Generation) AI backend powered by Google Gemini. Runs on Android, iOS, Desktop (JVM), and Web from a single shared codebase.

---

## Demo

### Android & iOS

<!-- Add screenshots: drag images onto this page in GitHub editor, then paste the URLs below -->

| Android | iOS |
|---------|-----|
| <img alt="Screenshot_20261120_141855" src="https://github.com/user-attachments/assets/12aba6b6-ea2d-4dda-bc1b-eef5954df443" width="250"> | <img alt="Simulator Screenshot - iPhone 16 - 2026-11-20 at 14 18 47" src="https://github.com/user-attachments/assets/773dc2cb-454f-472d-a1e7-0fb4cc85d04a" width="250"> |

### Desktop & Web

| Desktop | Web |
|---------|-----|
| <img alt="image" src="https://github.com/user-attachments/assets/1c5022ed-9f47-4570-a0b3-d1f272920104" width="400" alt="Desktop screenshot"> | <img alt="Screenshot 2026-11-20 at 15 23 07" src="https://github.com/user-attachments/assets/8ad3681c-96d4-4c5f-8f4f-a5882343407a"  width="400"> |


https://github.com/user-attachments/assets/bd080c63-0ca2-40db-bf3e-026a91e7ee01




https://github.com/user-attachments/assets/f017c84c-4ada-4b97-9b4b-06660d3982d4

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

The backend is a separate Python service: [golnaaz/ai-rag-backend](https://github.com/golnaaz/ai-rag-backend)

### Tech Stack

| Layer | Technology |
|-------|-----------|
| API server | [FastAPI](https://fastapi.tiangolo.com/) + [uvicorn](https://www.uvicorn.org/) (ASGI) |
| Streaming | Server-Sent Events (SSE) via `StreamingResponse` |
| AI — embeddings | Google Gemini `gemini-embedding-001` via `langchain-google-genai` |
| AI — generation | Google Gemini `gemini-2.5-flash` via `google-genai` SDK |
| Vector database | [Qdrant](https://qdrant.tech/) (local on-disk mode via `qdrant-client`) |
| PDF ingestion | [LangChain](https://python.langchain.com/) — `PyPDFLoader` + `RecursiveCharacterTextSplitter` (chunk size 1000, overlap 150) |
| Schema validation | [Pydantic](https://docs.pydantic.dev/) v2 |
| Config / secrets | [python-dotenv](https://github.com/theskumar/python-dotenv) — `GOOGLE_API_KEY` in `.env` (never committed) |
| Language | Python 3.11+ |

### How It Works

1. **Upload** — a PDF is received, split into ~1000-character chunks, embedded with Gemini, and stored in Qdrant with per-chunk metadata (`doc_id`, `filename`, `page_number`).
2. **Ask** — the question is embedded, the top-4 semantically similar chunks are retrieved (filtered to the uploaded doc), and Gemini streams an answer token-by-token over SSE.

### API Reference

#### `POST /upload`

Upload a PDF for indexing.

```
Content-Type: multipart/form-data
Body: file=<pdf>
```

Response `200 OK`:
```json
{
  "doc_id":      "550e8400-e29b-41d4-a716-446655440000",
  "filename":    "my-document.pdf",
  "pages_count": 12,
  "chunks_count": 47,
  "status":      "indexed"
}
```

#### `POST /ask-stream`

Ask a question about an uploaded document. Returns an SSE stream.

```
Content-Type: application/json
Body: { "question": "What is the main topic?", "doc_id": "<from upload>" }
```

SSE event types:

| `type` | Fields | Description |
|--------|--------|-------------|
| `token` | `content: str` | Partial answer text, emitted as Gemini generates |
| `done` | `sources: list[int]` | Stream complete; source page numbers included |
| `error` | `message: str` | Server-side error |

#### `GET /`

Health check — returns `{"status": "Backend is running smoothly!"}`.

### Setup

```bash
cd ai-rag-backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt

# Create .env with your Gemini API key
echo "GOOGLE_API_KEY=your_key_here" > .env

uvicorn main:app --host 0.0.0.0 --port 8000
```

> `GOOGLE_API_KEY` is loaded from `.env` via python-dotenv and is never hardcoded or committed to source control.

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
