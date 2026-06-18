package com.example.showtime.movies.domain

data class MovieFilters(
    val query: String = "",
    val genreId: String? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Float? = null,
    val sortOption: MovieSortOption = MovieSortOption.RatingDesc
) {
    val sectionKey: String
        get() = buildString {
            append("q=").append(query.trim())
            append("|genre=").append(genreId.orEmpty())
            append("|minYear=").append(minYear ?: "")
            append("|maxYear=").append(maxYear ?: "")
            append("|minRating=").append(minRating ?: "")
            append("|sort=").append(sortOption.name)
        }
}
