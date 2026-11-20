package org.golnaz.project.domain.usecase

import org.golnaz.project.model.UploadDocumentResponse
import org.golnaz.project.repository.AiRepository

class UploadDocumentUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(fileName: String, bytes: ByteArray): Result<UploadDocumentResponse> {
        return repository.uploadDocument(fileName, bytes)
    }
}