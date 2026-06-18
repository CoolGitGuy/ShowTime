package com.example.showtime.database

import androidx.room.RoomDatabaseConstructor

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase {
        // Keep the iOS Room entrypoint explicit until the shared data layer starts using it.
        error("Room iOS constructor is not wired yet.")
    }
}
