package com.example.bluetoothdemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform