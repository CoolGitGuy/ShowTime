package com.example.showtime.movies.data

import com.example.showtime.auth.UserDataCleaner
import com.example.showtime.movies.db.MovieDao

class LocalUserDataCleaner(
    private val movieDao: MovieDao
) : UserDataCleaner {
    override suspend fun clearUserData(userId: Int) {
        movieDao.clearUserScopedData(userId)
    }
}
