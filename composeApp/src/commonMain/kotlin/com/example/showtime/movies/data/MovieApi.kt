package com.example.showtime.movies.data

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface MovieApi {
    @GET("movies")
    suspend fun getMovies(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("query") query: String? = null,
        @Query("genre_id") genreId: String? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null,
        @Query("sort_by") sortBy: String = "imdb_rating",
        @Query("sort_order") sortOrder: String = "desc"
    ): PaginatedResponse<MovieListItemDto>

    @GET("movies/{id}")
    suspend fun getMovieById(@Path("id") id: String): MovieDetailDto

    @GET("movies/{id}/cast")
    suspend fun getMovieCast(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): PaginatedResponse<PersonSummaryDto>

    @GET("genres")
    suspend fun getGenres(): List<GenreApiModel>
}
