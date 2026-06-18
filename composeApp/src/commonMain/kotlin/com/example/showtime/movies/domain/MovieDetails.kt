package com.example.showtime.movies.domain

data class MovieDetails(
    val id: String,
    val title: String,
    val year: Int?,
    val runtime: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val overview: String?,
    val releaseDate: String?,
    val tagline: String?,
    val homepage: String?,
    val genres: List<String>,
    val cast: List<CastMember>
)
