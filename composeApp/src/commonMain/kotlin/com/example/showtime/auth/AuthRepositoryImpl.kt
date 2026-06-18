package com.example.showtime.auth

import com.example.showtime.networking.toApiException

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val sessionCoordinator: SessionCoordinator
) : AuthRepository {
    override suspend fun login(username: String, password: String) {
        runCatching {
            authApi.login(LoginRequest(username = username, password = password))
        }.onSuccess { response ->
            sessionCoordinator.saveAuthResponse(response)
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }
    }

    override suspend fun signUp(fullName: String, username: String, password: String) {
        runCatching {
            authApi.signUp(
                SignUpRequest(
                    fullName = fullName,
                    username = username,
                    password = password
                )
            )
        }.onSuccess { response ->
            sessionCoordinator.saveAuthResponse(response)
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }
    }

    override suspend fun refreshProfile(): UserProfile {
        return runCatching {
            authApi.getMe().toProfile()
        }.onSuccess { profile ->
            sessionCoordinator.saveUserProfile(profile)
        }.onFailure { throwable ->
            throw throwable.toApiException()
        }.getOrThrow()
    }

    override suspend fun logout() {
        sessionCoordinator.logout()
    }
}

private fun UserDto.toProfile(): UserProfile {
    return UserProfile(
        id = id,
        username = username,
        fullName = fullName
    )
}
