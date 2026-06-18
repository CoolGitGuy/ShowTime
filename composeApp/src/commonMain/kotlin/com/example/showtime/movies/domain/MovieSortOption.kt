package com.example.showtime.movies.domain

enum class MovieSortOption {
    RatingDesc,
    RatingAsc,
    YearDesc,
    YearAsc,
    TitleDesc,
    TitleAsc
}

fun MovieSortOption.displayLabel(): String {
    return when (this) {
        MovieSortOption.RatingDesc -> "Rating desc"
        MovieSortOption.RatingAsc -> "Rating asc"
        MovieSortOption.YearDesc -> "Year desc"
        MovieSortOption.YearAsc -> "Year asc"
        MovieSortOption.TitleDesc -> "Title Z-A"
        MovieSortOption.TitleAsc -> "Title A-Z"
    }
}
