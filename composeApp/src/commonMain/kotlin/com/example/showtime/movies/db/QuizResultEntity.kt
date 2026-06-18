package com.example.showtime.movies.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val category: Int,
    val score: Float,
    val playedAt: Long,
    val ranking: Int?
)
