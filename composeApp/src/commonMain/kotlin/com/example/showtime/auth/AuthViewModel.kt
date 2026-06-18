package com.example.showtime.auth

import com.example.showtime.core.mvi.StoreViewModel

class AuthViewModel(
    private val authRepository: AuthRepository
) : StoreViewModel<AuthContract.State, AuthContract.Intent, AuthContract.Effect>(
    initialState = AuthContract.State()
) {
    override fun handleIntent(intent: AuthContract.Intent) {
        when (intent) {
            is AuthContract.Intent.ModeChanged -> {
                updateState { state ->
                    state.copy(
                        mode = intent.mode,
                        fullName = "",
                        username = "",
                        password = "",
                        errorMessage = null
                    )
                }
            }

            is AuthContract.Intent.FullNameChanged -> {
                updateState { state -> state.copy(fullName = intent.value, errorMessage = null) }
            }

            is AuthContract.Intent.UsernameChanged -> {
                updateState { state -> state.copy(username = intent.value, errorMessage = null) }
            }

            is AuthContract.Intent.PasswordChanged -> {
                updateState { state -> state.copy(password = intent.value, errorMessage = null) }
            }

            AuthContract.Intent.Submit -> submit()
        }
    }

    private fun submit() {
        val validationError = validate(currentState)
        if (validationError != null) {
            updateState { state -> state.copy(errorMessage = validationError) }
            return
        }

        launch {
            updateState { state -> state.copy(isLoading = true, errorMessage = null) }

            runCatching {
                when (currentState.mode) {
                    AuthContract.Mode.Login -> {
                        authRepository.login(
                            username = currentState.username.trim(),
                            password = currentState.password
                        )
                    }

                    AuthContract.Mode.SignUp -> {
                        authRepository.signUp(
                            fullName = currentState.fullName.trim(),
                            username = currentState.username.trim(),
                            password = currentState.password
                        )
                    }
                }
            }.onSuccess {
                updateState { state ->
                    state.copy(
                        isLoading = false,
                        password = "",
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                updateState { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Authentication failed."
                    )
                }
            }
        }
    }
}

private fun validate(state: AuthContract.State): String? {
    if (state.mode == AuthContract.Mode.SignUp && state.fullName.isBlank()) {
        return "Full name is required."
    }

    val username = state.username.trim()
    if (!username.matches(Regex("^[A-Za-z0-9_]{3,}$"))) {
        return "Username must have at least 3 characters and use only letters, digits and underscore."
    }

    if (state.password.length < 8) {
        return "Password must have at least 8 characters."
    }

    return null
}
