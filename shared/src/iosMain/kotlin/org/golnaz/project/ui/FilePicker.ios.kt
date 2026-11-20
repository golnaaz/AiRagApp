package org.golnaz.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberFilePicker(onFilePicked: (fileName: String, bytes: ByteArray) -> Unit): () -> Unit {
    val latestCallback = rememberUpdatedState(onFilePicked)

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
                url.startAccessingSecurityScopedResource()
                val data = NSData.dataWithContentsOfURL(url) ?: run {
                    url.stopAccessingSecurityScopedResource()
                    return
                }
                val bytes = data.toByteArray()
                val fileName = url.lastPathComponent ?: "document.pdf"
                url.stopAccessingSecurityScopedResource()
                latestCallback.value(fileName, bytes)
            }
        }
    }

    return {
        val picker = UIDocumentPickerViewController(
            documentTypes = listOf("com.adobe.pdf"),
            inMode = platform.UIKit.UIDocumentPickerMode.UIDocumentPickerModeImport
        )
        picker.delegate = delegate
        picker.allowsMultipleSelection = false

        val rootViewController = UIApplication.sharedApplication
            .windows
            .filterIsInstance<platform.UIKit.UIWindow>()
            .firstOrNull()
            ?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val ptr = bytes ?: return ByteArray(0)
    return ByteArray(length.toInt()).apply {
        usePinned { memcpy(it.addressOf(0), ptr, length) }
    }
}