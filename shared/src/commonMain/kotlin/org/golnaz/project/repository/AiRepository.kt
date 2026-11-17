package org.golnaz.project.repository

import org.golnaz.project.data.NetworkClient
import org.golnaz.project.model.QueryRequest
import org.golnaz.project.model.RagResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AiRepository {
    private val client = NetworkClient.httpClient

    // Use 10.0.2.2 for Android Emulator, or your local IP/Cloud URL for real devices
    private val baseUrl = "http://10.0.2.2:8000"

    suspend fun askQuestion(questionText: String): Result<RagResponse> {
        return try {
            val response: RagResponse = client.post("$baseUrl/ask") {
                contentType(ContentType.Application.Json)
                setBody(QueryRequest(question = questionText))
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}