package org.golnaz.project.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.golnaz.project.model.QueryRequest
import org.golnaz.project.model.StreamEvent
import org.golnaz.project.model.UploadDocumentResponse

class DocumentApiDataSource(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun uploadPdf(fileName: String, pdfBytes: ByteArray): UploadDocumentResponse {
        val safeName = if (fileName.lowercase().endsWith(".pdf")) fileName else "$fileName.pdf"
        val response = httpClient.post("$baseUrl/upload") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "file",
                            value = pdfBytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "application/pdf")
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "form-data; name=\"file\"; filename=\"$safeName\""
                                )
                            }
                        )
                    }
                )
            )
        }
        if (!response.status.isSuccess()) {
            val detail = try {
                json.parseToJsonElement(response.bodyAsText())
                    .jsonObject["detail"]?.jsonPrimitive?.content
            } catch (_: Exception) { null }
            throw Exception(detail ?: "Upload failed (HTTP ${response.status.value})")
        }
        return response.body()
    }

    fun askQuestionStream(docId: String, question: String): Flow<StreamEvent> = flow {
        httpClient.preparePost("$baseUrl/ask-stream") {
            timeout {
                requestTimeoutMillis = Long.MAX_VALUE
                socketTimeoutMillis = Long.MAX_VALUE
            }
            contentType(ContentType.Application.Json)
            setBody(QueryRequest(question = question, docId = docId))
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                @Suppress("DEPRECATION")
                val line = channel.readUTF8Line() ?: break
                if (line.startsWith("data: ") && line.length > 6) {
                    try {
                        emit(json.decodeFromString<StreamEvent>(line.drop(6)))
                    } catch (_: Exception) {}
                }
            }
        }
    }
}