package com.example.showtime.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_probe")
data class StorageProbeEntity(
    @PrimaryKey val key: String,
    val value: String
)
