package com.example.showtime.movies.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query(
        """
        SELECT movies.* FROM movies
        INNER JOIN catalog_entries ON catalog_entries.movieId = movies.imdbId
        WHERE catalog_entries.sectionKey = :sectionKey AND catalog_entries.page = :page
        ORDER BY catalog_entries.indexInPage ASC
        """
    )
    suspend fun getCatalogPage(
        sectionKey: String,
        page: Int
    ): List<MovieEntity>

    @Query(
        """
        SELECT COUNT(*) FROM catalog_entries
        WHERE sectionKey = :sectionKey AND page = :page
        """
    )
    suspend fun getCatalogPageCount(
        sectionKey: String,
        page: Int
    ): Int

    @Query("SELECT * FROM catalog_sections WHERE sectionKey = :sectionKey")
    suspend fun getCatalogSection(sectionKey: String): CatalogSectionEntity?

    @Upsert
    suspend fun upsertCatalogSection(section: CatalogSectionEntity)

    @Query("DELETE FROM catalog_entries WHERE sectionKey = :sectionKey AND page = :page")
    suspend fun deleteCatalogEntriesForPage(sectionKey: String, page: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogEntries(entries: List<CatalogEntryEntity>)

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Query("SELECT * FROM movies WHERE imdbId = :movieId")
    fun observeMovie(movieId: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE imdbId = :movieId")
    suspend fun getMovie(movieId: String): MovieEntity?

    @Query("SELECT * FROM cast_members WHERE movieId = :movieId ORDER BY orderIndex ASC")
    fun observeCast(movieId: String): Flow<List<CastMemberEntity>>

    @Query("SELECT * FROM cast_members WHERE movieId = :movieId ORDER BY orderIndex ASC")
    suspend fun getCast(movieId: String): List<CastMemberEntity>

    @Query("DELETE FROM cast_members WHERE movieId = :movieId")
    suspend fun deleteCastForMovie(movieId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCast(members: List<CastMemberEntity>)

    @Query(
        """
        SELECT movies.* FROM movies
        INNER JOIN favorites ON favorites.movieId = movies.imdbId
        WHERE favorites.userId = :userId
        ORDER BY favorites.createdAt DESC
        """
    )
    fun observeFavorites(userId: Int): Flow<List<MovieEntity>>

    @Query(
        """
        SELECT movies.* FROM movies
        INNER JOIN watchlist ON watchlist.movieId = movies.imdbId
        WHERE watchlist.userId = :userId
        ORDER BY watchlist.createdAt DESC
        """
    )
    fun observeWatchlist(userId: Int): Flow<List<MovieEntity>>

    @Query("SELECT movieId FROM favorites WHERE userId = :userId")
    fun observeFavoriteIds(userId: Int): Flow<List<String>>

    @Query("SELECT movieId FROM watchlist WHERE userId = :userId")
    fun observeWatchlistIds(userId: Int): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    fun observeFavoriteCount(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM watchlist WHERE userId = :userId")
    fun observeWatchlistCount(userId: Int): Flow<Int>

    @Upsert
    suspend fun upsertFavorites(favorites: List<FavoriteEntity>)

    @Upsert
    suspend fun upsertWatchlist(watchlist: List<WatchlistEntity>)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteFavoritesForUser(userId: Int)

    @Query("DELETE FROM watchlist WHERE userId = :userId")
    suspend fun deleteWatchlistForUser(userId: Int)

    @Query("DELETE FROM favorites WHERE userId = :userId AND movieId = :movieId")
    suspend fun deleteFavorite(userId: Int, movieId: String)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND movieId = :movieId")
    suspend fun deleteWatchlistItem(userId: Int, movieId: String)

    @Query("SELECT COUNT(*) FROM movies WHERE posterPath IS NOT NULL OR backdropPath IS NOT NULL")
    suspend fun countQuizMovieCandidates(): Int

    @Query(
        """
        SELECT * FROM movies
        WHERE (posterPath IS NOT NULL OR backdropPath IS NOT NULL)
        ORDER BY imdbVotes DESC
        LIMIT :limit
        """
    )
    suspend fun getTopQuizCandidates(limit: Int): List<MovieEntity>

    @Query(
        """
        SELECT * FROM movies
        WHERE imdbId NOT IN (:excludedIds)
            AND (posterPath IS NOT NULL OR backdropPath IS NOT NULL)
        ORDER BY imdbVotes DESC
        LIMIT :limit
        """
    )
    suspend fun getTopQuizCandidatesExcluding(
        excludedIds: List<String>,
        limit: Int
    ): List<MovieEntity>

    @Query("SELECT MAX(score) FROM quiz_results WHERE userId = :userId")
    fun observeBestQuizScore(userId: Int): Flow<Float?>

    @Query("SELECT COUNT(*) FROM quiz_results WHERE userId = :userId")
    fun observeQuizPlayCount(userId: Int): Flow<Int>

    @Query(
        """
        SELECT * FROM quiz_results
        WHERE userId = :userId
        ORDER BY playedAt DESC
        LIMIT :limit
        """
    )
    fun observeQuizResults(
        userId: Int,
        limit: Int
    ): Flow<List<QuizResultEntity>>

    @Upsert
    suspend fun upsertQuizResults(results: List<QuizResultEntity>)

    @Query("DELETE FROM quiz_results WHERE userId = :userId")
    suspend fun deleteQuizResultsForUser(userId: Int)

    @Transaction
    suspend fun replaceCatalogPage(
        section: CatalogSectionEntity,
        movies: List<MovieEntity>,
        entries: List<CatalogEntryEntity>
    ) {
        upsertMovies(movies)
        upsertCatalogSection(section)
        deleteCatalogEntriesForPage(section.sectionKey, entries.firstOrNull()?.page ?: 1)
        if (entries.isNotEmpty()) {
            insertCatalogEntries(entries)
        }
    }

    @Transaction
    suspend fun replaceMovieDetails(
        movie: MovieEntity,
        cast: List<CastMemberEntity>
    ) {
        upsertMovies(listOf(movie))
        deleteCastForMovie(movie.imdbId)
        if (cast.isNotEmpty()) {
            insertCast(cast)
        }
    }

    @Transaction
    suspend fun replaceFavorites(
        userId: Int,
        favorites: List<FavoriteEntity>
    ) {
        deleteFavoritesForUser(userId)
        if (favorites.isNotEmpty()) {
            upsertFavorites(favorites)
        }
    }

    @Transaction
    suspend fun replaceWatchlist(
        userId: Int,
        watchlist: List<WatchlistEntity>
    ) {
        deleteWatchlistForUser(userId)
        if (watchlist.isNotEmpty()) {
            upsertWatchlist(watchlist)
        }
    }

    @Transaction
    suspend fun clearUserScopedData(userId: Int) {
        deleteFavoritesForUser(userId)
        deleteWatchlistForUser(userId)
        deleteQuizResultsForUser(userId)
    }
}
