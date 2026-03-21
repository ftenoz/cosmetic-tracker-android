package com.cosmetictracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_FIRST_NAME_KEY = stringPreferencesKey("user_first_name")
        private val USER_LAST_NAME_KEY = stringPreferencesKey("user_last_name")
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserInfo(
        userId: String,
        email: String,
        firstName: String,
        lastName: String?
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_FIRST_NAME_KEY] = firstName
            lastName?.let { preferences[USER_LAST_NAME_KEY] = it }
        }
    }

    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { it[USER_ID_KEY] }
    }

    fun getUserEmail(): Flow<String?> {
        return context.dataStore.data.map { it[USER_EMAIL_KEY] }
    }

    fun getUserFirstName(): Flow<String?> {
        return context.dataStore.data.map { it[USER_FIRST_NAME_KEY] }
    }

    fun getUserLastName(): Flow<String?> {
        return context.dataStore.data.map { it[USER_LAST_NAME_KEY] }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
