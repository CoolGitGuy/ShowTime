package com.example.showtime.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

fun createDataStore(
    context: Context
): DataStore<Preferences> = PreferenceDataStoreFactory.create(
    produceFile = { context.filesDir.resolve(dataStoreFileName) }
)
