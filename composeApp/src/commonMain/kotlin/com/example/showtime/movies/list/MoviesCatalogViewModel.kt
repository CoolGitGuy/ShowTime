package com.example.showtime.movies.list

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.movies.data.MovieRepository
import com.example.showtime.movies.domain.MovieFilters

class MoviesCatalogViewModel(
    private val movieRepository: MovieRepository
) : StoreViewModel<MoviesCatalogContract.State, MoviesCatalogContract.Intent, MoviesCatalogContract.Effect>(
    initialState = MoviesCatalogContract.State()
) {
    init {
        applyFilters(MovieFilters())
        loadGenres()
        launch {
            runCatching { movieRepository.ensureCatalogBootstrap() }
        }
    }

    override fun handleIntent(intent: MoviesCatalogContract.Intent) {
        when (intent) {
            MoviesCatalogContract.Intent.ToggleFilters -> {
                updateState { state -> state.copy(isFiltersExpanded = !state.isFiltersExpanded) }
            }

            is MoviesCatalogContract.Intent.QueryChanged -> {
                updateState { state ->
                    state.copy(
                        filters = state.filters.copy(query = intent.value),
                        errorMessage = null
                    )
                }
            }

            is MoviesCatalogContract.Intent.GenreChanged -> {
                updateState { state ->
                    state.copy(
                        filters = state.filters.copy(genreId = intent.genreId),
                        errorMessage = null
                    )
                }
            }

            is MoviesCatalogContract.Intent.MinYearChanged -> {
                updateState { state ->
                    state.copy(
                        filters = state.filters.copy(minYear = intent.value.toIntOrNull()),
                        errorMessage = null
                    )
                }
            }

            is MoviesCatalogContract.Intent.MaxYearChanged -> {
                updateState { state ->
                    state.copy(
                        filters = state.filters.copy(maxYear = intent.value.toIntOrNull()),
                        errorMessage = null
                    )
                }
            }

            is MoviesCatalogContract.Intent.MinRatingChanged -> {
                updateState { state ->
                    state.copy(
                        filters = state.filters.copy(minRating = intent.value.toFloatOrNull()),
                        errorMessage = null
                    )
                }
            }

            is MoviesCatalogContract.Intent.SortChanged -> {
                updateState { state ->
                    state.copy(
                        filters = state.filters.copy(sortOption = intent.option),
                        errorMessage = null
                    )
                }
                applyFilters(currentState.filters.copy(sortOption = intent.option))
            }

            MoviesCatalogContract.Intent.ApplyFilters -> {
                applyFilters(currentState.filters)
            }

            MoviesCatalogContract.Intent.ClearFilters -> {
                val filters = MovieFilters()
                updateState { state ->
                    state.copy(
                        filters = filters,
                        errorMessage = null
                    )
                }
                applyFilters(filters)
            }

            MoviesCatalogContract.Intent.RefreshGenres -> loadGenres()
        }
    }

    private fun applyFilters(filters: MovieFilters) {
        updateState { state ->
            state.copy(
                filters = filters,
                isFiltersExpanded = false,
                pagedMovies = movieRepository
                    .pagedMovies(filters)
                    .cachedIn(viewModelScope)
            )
        }
    }

    private fun loadGenres() {
        launch {
            updateState { state -> state.copy(isLoadingGenres = true, errorMessage = null) }
            runCatching { movieRepository.refreshGenres() }
                .onSuccess { genres ->
                    updateState { state ->
                        state.copy(
                            genres = genres,
                            isLoadingGenres = false
                        )
                    }
                }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(
                            isLoadingGenres = false,
                            errorMessage = throwable.message ?: "Genres could not be loaded."
                        )
                    }
                }
        }
    }
}
