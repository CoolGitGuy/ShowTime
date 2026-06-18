package com.example.showtime.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.showtime.database.AppDatabase
import com.example.showtime.database.createAppDatabase
import com.example.showtime.database.getDatabaseBuilder
import com.example.showtime.session.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformStorageModules(): List<Module> {
    return listOf(
        module {
            single<DataStore<Preferences>> {
                createDataStore()
            }

            single<AppDatabase> {
                createAppDatabase(
                    builder = getDatabaseBuilder()
                )
            }
        }
    )
}
