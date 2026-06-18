package com.example.showtime.movies.detail

import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.core.ui.AsyncData
import com.example.showtime.movies.data.MovieRepository
import com.example.showtime.session.SessionStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

class MovieDetailsViewModel(
    private val movieId: String,
    private val movieRepository: MovieRepository,
    private val sessionStorage: SessionStorage
) : StoreViewModel<MovieDetailsContract.State, MovieDetailsContract.Intent, MovieDetailsContract.Effect>(
    initialState = MovieDetailsContract.State()
) {
    init {
        launch {
            combine(
                movieRepository.observeMovie(movieId),
                sessionStorage.observeSession()
            ) { movie, session ->
                val userId = session?.userId
                val favoriteIds = if (userId != null) {
                    movieRepository.observeFavoriteIds(userId)
                } else {
                    kotlinx.coroutines.flow.flowOf(emptySet())
                }
                val watchlistIds = if (userId != null) {
                    movieRepository.observeWatchlistIds(userId)
                } else {
                    kotlinx.coroutines.flow.flowOf(emptySet())
                }
                Triple(movie, favoriteIds, watchlistIds)
            }.collectLatest { (movie, favoriteFlow, watchlistFlow) ->
                combine(favoriteFlow, watchlistFlow) { favoriteIds, watchlistIds ->
                    Triple(movie, favoriteIds, watchlistIds)
                }.collectLatest { (details, favoriteIds, watchlistIds) ->
                    updateState { state ->
                        state.copy(
                            movie = details?.let { AsyncData.Data(it) } ?: AsyncData.Loading,
                            isFavorite = favoriteIds.contains(movieId),
                            isWatchlist = watchlistIds.contains(movieId)
                        )
                    }
                }
            }
        }

        refresh()
    }

    override fun handleIntent(intent: MovieDetailsContract.Intent) {
        when (intent) {
            MovieDetailsContract.Intent.Refresh -> refresh()
            MovieDetailsContract.Intent.ToggleFavorite -> toggleFavorite()
            MovieDetailsContract.Intent.ToggleWatchlist -> toggleWatchlist()
        }
    }

    private fun refresh() {
        launch {
            updateState { state -> state.copy(movie = AsyncData.Loading, errorMessage = null) }
            runCatching { movieRepository.refreshMovieDetails(movieId) }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(
                            movie = AsyncData.Error(throwable.message, throwable),
                            errorMessage = throwable.message
                        )
                    }
                }
        }
    }

    private fun toggleFavorite() {
        val userId = sessionStorage.session.value?.userId ?: return
        val shouldEnable = !currentState.isFavorite
        launch {
            runCatching {
                movieRepository.setFavorite(userId, movieId, shouldEnable)
            }.onFailure { throwable ->
                updateState { state ->
                    state.copy(errorMessage = throwable.message ?: "Favorite update failed.")
                }
            }
        }
    }

    private fun toggleWatchlist() {
        val userId = sessionStorage.session.value?.userId ?: return
        val shouldEnable = !currentState.isWatchlist
        launch {
            runCatching {
                movieRepository.setWatchlist(userId, movieId, shouldEnable)
            }.onFailure { throwable ->
                updateState { state ->
                    state.copy(errorMessage = throwable.message ?: "Watchlist update failed.")
                }
            }
        }
    }
}
