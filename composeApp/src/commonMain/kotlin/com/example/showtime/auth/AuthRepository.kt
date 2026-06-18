package com.example.showtime.auth

interface AuthRepository {
    suspend fun login(username: String, password: String)

    suspend fun signUp(fullName: String, username: String, password: String)

    suspend fun refreshProfile(): UserProfile

    suspend fun logout()
}
