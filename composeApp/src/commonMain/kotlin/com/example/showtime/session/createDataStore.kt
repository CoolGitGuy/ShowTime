package com.example.showtime.session

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences

internal const val dataStoreFileName = "showtime_session.preferences_pb"

fun createDataStore(
    storage: Storage<Preferences>
): DataStore<Preferences> = DataStoreFactory.create(storage = storage)
