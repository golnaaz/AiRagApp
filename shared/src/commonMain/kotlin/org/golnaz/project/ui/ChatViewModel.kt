package org.golnaz.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.golnaz.project.domain.usecase.AskQuestionUseCase
import org.golnaz.project.domain.usecase.UploadDocumentUseCase

class ChatViewModel(
    private val uploadDocumentUseCase: UploadDocumentUseCase,
    private val askQuestionUseCase: AskQuestionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onQuestionChanged(text: String) {
        _uiState.update { it.copy(questionInput = text) }
    }

    fun uploadDocument(fileName: String, bytes: ByteArray) {
        try {
            viewModelScope.launch {
                _uiState.update { it.copy(isUploading = true, uploadStatus = null) }
                uploadDocumentUseCase(fileName, bytes)
                    .onSuccess { response ->
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                uploadedFileName = response.filename,
                                docId = response.docId,
                                uploadStatus = "${response.pagesCount} pages · ${response.chunksCount} chunks indexed"
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                uploadStatus = "Upload failed: ${error.message}"
                            )
                        }
                    }
            }
        } catch (e: Throwable) {
            // Catches synchronous failures from viewModelScope itself (e.g. dispatcher issues)
            _uiState.update {
                it.copy(isUploading = false, uploadStatus = "VM launch error: ${e.message}")
            }
        }
    }

    fun askQuestion() {
        val state = _uiState.value
        val docId = state.docId ?: return
        val question = state.questionInput.takeIf { it.isNotBlank() } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isAsking = true, aiAnswer = "") }
            try {
                askQuestionUseCase(docId, question).collect { event ->
                    when (event.type) {
                        "token" -> _uiState.update { it.copy(aiAnswer = it.aiAnswer + event.content.orEmpty()) }
                        "done"  -> _uiState.update { it.copy(isAsking = false) }
                        "error" -> _uiState.update { it.copy(isAsking = false, aiAnswer = "Error: ${event.message}") }
                    }
                }
            } finally {
                _uiState.update { it.copy(isAsking = false) }
            }
        }
    }
}