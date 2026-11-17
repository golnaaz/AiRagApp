package org.golnaz.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform