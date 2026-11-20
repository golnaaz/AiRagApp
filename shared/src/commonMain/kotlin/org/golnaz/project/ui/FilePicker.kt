package org.golnaz.project.ui

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFilePicker(onFilePicked: (fileName: String, bytes: ByteArray) -> Unit): () -> Unit