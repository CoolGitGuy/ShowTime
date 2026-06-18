package com.example.showtime.movies.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_sections")
data class CatalogSectionEntity(
    @PrimaryKey
    val sectionKey: String,
    val totalPages: Int,
    val totalItems: Int,
    val pageSize: Int
)
