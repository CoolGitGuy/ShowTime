package com.example.showtime.watchlist

import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.movies.data.MovieRepository
import com.example.showtime.session.SessionStorage
import kotlinx.coroutines.flow.collectLatest

class WatchlistViewModel(
    private val sessionStorage: SessionStorage,
    private val movieRepository: MovieRepository
) : StoreViewModel<WatchlistContract.State, WatchlistContract.Intent, WatchlistContract.Effect>(
    initialState = WatchlistContract.State()
) {
    init {
        launch {
            sessionStorage.observeSession().collectLatest { session ->
                if (session == null) {
                    updateState { state -> state.copy(movies = emptyList(), isLoading = false) }
                    return@collectLatest
                }

                launch {
                    movieRepository.observeWatchlist(session.userId).collectLatest { movies ->
                        updateState { state ->
                            state.copy(
                                movies = movies,
                                isLoading = false
                            )
                        }
                    }
                }

                refresh()
            }
        }
    }

    override fun handleIntent(intent: WatchlistContract.Intent) {
        when (intent) {
            WatchlistContract.Intent.Refresh -> refresh()
            is WatchlistContract.Intent.Remove -> remove(intent.movieId)
        }
    }

    private fun refresh() {
        val userId = sessionStorage.session.value?.userId ?: return
        launch {
            updateState { state -> state.copy(isLoading = true, errorMessage = null) }
            runCatching { movieRepository.refreshWatchlist(userId) }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Watchlist failed to sync."
                        )
                    }
                }
        }
    }

    private fun remove(movieId: String) {
        val userId = sessionStorage.session.value?.userId ?: return
        launch {
            runCatching { movieRepository.setWatchlist(userId, movieId, false) }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(errorMessage = throwable.message ?: "Watchlist removal failed.")
                    }
                }
        }
    }
}
