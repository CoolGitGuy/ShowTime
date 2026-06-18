package com.example.showtime.movies.list

import androidx.paging.PagingData
import com.example.showtime.core.mvi.UiEffect
import com.example.showtime.core.mvi.UiIntent
import com.example.showtime.core.mvi.UiState
import com.example.showtime.movies.domain.Genre
import com.example.showtime.movies.domain.MovieFilters
import com.example.showtime.movies.domain.MovieSortOption
import com.example.showtime.movies.domain.MovieSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object MoviesCatalogContract {
    data class State(
        val pagedMovies: Flow<PagingData<MovieSummary>> = emptyFlow(),
        val genres: List<Genre> = emptyList(),
        val isLoadingGenres: Boolean = false,
        val isFiltersExpanded: Boolean = false,
        val errorMessage: String? = null,
        val filters: MovieFilters = MovieFilters()
    ) : UiState

    sealed interface Intent : UiIntent {
        data object ToggleFilters : Intent
        data class QueryChanged(val value: String) : Intent
        data class GenreChanged(val genreId: String?) : Intent
        data class MinYearChanged(val value: String) : Intent
        data class MaxYearChanged(val value: String) : Intent
        data class MinRatingChanged(val value: String) : Intent
        data class SortChanged(val option: MovieSortOption) : Intent
        data object ApplyFilters : Intent
        data object ClearFilters : Intent
        data object RefreshGenres : Intent
    }

    sealed interface Effect : UiEffect
}
