package org.golnaz.project.model

import kotlinx.serialization.Serializable

@Serializable
data class QueryRequest(
    val question: String
)

@Serializable
data class RagResponse(
    val status: String,
    val answer: String,
    val sources: List<Int>
)
