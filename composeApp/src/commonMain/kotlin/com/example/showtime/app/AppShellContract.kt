package com.example.showtime.app

import com.example.showtime.core.mvi.UiEffect
import com.example.showtime.core.mvi.UiIntent
import com.example.showtime.core.mvi.UiState

object AppShellContract {
    data class State(
        val destination: Destination = Destination.Splash
    ) : UiState

    sealed interface Intent : UiIntent

    sealed interface Effect : UiEffect
}

enum class Destination {
    Splash,
    Auth,
    Main
}
