package com.example.showtime.profile

import com.example.showtime.auth.AuthRepository
import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.session.SessionStorage
import kotlinx.coroutines.flow.collectLatest

class ProfileViewModel(
    private val sessionStorage: SessionStorage,
    private val authRepository: AuthRepository
) : StoreViewModel<ProfileContract.State, ProfileContract.Intent, ProfileContract.Effect>(
    initialState = ProfileContract.State()
) {
    init {
        launch {
            sessionStorage.observeSession().collectLatest { session ->
                updateState { state ->
                    state.copy(
                        session = session,
                        errorMessage = null
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: ProfileContract.Intent) {
        when (intent) {
            ProfileContract.Intent.Logout -> logout()
            ProfileContract.Intent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        launch {
            updateState { state -> state.copy(isRefreshing = true, errorMessage = null) }
            runCatching { authRepository.refreshProfile() }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(
                            isRefreshing = false,
                            errorMessage = throwable.message ?: "Profile refresh failed."
                        )
                    }
                }
                .onSuccess {
                    updateState { state -> state.copy(isRefreshing = false, errorMessage = null) }
                }
        }
    }

    private fun logout() {
        launch {
            updateState { state -> state.copy(isRefreshing = true, errorMessage = null) }
            runCatching { authRepository.logout() }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(
                            isRefreshing = false,
                            errorMessage = throwable.message ?: "Logout failed."
                        )
                    }
                }
        }
    }
}
