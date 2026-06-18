package com.example.showtime.movies.domain

data class QuizHistoryEntry(
    val id: Int,
    val category: Int,
    val score: Float,
    val playedAt: Long,
    val ranking: Int?
)
