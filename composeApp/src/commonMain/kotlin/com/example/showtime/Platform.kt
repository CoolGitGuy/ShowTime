package com.example.showtime

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
