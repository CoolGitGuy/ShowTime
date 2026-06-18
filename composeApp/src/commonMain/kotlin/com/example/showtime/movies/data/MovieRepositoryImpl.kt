package com.example.showtime.movies.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import com.example.showtime.movies.db.CastMemberEntity
import com.example.showtime.movies.db.FavoriteEntity
import com.example.showtime.movies.db.MovieDao
import com.example.showtime.movies.db.MovieEntity
import com.example.showtime.movies.db.QuizResultEntity
import com.example.showtime.movies.db.WatchlistEntity
import com.example.showtime.movies.domain.CastMember
import com.example.showtime.movies.domain.Genre
import com.example.showtime.movies.domain.LeaderboardEntry
import com.example.showtime.movies.domain.MovieDetails
import com.example.showtime.movies.domain.MovieFilters
import com.example.showtime.movies.domain.MovieSortOption
import com.example.showtime.movies.domain.MovieSummary
import com.example.showtime.movies.domain.QuizHistoryEntry
import com.example.showtime.movies.domain.QuizSubmissionResult
import com.example.showtime.networking.toApiException

class MovieRepositoryImpl(
    private val movieDao: MovieDao,
    private val movieApi: MovieApi,
    private val showtimeUserApi: ShowtimeUserApi
) : MovieRepository {
    override fun pagedMovies(filters: MovieFilters): Flow<PagingData<MovieSummary>> {
        return Pager(
            config = PagingConfig(
                pageSize = MoviesCatalogPagingSource.PAGE_SIZE,
                initialLoadSize = MoviesCatalogPagingSource.PAGE_SIZE
            ),
            pagingSourceFactory = {
                MoviesCatalogPagingSource(
                    movieDao = movieDao,
                    movieApi = movieApi,
                    filters = filters
                )
            }
        ).flow
    }

    override suspend fun refreshGenres(): List<Genre> {
        return movieApi.getGenres().map { it.toGenre() }
    }

    override fun observeMovie(movieId: String): Flow<MovieDetails?> {
        return combine(
            movieDao.observeMovie(movieId),
            movieDao.observeCast(movieId)
        ) { movie, cast ->
            movie?.toDetails(cast)
        }
    }

    override suspend fun refreshMovieDetails(movieId: String) {
        runCatching {
            val details = movieApi.getMovieById(movieId)
            val cast = movieApi.getMovieCast(movieId, pageSize = 10).items
            val existingMovie = movieDao.getMovie(movieId)
            val detailEntity = details.toEntity(existingMovie)
            val castEntities = cast.mapIndexed { index, item ->
                CastMemberEntity(
                    movieId = movieId,
                    personId = item.imdbId,
                    name = item.name,
                    orderIndex = index,
                    profilePath = item.profilePath
                )
            }
            movieDao.replaceMovieDetails(detailEntity, castEntities)
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }
    }

    override suspend fun ensureCatalogBootstrap() {
        if (movieDao.countQuizMovieCandidates() >= 10) {
            return
        }

        for (page in 1..5) {
            val response = movieApi.getMovies(
                page = page,
                pageSize = MoviesCatalogPagingSource.PAGE_SIZE,
                sortBy = MovieSortOption.RatingDesc.toApiSortBy(),
                sortOrder = MovieSortOption.RatingDesc.toApiSortOrder()
            )

            movieDao.replaceCatalogPage(
                section = com.example.showtime.movies.db.CatalogSectionEntity(
                    sectionKey = DEFAULT_SECTION_KEY,
                    totalPages = response.totalPages,
                    totalItems = response.totalItems,
                    pageSize = response.pageSize
                ),
                movies = response.items.map { it.toEntity() },
                entries = response.items.mapIndexed { index, movie ->
                    com.example.showtime.movies.db.CatalogEntryEntity(
                        sectionKey = DEFAULT_SECTION_KEY,
                        movieId = movie.imdbId,
                        page = page,
                        indexInPage = index
                    )
                }
            )
        }
    }

    override suspend fun countQuizCandidates(): Int {
        return movieDao.countQuizMovieCandidates()
    }

    override suspend fun getTopQuizCandidates(limit: Int): List<MovieSummary> {
        return movieDao.getTopQuizCandidates(limit).map { it.toSummary() }
    }

    override suspend fun getTopQuizCandidatesExcluding(
        excludedIds: Set<String>,
        limit: Int
    ): List<MovieSummary> {
        val ids = if (excludedIds.isEmpty()) listOf("__none__") else excludedIds.toList()
        return movieDao.getTopQuizCandidatesExcluding(ids, limit).map { it.toSummary() }
    }

    override suspend fun getMovieCast(movieId: String): List<CastMember> {
        val existingCast = movieDao.getCast(movieId)
        if (existingCast.isNotEmpty()) {
            return existingCast.map { it.toDomain() }
        }

        refreshMovieDetails(movieId)
        return movieDao.getCast(movieId).map { it.toDomain() }
    }

    override fun observeFavorites(userId: Int): Flow<List<MovieSummary>> {
        return movieDao.observeFavorites(userId).map { rows -> rows.map { it.toSummary() } }
    }

    override fun observeWatchlist(userId: Int): Flow<List<MovieSummary>> {
        return movieDao.observeWatchlist(userId).map { rows -> rows.map { it.toSummary() } }
    }

    override fun observeFavoriteIds(userId: Int): Flow<Set<String>> {
        return movieDao.observeFavoriteIds(userId).map { it.toSet() }
    }

    override fun observeWatchlistIds(userId: Int): Flow<Set<String>> {
        return movieDao.observeWatchlistIds(userId).map { it.toSet() }
    }

    override fun observeFavoriteCount(userId: Int): Flow<Int> {
        return movieDao.observeFavoriteCount(userId)
    }

    override fun observeWatchlistCount(userId: Int): Flow<Int> {
        return movieDao.observeWatchlistCount(userId)
    }

    override suspend fun refreshFavorites(userId: Int) {
        runCatching {
            val response = showtimeUserApi.getFavorites()
            val now = Clock.System.now().toEpochMilliseconds()
            movieDao.upsertMovies(response.map { it.toEntity() })
            movieDao.replaceFavorites(
                userId = userId,
                favorites = response.mapIndexed { index, movie ->
                    FavoriteEntity(
                        userId = userId,
                        movieId = movie.imdbId,
                        createdAt = now - index
                    )
                }
            )
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }
    }

    override suspend fun refreshWatchlist(userId: Int) {
        runCatching {
            val response = showtimeUserApi.getWatchlist()
            val now = Clock.System.now().toEpochMilliseconds()
            movieDao.upsertMovies(response.map { it.toEntity() })
            movieDao.replaceWatchlist(
                userId = userId,
                watchlist = response.mapIndexed { index, movie ->
                    WatchlistEntity(
                        userId = userId,
                        movieId = movie.imdbId,
                        createdAt = now - index
                    )
                }
            )
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }
    }

    override suspend fun setFavorite(userId: Int, movieId: String, enabled: Boolean) {
        val optimisticRow = FavoriteEntity(
            userId = userId,
            movieId = movieId,
            createdAt = Clock.System.now().toEpochMilliseconds()
        )

        if (enabled) {
            movieDao.upsertFavorites(listOf(optimisticRow))
        } else {
            movieDao.deleteFavorite(userId, movieId)
        }

        runCatching {
            if (enabled) {
                showtimeUserApi.addFavorite(movieId)
            } else {
                showtimeUserApi.removeFavorite(movieId)
            }
        }.onFailure { throwable ->
            if (enabled) {
                movieDao.deleteFavorite(userId, movieId)
            } else {
                movieDao.upsertFavorites(listOf(optimisticRow))
            }
            throw throwable.toApiException()
        }
    }

    override suspend fun setWatchlist(userId: Int, movieId: String, enabled: Boolean) {
        val optimisticRow = WatchlistEntity(
            userId = userId,
            movieId = movieId,
            createdAt = Clock.System.now().toEpochMilliseconds()
        )

        if (enabled) {
            movieDao.upsertWatchlist(listOf(optimisticRow))
        } else {
            movieDao.deleteWatchlistItem(userId, movieId)
        }

        runCatching {
            if (enabled) {
                showtimeUserApi.addWatchlistItem(movieId)
            } else {
                showtimeUserApi.removeWatchlistItem(movieId)
            }
        }.onFailure { throwable ->
            if (enabled) {
                movieDao.deleteWatchlistItem(userId, movieId)
            } else {
                movieDao.upsertWatchlist(listOf(optimisticRow))
            }
            throw throwable.toApiException()
        }
    }

    override suspend fun clearUserScopedData(userId: Int) {
        movieDao.clearUserScopedData(userId)
    }

    override fun observeBestQuizScore(userId: Int): Flow<Float?> {
        return movieDao.observeBestQuizScore(userId)
    }

    override fun observeQuizPlayCount(userId: Int): Flow<Int> {
        return movieDao.observeQuizPlayCount(userId)
    }

    override fun observeRecentQuizResults(userId: Int, limit: Int): Flow<List<QuizHistoryEntry>> {
        return movieDao.observeQuizResults(userId, limit).map { rows ->
            rows.map { it.toDomain() }
        }
    }

    override suspend fun refreshQuizResults(userId: Int) {
        runCatching {
            val response = showtimeUserApi.getMyQuizResults(pageSize = 100)
            movieDao.deleteQuizResultsForUser(userId)
            movieDao.upsertQuizResults(
                response.items.map {
                    QuizResultEntity(
                        id = it.id,
                        userId = userId,
                        category = it.category,
                        score = it.score,
                        playedAt = it.playedAt,
                        ranking = null
                    )
                }
            )
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }
    }

    override suspend fun submitQuizResult(
        userId: Int,
        score: Float,
        category: Int
    ): QuizSubmissionResult {
        return runCatching {
            val response = showtimeUserApi.postQuizResult(
                PostQuizResultRequest(
                    score = score,
                    category = category
                )
            )
            val entity = QuizResultEntity(
                id = response.result.id,
                userId = userId,
                category = response.result.category,
                score = response.result.score,
                playedAt = response.result.playedAt,
                ranking = response.ranking
            )
            movieDao.upsertQuizResults(listOf(entity))
            QuizSubmissionResult(
                result = entity.toDomain(),
                ranking = response.ranking
            )
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }.getOrThrow()
    }

    override suspend fun getLeaderboard(category: Int, page: Int, pageSize: Int): List<LeaderboardEntry> {
        return runCatching {
            showtimeUserApi.getLeaderboard(
                category = category,
                page = page,
                pageSize = pageSize
            ).items.map { it.toDomain() }
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }.getOrThrow()
    }

    companion object {
        val DEFAULT_FILTERS = MovieFilters()
        const val DEFAULT_SECTION_KEY = "default_catalog"
    }
}

