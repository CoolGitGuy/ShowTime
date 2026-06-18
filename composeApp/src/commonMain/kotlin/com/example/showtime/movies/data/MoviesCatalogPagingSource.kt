package com.example.showtime.movies.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.showtime.movies.db.CatalogEntryEntity
import com.example.showtime.movies.db.CatalogSectionEntity
import com.example.showtime.movies.db.MovieDao
import com.example.showtime.movies.domain.MovieFilters
import com.example.showtime.movies.domain.MovieSummary

class MoviesCatalogPagingSource(
    private val movieDao: MovieDao,
    private val movieApi: MovieApi,
    private val filters: MovieFilters
) : PagingSource<Int, MovieSummary>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieSummary> {
        val page = params.key ?: 1
        val sectionKey = filters.sectionKey

        return runCatching {
            val cachedCount = movieDao.getCatalogPageCount(sectionKey, page)
            if (cachedCount == 0) {
                val response = movieApi.getMovies(
                    page = page,
                    pageSize = PAGE_SIZE,
                    query = filters.query.ifBlank { null },
                    genreId = filters.genreId,
                    minYear = filters.minYear,
                    maxYear = filters.maxYear,
                    minRating = filters.minRating,
                    sortBy = filters.sortOption.toApiSortBy(),
                    sortOrder = filters.sortOption.toApiSortOrder()
                )

                val movies = response.items.map { it.toEntity() }
                val entries = response.items.mapIndexed { index, movie ->
                    CatalogEntryEntity(
                        sectionKey = sectionKey,
                        movieId = movie.imdbId,
                        page = page,
                        indexInPage = index
                    )
                }

                movieDao.replaceCatalogPage(
                    section = CatalogSectionEntity(
                        sectionKey = sectionKey,
                        totalPages = response.totalPages,
                        totalItems = response.totalItems,
                        pageSize = response.pageSize
                    ),
                    movies = movies,
                    entries = entries
                )
            }

            val section = movieDao.getCatalogSection(sectionKey)
            val rows = movieDao.getCatalogPage(sectionKey, page)
            LoadResult.Page(
                data = rows.map { it.toSummary() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (section != null && page < section.totalPages) page + 1 else null
            )
        }.getOrElse { throwable ->
            LoadResult.Error(throwable)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieSummary>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition)
        return anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
