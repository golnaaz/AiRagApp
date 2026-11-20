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

The backend lives in the [`backend/`](./backend) folder — a Python FastAPI server that powers the RAG pipeline.

### How it works

1. On startup it loads `sample.pdf`, splits it into chunks, and stores them as vectors in a local **Qdrant** database
2. On each request it finds the 3 most relevant chunks via semantic search
3. Injects them as context into a **Gemini** prompt and streams the answer back token by token over SSE

### Setup

```bash
cd backend

python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

pip install -r requirements.txt

cp .env.example .env
# Edit .env and set GOOGLE_API_KEY=your_key

cp /path/to/your/file.pdf sample.pdf

uvicorn main:app --host 0.0.0.0 --port 8000
```

API endpoint: `POST /ask-stream` — streams the answer as Server-Sent Events.

For the Android emulator use `http://10.0.2.2:8000` as the base URL (emulator alias for host machine localhost).

## Project Structure

```
backend/         → Python FastAPI RAG server
  main.py        → streaming /ask-stream endpoint
  rag_service.py → Qdrant vector store + prompt builder
  schemas.py     → Pydantic request/response models
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
