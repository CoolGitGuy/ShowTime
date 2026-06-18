package com.example.showtime.movies.db

import androidx.room.Entity

@Entity(
    tableName = "catalog_entries",
    primaryKeys = ["sectionKey", "movieId"]
)
data class CatalogEntryEntity(
    val sectionKey: String,
    val movieId: String,
    val page: Int,
    val indexInPage: Int
)
