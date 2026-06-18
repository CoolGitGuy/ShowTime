package com.example.showtime.session

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    fun observeToken(): Flow<String?>
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}
