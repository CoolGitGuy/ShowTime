package com.example.showtime.auth

import com.example.showtime.session.SessionStorage
import com.example.showtime.session.UserSession

class SessionCoordinator(
    private val sessionStorage: SessionStorage,
    private val userDataCleaner: UserDataCleaner?
) {
    suspend fun saveAuthResponse(response: AuthResponse) {
        sessionStorage.saveSession(
            UserSession(
                userId = response.user.id,
                username = response.user.username,
                fullName = response.user.fullName,
                accessToken = response.accessToken
            )
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val currentSession = sessionStorage.session.value ?: return
        sessionStorage.saveSession(
            currentSession.copy(
                username = profile.username,
                fullName = profile.fullName
            )
        )
    }

    suspend fun logout() {
        sessionStorage.session.value?.userId?.let { userId ->
            userDataCleaner?.clearUserData(userId)
        }
        sessionStorage.clearSession()
    }
}
