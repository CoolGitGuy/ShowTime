package com.example.showtime.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageProbeDao {
    @Query("SELECT * FROM storage_probe ORDER BY `key`")
    fun observeAll(): Flow<List<StorageProbeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StorageProbeEntity)
}
