package org.golnaz.project.ui

import androidx.compose.runtime.Composable
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberFilePicker(onFilePicked: (fileName: String, bytes: ByteArray) -> Unit): () -> Unit {
    return {
        val chooser = JFileChooser()
        chooser.fileFilter = FileNameExtensionFilter("PDF Files", "pdf")
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = chooser.selectedFile
            onFilePicked(file.name, file.readBytes())
        }
    }
}