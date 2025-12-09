package com.example.languagecards.dao

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    companion object {
        private val SELECTED_LANGUAGE_KEY = intPreferencesKey("selected_language")
    }

    val selectedLanguage: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_LANGUAGE_KEY] ?: LanguageType.FRENCH
    }

    suspend fun setSelectedLanguage(language: Int) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE_KEY] = language
        }
    }
}
