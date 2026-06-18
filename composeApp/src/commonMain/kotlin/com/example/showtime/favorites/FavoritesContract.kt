package com.example.showtime.favorites

import com.example.showtime.core.mvi.UiEffect
import com.example.showtime.core.mvi.UiIntent
import com.example.showtime.core.mvi.UiState
import com.example.showtime.movies.domain.MovieSummary

object FavoritesContract {
    data class State(
        val movies: List<MovieSummary> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Refresh : Intent
        data class Remove(val movieId: String) : Intent
    }

    sealed interface Effect : UiEffect
}
