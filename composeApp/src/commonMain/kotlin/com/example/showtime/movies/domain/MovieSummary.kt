package com.example.showtime.movies.domain

data class MovieSummary(
    val id: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<String>
)
