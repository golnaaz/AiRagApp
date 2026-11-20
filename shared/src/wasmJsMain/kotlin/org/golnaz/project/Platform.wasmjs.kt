package org.golnaz.project

class WasmJsPlatform : Platform {
    override val name: String = "Web (WasmJs)"
}

actual fun getPlatform(): Platform = WasmJsPlatform()