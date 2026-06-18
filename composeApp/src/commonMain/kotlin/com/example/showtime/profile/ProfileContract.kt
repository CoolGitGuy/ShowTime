package com.example.showtime.profile

import com.example.showtime.core.mvi.UiEffect
import com.example.showtime.core.mvi.UiIntent
import com.example.showtime.core.mvi.UiState
import com.example.showtime.movies.domain.QuizHistoryEntry
import com.example.showtime.session.UserSession

object ProfileContract {
    data class State(
        val session: UserSession? = null,
        val isRefreshing: Boolean = false,
        val isLoggingOut: Boolean = false,
        val favoriteCount: Int = 0,
        val watchlistCount: Int = 0,
        val bestQuizScore: Float? = null,
        val quizPlayCount: Int = 0,
        val recentResults: List<QuizHistoryEntry> = emptyList(),
        val errorMessage: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Refresh : Intent
        data object Logout : Intent
    }

    sealed interface Effect : UiEffect
}
