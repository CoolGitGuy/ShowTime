package com.example.showtime.movies.db

import androidx.room.Entity

@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "movieId"]
)
data class FavoriteEntity(
    val userId: Int,
    val movieId: String,
    val createdAt: Long
)
