package com.example.showtime.auth

import com.example.showtime.core.mvi.UiEffect
import com.example.showtime.core.mvi.UiIntent
import com.example.showtime.core.mvi.UiState

object AuthContract {
    data class State(
        val mode: Mode = Mode.Login,
        val fullName: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : UiState

    enum class Mode {
        Login,
        SignUp
    }

    sealed interface Intent : UiIntent {
        data class ModeChanged(val mode: Mode) : Intent
        data class FullNameChanged(val value: String) : Intent
        data class UsernameChanged(val value: String) : Intent
        data class PasswordChanged(val value: String) : Intent
        data object Submit : Intent
    }

    sealed interface Effect : UiEffect
}
