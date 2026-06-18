package com.example.showtime.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.example.showtime.movies.db.CatalogEntryEntity
import com.example.showtime.movies.db.CatalogSectionEntity
import com.example.showtime.movies.db.CastMemberEntity
import com.example.showtime.movies.db.FavoriteEntity
import com.example.showtime.movies.db.MovieDao
import com.example.showtime.movies.db.MovieEntity
import com.example.showtime.movies.db.QuizResultEntity
import com.example.showtime.movies.db.WatchlistEntity

@Database(
    entities = [
        StorageProbeEntity::class,
        MovieEntity::class,
        CatalogSectionEntity::class,
        CatalogEntryEntity::class,
        CastMemberEntity::class,
        FavoriteEntity::class,
        WatchlistEntity::class,
        QuizResultEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storageProbeDao(): StorageProbeDao
    abstract fun movieDao(): MovieDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
