package org.golnaz.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadDocumentResponse(
    @SerialName("doc_id") val docId: String,
    val filename: String,
    @SerialName("pages_count") val pagesCount: Int,
    @SerialName("chunks_count") val chunksCount: Int,
    val status: String
)
@Serializable
data class QueryRequest(
    val question: String,
    @SerialName("doc_id") val docId: String
)

@Serializable
data class RagResponse(
    val status: String,
    val answer: String,
    val sources: List<Int>
)

@Serializable
data class StreamEvent(
    val type: String,
    val content: String? = null,
    val sources: List<Int>? = null,
    val message: String? = null
)
