package com.example.showtime.auth

interface UserDataCleaner {
    suspend fun clearUserData(userId: Int)
}
