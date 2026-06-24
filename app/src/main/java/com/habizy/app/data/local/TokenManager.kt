package com.habizy.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habizy.app.data.model.AuthResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val PROFILE_COMPLETED = booleanPreferencesKey("profile_completed")
        private val COLOCATION_ID = stringPreferencesKey("colocation_id")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    // -- Observable flows --

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN] != null
    }

    val profileCompleted: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[PROFILE_COMPLETED]
    }

    val colocationIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[COLOCATION_ID]
    }

    // -- Synchronous-style reads (suspend) --

    suspend fun getAccessToken(): String? =
        context.dataStore.data.first()[ACCESS_TOKEN]

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.first()[REFRESH_TOKEN]

    suspend fun getColocationId(): String? =
        context.dataStore.data.first()[COLOCATION_ID]

    suspend fun getProfileCompleted(): Boolean? =
        context.dataStore.data.first()[PROFILE_COMPLETED]

    suspend fun getFcmToken(): String? =
        context.dataStore.data.first()[FCM_TOKEN]

    // -- Writes --

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveAuthResponse(response: AuthResponse) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = response.accessToken
            prefs[REFRESH_TOKEN] = response.refreshToken
            if (response.profileCompleted != null) {
                prefs[PROFILE_COMPLETED] = response.profileCompleted
            }
        }
    }

    suspend fun saveProfileCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PROFILE_COMPLETED] = completed
        }
    }

    suspend fun setAccessToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = token
        }
    }

    suspend fun saveColocationId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[COLOCATION_ID] = id
        }
    }

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[FCM_TOKEN] = token
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
            prefs.remove(PROFILE_COMPLETED)
            prefs.remove(COLOCATION_ID)
        }
    }
}
