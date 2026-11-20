@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.golnaz.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise

@JsFun("""() => {
    console.log('[FilePicker] jsPickFilePdf() called');

    if (typeof window.showOpenFilePicker !== 'undefined') {
        console.log('[FilePicker] using showOpenFilePicker');
        return window.showOpenFilePicker({
            types: [{ description: 'PDF', accept: { 'application/pdf': ['.pdf'] } }],
            multiple: false
        }).then(function(handles) {
            return handles[0].getFile();
        }).then(function(file) {
            console.log('[FilePicker] file:', file.name, file.size, 'bytes');
            return file.arrayBuffer().then(function(buf) {
                var arr = new Uint8Array(buf);
                var hex = Array.from(arr, function(b) {
                    return b.toString(16).padStart(2, '0');
                }).join('');
                console.log('[FilePicker] hex ready, len:', hex.length);
                return file.name + '\n' + hex;
            });
        }).catch(function(err) {
            console.log('[FilePicker] showOpenFilePicker error:', String(err));
            return null;
        });
    }

    console.log('[FilePicker] fallback: input.click()');
    return new Promise(function(resolve) {
        var input = document.getElementById('__kmpPdfInput');
        if (!input) {
            console.error('[FilePicker] #__kmpPdfInput not found');
            resolve(null);
            return;
        }
        function onFileChange() {
            input.removeEventListener('change', onFileChange);
            var file = input.files && input.files[0];
            input.value = '';
            if (!file) { resolve(null); return; }
            console.log('[FilePicker] input file:', file.name, file.size, 'bytes');
            var reader = new FileReader();
            reader.onload = function(e) {
                var arr = new Uint8Array(e.target.result);
                var hex = Array.from(arr, function(b) {
                    return b.toString(16).padStart(2, '0');
                }).join('');
                console.log('[FilePicker] FileReader done, hex len:', hex.length);
                resolve(file.name + '\n' + hex);
            };
            reader.onerror = function(e) {
                console.error('[FilePicker] FileReader error:', e);
                resolve(null);
            };
            reader.readAsArrayBuffer(file);
        }
        input.addEventListener('change', onFileChange);
        console.log('[FilePicker] calling input.click()');
        input.click();
    });
}""")
private external fun jsPickFilePdf(): Promise<JsString?>

@Composable
actual fun rememberFilePicker(onFilePicked: (fileName: String, bytes: ByteArray) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val latestCallback by rememberUpdatedState(onFilePicked)
    return {
        println("[FilePicker] button clicked")
        val promise = jsPickFilePdf()
        scope.launch {
            try {
                println("[FilePicker] awaiting promise")
                val result = promise.await()
                if (result == null) {
                    println("[FilePicker] null result — cancelled or error")
                    return@launch
                }
                val str = result.toString()
                val sep = str.indexOf('\n')
                if (sep < 0) {
                    println("[FilePicker] no separator found")
                    return@launch
                }
                val fileName = str.substring(0, sep)
                val hex = str.substring(sep + 1)
                println("[FilePicker] fileName=$fileName hexLen=${hex.length}")
                val bytes = ByteArray(hex.length / 2) { i ->
                    hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
                println("[FilePicker] ${bytes.size} bytes decoded, calling uploadDocument")
                latestCallback(fileName, bytes)
            } catch (e: Throwable) {
                println("[FilePicker] EXCEPTION ${e::class.simpleName}: ${e.message}")
            }
        }
    }
}