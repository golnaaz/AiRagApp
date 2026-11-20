package org.golnaz.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberFilePicker(onFilePicked: (fileName: String, bytes: ByteArray) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val latestCallback by rememberUpdatedState(onFilePicked)
    return {
        scope.launch {
            // Show dialog on the main (Swing EDT) thread — this suspends the coroutine
            // but does NOT block the EDT with file I/O.
            val chooser = JFileChooser()
            chooser.fileFilter = FileNameExtensionFilter("PDF Files", "pdf")
            val approved = chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
            if (approved) {
                val file = chooser.selectedFile
                // Read bytes off the main thread so Compose can process state changes
                // while the file is being read (e.g. show the upload spinner).
                val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                latestCallback(file.name, bytes)
            }
        }
    }
}