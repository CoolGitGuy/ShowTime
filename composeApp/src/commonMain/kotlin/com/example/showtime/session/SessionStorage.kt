package com.example.showtime.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionStorage {
    val session: StateFlow<UserSession?>

    fun observeSession(): Flow<UserSession?> = session

    fun observeToken(): Flow<String?>

    suspend fun awaitInitialSession(): UserSession?

    suspend fun saveSession(session: UserSession)

    suspend fun clearSession()
}
