package org.golnaz.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.getKoin

@Composable
actual fun chatViewModel(): ChatViewModel {
    val koin = getKoin()
    return remember { koin.get(clazz = ChatViewModel::class) }
}