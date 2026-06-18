package com.example.showtime.session

data class UserSession(
    val userId: Int,
    val username: String,
    val fullName: String,
    val accessToken: String
)
