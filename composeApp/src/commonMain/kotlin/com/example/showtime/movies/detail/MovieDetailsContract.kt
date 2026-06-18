package com.example.showtime.movies.detail

import com.example.showtime.core.mvi.UiEffect
import com.example.showtime.core.mvi.UiIntent
import com.example.showtime.core.mvi.UiState
import com.example.showtime.core.ui.AsyncData
import com.example.showtime.movies.domain.MovieDetails

object MovieDetailsContract {
    data class State(
        val movie: AsyncData<MovieDetails> = AsyncData.Uninitialized,
        val isFavorite: Boolean = false,
        val isWatchlist: Boolean = false,
        val errorMessage: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Refresh : Intent
        data object ToggleFavorite : Intent
        data object ToggleWatchlist : Intent
    }

    sealed interface Effect : UiEffect
}
