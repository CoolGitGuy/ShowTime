package com.example.showtime.movies.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<T>
)

@Serializable
data class GenreApiModel(
    val id: Int,
    val name: String
)

@Serializable
data class MovieListItemDto(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val genres: List<GenreApiModel>? = emptyList()
)

@Serializable
data class MovieDetailDto(
    val imdbId: String,
    val title: String,
    val overview: String? = null,
    val tagline: String? = null,
    val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val homepage: String? = null,
    val genres: List<GenreApiModel> = emptyList()
)

@Serializable
data class PersonSummaryDto(
    val imdbId: String,
    val name: String,
    val profilePath: String? = null
)

@Serializable
data class LeaderboardEntryDto(
    val rank: Int,
    @SerialName("user_id")
    val userId: Int,
    val username: String,
    @SerialName("full_name")
    val fullName: String,
    val score: Float,
    @SerialName("played_at")
    val playedAt: Long,
    @SerialName("total_plays")
    val totalPlays: Int
)

@Serializable
data class QuizResultDto(
    val id: Int,
    val category: Int,
    val score: Float,
    @SerialName("played_at")
    val playedAt: Long
)

@Serializable
data class PostQuizResultRequest(
    val score: Float,
    val category: Int
)

@Serializable
data class PostQuizResultResponse(
    val result: QuizResultDto,
    val ranking: Int
)
