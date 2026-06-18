package com.example.showtime.movies.data

import androidx.paging.PagingData
import com.example.showtime.movies.domain.CastMember
import com.example.showtime.movies.domain.Genre
import com.example.showtime.movies.domain.LeaderboardEntry
import com.example.showtime.movies.domain.MovieDetails
import com.example.showtime.movies.domain.MovieFilters
import com.example.showtime.movies.domain.MovieSummary
import com.example.showtime.movies.domain.QuizHistoryEntry
import com.example.showtime.movies.domain.QuizSubmissionResult
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun pagedMovies(filters: MovieFilters): Flow<PagingData<MovieSummary>>

    suspend fun refreshGenres(): List<Genre>

    fun observeMovie(movieId: String): Flow<MovieDetails?>

    suspend fun refreshMovieDetails(movieId: String)

    suspend fun ensureCatalogBootstrap()

    suspend fun countQuizCandidates(): Int

    suspend fun getTopQuizCandidates(limit: Int): List<MovieSummary>

    suspend fun getTopQuizCandidatesExcluding(excludedIds: Set<String>, limit: Int): List<MovieSummary>

    suspend fun getMovieCast(movieId: String): List<CastMember>

    fun observeFavorites(userId: Int): Flow<List<MovieSummary>>

    fun observeWatchlist(userId: Int): Flow<List<MovieSummary>>

    fun observeFavoriteIds(userId: Int): Flow<Set<String>>

    fun observeWatchlistIds(userId: Int): Flow<Set<String>>

    fun observeFavoriteCount(userId: Int): Flow<Int>

    fun observeWatchlistCount(userId: Int): Flow<Int>

    suspend fun refreshFavorites(userId: Int)

    suspend fun refreshWatchlist(userId: Int)

    suspend fun setFavorite(userId: Int, movieId: String, enabled: Boolean)

    suspend fun setWatchlist(userId: Int, movieId: String, enabled: Boolean)

    suspend fun clearUserScopedData(userId: Int)

    fun observeBestQuizScore(userId: Int): Flow<Float?>

    fun observeQuizPlayCount(userId: Int): Flow<Int>

    fun observeRecentQuizResults(userId: Int, limit: Int): Flow<List<QuizHistoryEntry>>

    suspend fun refreshQuizResults(userId: Int)

    suspend fun submitQuizResult(userId: Int, score: Float, category: Int): QuizSubmissionResult

    suspend fun getLeaderboard(category: Int, page: Int, pageSize: Int): List<LeaderboardEntry>
}
