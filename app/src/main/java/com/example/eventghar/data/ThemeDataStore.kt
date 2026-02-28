package com.example.eventghar.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemeDataStore {
    // Key is per-UID: "is_dark_mode_<uid>" — falls back to global key for logged-out state
    private fun darkModeKey(uid: String?) =
        booleanPreferencesKey(if (uid.isNullOrBlank()) "is_dark_mode" else "is_dark_mode_$uid")

    fun isDarkModeFlow(context: Context, uid: String?): Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[darkModeKey(uid)] ?: false
        }

    suspend fun setDarkMode(context: Context, uid: String?, isDark: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[darkModeKey(uid)] = isDark
        }
    }
}


