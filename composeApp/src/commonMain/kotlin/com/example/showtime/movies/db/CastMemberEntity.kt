package com.example.showtime.movies.db

import androidx.room.Entity

@Entity(
    tableName = "cast_members",
    primaryKeys = ["movieId", "personId"]
)
data class CastMemberEntity(
    val movieId: String,
    val personId: String,
    val name: String,
    val orderIndex: Int,
    val profilePath: String?
)
