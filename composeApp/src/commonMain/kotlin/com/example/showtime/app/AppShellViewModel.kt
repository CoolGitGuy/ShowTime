package com.example.showtime.app

import com.example.showtime.auth.AuthRepository
import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.session.SessionStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged

class AppShellViewModel(
    private val sessionStorage: SessionStorage,
    private val authRepository: AuthRepository
) : StoreViewModel<AppShellContract.State, AppShellContract.Intent, AppShellContract.Effect>(
    initialState = AppShellContract.State()
) {
    init {
        launch {
            val initialSession = sessionStorage.awaitInitialSession()
            if (initialSession != null) {
                runCatching { authRepository.refreshProfile() }
            }

            sessionStorage.observeToken()
                .distinctUntilChanged()
                .collectLatest { token ->
                    val destination = if (token.isNullOrBlank()) {
                        Destination.Auth
                    } else {
                        Destination.Main
                    }

                    updateState { state ->
                        state.copy(
                            destination = destination,
                            isBootstrapping = false
                        )
                    }
                }
        }
    }

    override fun handleIntent(intent: AppShellContract.Intent) = Unit
}
