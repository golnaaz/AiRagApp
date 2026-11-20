package org.golnaz.project.di

import org.golnaz.project.data.BASE_URL
import org.golnaz.project.data.DocumentApiDataSource
import org.golnaz.project.data.NetworkClient
import org.golnaz.project.domain.usecase.AskQuestionUseCase
import org.golnaz.project.domain.usecase.UploadDocumentUseCase
import org.golnaz.project.repository.AiRepository
import org.golnaz.project.ui.ChatViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // Data
    single { NetworkClient.httpClient }
    single { DocumentApiDataSource(httpClient = get(), baseUrl = BASE_URL) }

    // Repository
    single { AiRepository(dataSource = get()) }

    // Use Cases
    factoryOf(::UploadDocumentUseCase)
    factoryOf(::AskQuestionUseCase)

    // Registered as singleton so koin.get() finds it on all platforms
    singleOf(::ChatViewModel)
}