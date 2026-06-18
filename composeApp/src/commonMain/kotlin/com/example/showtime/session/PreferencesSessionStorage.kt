package com.example.showtime.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

private val authTokenKey = stringPreferencesKey("auth_token")
private val userIdKey = intPreferencesKey("user_id")
private val usernameKey = stringPreferencesKey("username")
private val fullNameKey = stringPreferencesKey("full_name")

class PreferencesSessionStorage(
    private val dataStore: DataStore<Preferences>
) : SessionStorage {
    private val scope = CoroutineScope(Dispatchers.Default)

    override val session: StateFlow<UserSession?> = dataStore.data
        .map { preferences ->
            val token = preferences[authTokenKey]
            val userId = preferences[userIdKey]
            val username = preferences[usernameKey]
            val fullName = preferences[fullNameKey]

            if (token.isNullOrBlank() || userId == null || username.isNullOrBlank() || fullName.isNullOrBlank()) {
                null
            } else {
                UserSession(
                    userId = userId,
                    username = username,
                    fullName = fullName,
                    accessToken = token
                )
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { dataStore.data.first().toSessionOrNull() }
        )

    override fun observeToken(): Flow<String?> {
        return observeSession().map { it?.accessToken }
    }

    override suspend fun awaitInitialSession(): UserSession? {
        return dataStore.data.first().toSessionOrNull()
    }

    override suspend fun saveSession(session: UserSession) {
        dataStore.edit { preferences ->
            preferences[authTokenKey] = session.accessToken
            preferences[userIdKey] = session.userId
            preferences[usernameKey] = session.username
            preferences[fullNameKey] = session.fullName
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(authTokenKey)
            preferences.remove(userIdKey)
            preferences.remove(usernameKey)
            preferences.remove(fullNameKey)
        }
    }
}

private fun Preferences.toSessionOrNull(): UserSession? {
    val token = this[authTokenKey]
    val userId = this[userIdKey]
    val username = this[usernameKey]
    val fullName = this[fullNameKey]

    if (token.isNullOrBlank() || userId == null || username.isNullOrBlank() || fullName.isNullOrBlank()) {
        return null
    }

    return UserSession(
        userId = userId,
        username = username,
        fullName = fullName,
        accessToken = token
    )
}
