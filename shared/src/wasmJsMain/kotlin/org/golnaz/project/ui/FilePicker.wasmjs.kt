package org.golnaz.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise

// Uses showOpenFilePicker() (File System Access API) in modern browsers — explicit
// user-gesture support. Falls back to input.click() for older browsers.
// Result is "filename\nhexBytes" so parsing stays pure Kotlin.
@JsFun("""() => {
    if (typeof window.showOpenFilePicker !== 'undefined') {
        return window.showOpenFilePicker({
            types: [{ description: 'PDF', accept: { 'application/pdf': ['.pdf'] } }],
            multiple: false
        }).then(function(handles) {
            return handles[0].getFile();
        }).then(function(file) {
            return file.arrayBuffer().then(function(buf) {
                var arr = new Uint8Array(buf);
                var hex = Array.from(arr, function(b) { return b.toString(16).padStart(2,'0'); }).join('');
                return file.name + '\n' + hex;
            });
        }).catch(function() { return null; });
    }
    return new Promise(function(resolve) {
        var input = document.createElement('input');
        input.type = 'file';
        input.accept = 'application/pdf';
        input.onchange = function() {
            var file = this.files ? this.files[0] : null;
            if (!file) { resolve(null); return; }
            var reader = new FileReader();
            reader.onload = function(e) {
                var arr = new Uint8Array(e.target.result);
                var hex = Array.from(arr, function(b) { return b.toString(16).padStart(2,'0'); }).join('');
                resolve(file.name + '\n' + hex);
            };
            reader.readAsArrayBuffer(file);
        };
        input.click();
    });
}""")
private external fun jsPickFilePdf(): Promise<JsString?>

@Composable
actual fun rememberFilePicker(onFilePicked: (fileName: String, bytes: ByteArray) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val latestCallback by rememberUpdatedState(onFilePicked)
    return {
        // Must call jsPickFilePdf() synchronously here (before any suspend point)
        // so the browser treats it as part of the user-gesture activation chain.
        val promise = jsPickFilePdf()
        scope.launch {
            val result = promise.await() ?: return@launch
            val str = result.toString()
            val sep = str.indexOf('\n')
            if (sep < 0) return@launch
            val fileName = str.substring(0, sep)
            val hex = str.substring(sep + 1)
            val bytes = ByteArray(hex.length / 2) { i ->
                hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
            latestCallback(fileName, bytes)
        }
    }
}
