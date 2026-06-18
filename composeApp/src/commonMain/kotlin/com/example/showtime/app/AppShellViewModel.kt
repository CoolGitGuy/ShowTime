package com.example.showtime.app

import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.session.SessionStorage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged

class AppShellViewModel(
    private val sessionStorage: SessionStorage
) : StoreViewModel<AppShellContract.State, AppShellContract.Intent, AppShellContract.Effect>(
    initialState = AppShellContract.State()
) {
    init {
        launch {
            sessionStorage.observeToken()
                .distinctUntilChanged()
                .collect { token ->
                    val destination = if (token.isNullOrBlank()) {
                        Destination.Auth
                    } else {
                        Destination.Main
                    }

                    updateState { state ->
                        state.copy(destination = destination)
                    }
                }
        }
    }

    override fun handleIntent(intent: AppShellContract.Intent) = Unit
}
