package org.golnaz.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.golnaz.project.model.StreamEvent
import org.golnaz.project.repository.AiRepository

class AskQuestionUseCase(private val repository: AiRepository) {
    operator fun invoke(docId: String, question: String): Flow<StreamEvent> {
        return repository.askQuestionStream(docId, question)
    }
}