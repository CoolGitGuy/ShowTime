package com.example.showtime.movies.data

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface ShowtimeUserApi {
    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieListItemDto>

    @POST("me/favorites/{movieId}")
    suspend fun addFavorite(@Path("movieId") movieId: String)

    @DELETE("me/favorites/{movieId}")
    suspend fun removeFavorite(@Path("movieId") movieId: String)

    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieListItemDto>

    @POST("me/watchlist/{movieId}")
    suspend fun addWatchlistItem(@Path("movieId") movieId: String)

    @DELETE("me/watchlist/{movieId}")
    suspend fun removeWatchlistItem(@Path("movieId") movieId: String)

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("category") category: Int = 1,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): PaginatedResponse<LeaderboardEntryDto>

    @POST("leaderboard")
    suspend fun postQuizResult(
        @Body request: PostQuizResultRequest
    ): PostQuizResultResponse

    @GET("me/quiz-results")
    suspend fun getMyQuizResults(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): PaginatedResponse<QuizResultDto>
}
