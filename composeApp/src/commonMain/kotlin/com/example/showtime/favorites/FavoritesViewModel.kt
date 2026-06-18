package com.example.showtime.favorites

import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.movies.data.MovieRepository
import com.example.showtime.session.SessionStorage
import kotlinx.coroutines.flow.collectLatest

class FavoritesViewModel(
    private val sessionStorage: SessionStorage,
    private val movieRepository: MovieRepository
) : StoreViewModel<FavoritesContract.State, FavoritesContract.Intent, FavoritesContract.Effect>(
    initialState = FavoritesContract.State()
) {
    init {
        launch {
            sessionStorage.observeSession().collectLatest { session ->
                if (session == null) {
                    updateState { state -> state.copy(movies = emptyList(), isLoading = false) }
                    return@collectLatest
                }

                launch {
                    movieRepository.observeFavorites(session.userId).collectLatest { movies ->
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

    override fun handleIntent(intent: FavoritesContract.Intent) {
        when (intent) {
            FavoritesContract.Intent.Refresh -> refresh()
            is FavoritesContract.Intent.Remove -> remove(intent.movieId)
        }
    }

    private fun refresh() {
        val userId = sessionStorage.session.value?.userId ?: return
        launch {
            updateState { state -> state.copy(isLoading = true, errorMessage = null) }
            runCatching { movieRepository.refreshFavorites(userId) }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Favorites failed to sync."
                        )
                    }
                }
        }
    }

    private fun remove(movieId: String) {
        val userId = sessionStorage.session.value?.userId ?: return
        launch {
            runCatching { movieRepository.setFavorite(userId, movieId, false) }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(errorMessage = throwable.message ?: "Favorite removal failed.")
                    }
                }
        }
    }
}
