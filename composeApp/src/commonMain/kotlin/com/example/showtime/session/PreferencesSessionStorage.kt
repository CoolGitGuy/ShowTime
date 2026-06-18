package com.example.showtime.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val authTokenKey = stringPreferencesKey("auth_token")

class PreferencesSessionStorage(
    private val dataStore: DataStore<Preferences>
) : SessionStorage {
    override fun observeToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[authTokenKey]
        }
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[authTokenKey] = token
        }
    }

    override suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(authTokenKey)
        }
    }
}
