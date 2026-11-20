package org.golnaz.project.ui

data class ChatUiState(
    val uploadedFileName: String? = null,
    val docId: String? = null,
    val isUploading: Boolean = false,
    val uploadStatus: String? = null,
    val questionInput: String = "",
    val aiAnswer: String = "Upload a PDF and ask a question...",
    val isAsking: Boolean = false
)