internal fun MovieListItemDto.toEntity(): MovieEntity {
    return MovieEntity(
        imdbId = imdbId,
        title = title,
        year = year,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        posterPath = posterPath,
        backdropPath = backdropPath,
        genresCsv = genres.orEmpty().joinToString("|") { it.name },
        overview = null,
        runtime = null,
        releaseDate = null,
        tagline = null,
        homepage = null
    )
}

internal fun MovieDetailDto.toEntity(existing: MovieEntity?): MovieEntity {
    return MovieEntity(
        imdbId = imdbId,
        title = title,
        year = year ?: existing?.year,
        imdbRating = imdbRating ?: existing?.imdbRating,
        imdbVotes = imdbVotes ?: existing?.imdbVotes,
        posterPath = posterPath ?: existing?.posterPath,
        backdropPath = backdropPath ?: existing?.backdropPath,
        genresCsv = genres.joinToString("|") { it.name },
        overview = overview,
        runtime = runtime,
        releaseDate = releaseDate,
        tagline = tagline,
        homepage = homepage
    )
}

internal fun MovieEntity.toSummary(): MovieSummary {
    return MovieSummary(
        id = imdbId,
        title = title,
        year = year,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        posterUrl = posterPath.asPosterUrl(),
        backdropUrl = backdropPath.asBackdropUrl(),
        genres = genresCsv.splitGenres()
    )
}

