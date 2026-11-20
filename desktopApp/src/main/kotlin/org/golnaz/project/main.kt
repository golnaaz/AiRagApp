package org.golnaz.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.golnaz.project.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "AiRagApp",
    ) {
        App()
    }
}