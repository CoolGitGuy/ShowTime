package com.example.showtime.profile

import com.example.showtime.core.mvi.UiEffect
import com.example.showtime.core.mvi.UiIntent
import com.example.showtime.core.mvi.UiState
import com.example.showtime.session.UserSession

object ProfileContract {
    data class State(
        val session: UserSession? = null,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Refresh : Intent
        data object Logout : Intent
    }

    sealed interface Effect : UiEffect
}