internal fun MovieEntity.toDetails(cast: List<CastMemberEntity>): MovieDetails {
    return MovieDetails(
        id = imdbId,
        title = title,
        year = year,
        runtime = runtime,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        posterUrl = posterPath.asPosterUrl(),
        backdropUrl = backdropPath.asBackdropUrl(),
        overview = overview,
        releaseDate = releaseDate,
        tagline = tagline,
        homepage = homepage,
        genres = genresCsv.splitGenres(),
        cast = cast.map { it.toDomain() }
    )
}

internal fun CastMemberEntity.toDomain(): CastMember {
    return CastMember(
        id = personId,
        name = name,
        profileUrl = profilePath.asProfileUrl()
    )
}

internal fun GenreApiModel.toGenre(): Genre {
    return Genre(
        id = id.toString(),
        name = name
    )
}

internal fun QuizResultEntity.toDomain(): QuizHistoryEntry {
    return QuizHistoryEntry(
        id = id,
        category = category,
        score = score,
        playedAt = playedAt,
        ranking = ranking
    )
}

internal fun LeaderboardEntryDto.toDomain(): LeaderboardEntry {
    return LeaderboardEntry(
        rank = rank,
        userId = userId,
        username = username,
        fullName = fullName,
        score = score,
        playedAt = playedAt,
        totalPlays = totalPlays
    )
}

internal fun String?.splitGenres(): List<String> {
    if (this.isNullOrBlank()) {
        return emptyList()
    }
    return split("|").filter { it.isNotBlank() }
}

internal fun String?.asPosterUrl(): String? {
    return this?.let { "https://image.tmdb.org/t/p/w342$it" }
}

internal fun String?.asBackdropUrl(): String? {
    return this?.let { "https://image.tmdb.org/t/p/w780$it" }
}

internal fun String?.asProfileUrl(): String? {
    return this?.let { "https://image.tmdb.org/t/p/w185$it" }
}

internal fun MovieSortOption.toApiSortBy(): String {
    return when (this) {
        MovieSortOption.RatingDesc,
        MovieSortOption.RatingAsc -> "imdb_rating"
        MovieSortOption.YearDesc,
        MovieSortOption.YearAsc -> "year"
        MovieSortOption.TitleDesc,
        MovieSortOption.TitleAsc -> "title"
    }
}

internal fun MovieSortOption.toApiSortOrder(): String {
    return when (this) {
        MovieSortOption.RatingDesc,
        MovieSortOption.YearDesc,
        MovieSortOption.TitleDesc -> "desc"
        MovieSortOption.RatingAsc,
        MovieSortOption.YearAsc,
        MovieSortOption.TitleAsc -> "asc"
    }
}
