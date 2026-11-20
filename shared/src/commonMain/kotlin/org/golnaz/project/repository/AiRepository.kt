package org.golnaz.project.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.golnaz.project.data.DocumentApiDataSource
import org.golnaz.project.model.StreamEvent
import org.golnaz.project.model.UploadDocumentResponse

class AiRepository(private val dataSource: DocumentApiDataSource) {

    suspend fun uploadDocument(fileName: String, pdfBytes: ByteArray): Result<UploadDocumentResponse> {
        return runCatching {
            dataSource.uploadPdf(fileName = fileName, pdfBytes = pdfBytes)
        }
    }

    fun askQuestionStream(docId: String, question: String): Flow<StreamEvent> {
        return dataSource.askQuestionStream(docId = docId, question = question)
            .catch { emit(StreamEvent(type = "error", message = it.message)) }
    }
}