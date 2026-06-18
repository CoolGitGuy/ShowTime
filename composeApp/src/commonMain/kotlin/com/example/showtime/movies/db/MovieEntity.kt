package com.example.showtime.movies.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val backdropPath: String?,
    val genresCsv: String,
    val overview: String?,
    val runtime: Int?,
    val releaseDate: String?,
    val tagline: String?,
    val homepage: String?
)
