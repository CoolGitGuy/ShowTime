package com.example.showtime.movies.domain

data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val username: String,
    val fullName: String,
    val score: Float,
    val playedAt: Long,
    val totalPlays: Int
)
