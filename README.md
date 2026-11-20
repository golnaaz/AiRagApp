# AiRagApp

## WORK IN PROGRESS

A Kotlin Multiplatform app (Android + Desktop) built with Compose Multiplatform that lets you ask questions about a PDF document via an AI RAG backend.

## Features

- Ask questions in natural language about any PDF
- Answers powered by Google Gemini via a RAG pipeline
- Shows source page numbers from the PDF
- Runs on Android and Desktop from a single shared codebase

## Tech Stack

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor](https://ktor.io/) — HTTP client
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) — JSON

## Backend

This app connects to [ai-rag-backend](https://github.com/golnaaz/ai-rag-backend) — a Python FastAPI server.

For Android emulator the base URL is `http://10.0.2.2:8000` (emulator alias for host machine localhost). Start the backend with:

```bash
uvicorn main:app --host 0.0.0.0 --port 8000
```

## Project Structure

```
shared/          → shared Kotlin code (UI, network, models)
  data/          → Ktor HTTP client setup
  model/         → request/response data classes
  repository/    → API calls
  ui/            → Compose screens
androidApp/      → Android entry point
desktopApp/      → Desktop entry point
```

## Running the apps

- Android: `./gradlew :androidApp:assembleDebug`
- Desktop: `./gradlew :desktopApp:run`
