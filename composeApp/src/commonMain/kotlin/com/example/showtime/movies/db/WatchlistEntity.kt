package com.example.showtime.movies.db

import androidx.room.Entity

@Entity(
    tableName = "watchlist",
    primaryKeys = ["userId", "movieId"]
)
data class WatchlistEntity(
    val userId: Int,
    val movieId: String,
    val createdAt: Long
)
