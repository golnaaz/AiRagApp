package org.golnaz.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise

// All file reading happens in JS. Result is "filename\nhexBytes".
//
// Strategy:
//   1. showOpenFilePicker() — File System Access API, Chrome 86 / Edge 86 / Safari 15.2+.
//      Explicitly designed for user-gesture contexts in web apps.
//   2. Pre-existing <input id="__kmpPdfInput"> from index.html — avoids creating a
//      new element dynamically, which is less reliable for input.click() in some browsers.
//
// Every step logs to the browser console so failures are visible in F12 → Console.
@JsFun("""() => {
    console.log('[FilePicker] jsPickFilePdf() called');

    // ── Path 1: showOpenFilePicker (Chrome/Edge/Safari 15.2+) ────────────────
    if (typeof window.showOpenFilePicker !== 'undefined') {
        console.log('[FilePicker] using showOpenFilePicker');
        return window.showOpenFilePicker({
            types: [{ description: 'PDF', accept: { 'application/pdf': ['.pdf'] } }],
            multiple: false
        }).then(function(handles) {
            console.log('[FilePicker] showOpenFilePicker got handle');
            return handles[0].getFile();
        }).then(function(file) {
            console.log('[FilePicker] getFile ok:', file.name, file.size, 'bytes');
            return file.arrayBuffer().then(function(buf) {
                var arr = new Uint8Array(buf);
                var hex = Array.from(arr, function(b) {
                    return b.toString(16).padStart(2, '0');
                }).join('');
                console.log('[FilePicker] hex ready, length:', hex.length);
                return file.name + '\n' + hex;
            });
        }).catch(function(err) {
            console.log('[FilePicker] showOpenFilePicker error (may be user cancel):', String(err));
            return null;
        });
    }

    // ── Path 2: pre-existing <input> from index.html ──────────────────────────
    console.log('[FilePicker] showOpenFilePicker not available, using input.click() fallback');
    return new Promise(function(resolve) {
        var input = document.getElementById('__kmpPdfInput');
        if (!input) {
            console.error('[FilePicker] #__kmpPdfInput not found in DOM');
            resolve(null);
            return;
        }

        function onFileChange() {
            input.removeEventListener('change', onFileChange);
            var file = input.files && input.files[0];
            input.value = '';  // reset so the same file can be picked again
            if (!file) {
                console.log('[FilePicker] no file in input (cancelled?)');
                resolve(null);
                return;
            }
            console.log('[FilePicker] input.onchange: file=', file.name, file.size, 'bytes');
            var reader = new FileReader();
            reader.onload = function(e) {
                var arr = new Uint8Array(e.target.result);
                var hex = Array.from(arr, function(b) {
                    return b.toString(16).padStart(2, '0');
                }).join('');
                console.log('[FilePicker] FileReader done, hex length:', hex.length);
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
        // jsPickFilePdf() MUST be called synchronously here (before any suspend point)
        // so the browser treats it as part of the user-gesture activation chain.
        println("[FilePicker] button clicked, starting jsPickFilePdf()")
        val promise = jsPickFilePdf()
        scope.launch {
            try {
                println("[FilePicker] awaiting promise...")
                val result = promise.await()
                if (result == null) {
                    println("[FilePicker] promise resolved null — user cancelled or error")
                    return@launch
                }
                val str = result.toString()
                val sep = str.indexOf('\n')
                if (sep < 0) {
                    println("[FilePicker] ERROR: no separator in result string")
                    return@launch
                }
                val fileName = str.substring(0, sep)
                val hex = str.substring(sep + 1)
                println("[FilePicker] fileName=$fileName hexLen=${hex.length}")
                val bytes = ByteArray(hex.length / 2) { i ->
                    hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
                println("[FilePicker] decoded ${bytes.size} bytes, calling uploadDocument")
                latestCallback(fileName, bytes)
            } catch (e: Throwable) {
                println("[FilePicker] EXCEPTION: ${e::class.simpleName}: ${e.message}")
            }
        }
    }
}