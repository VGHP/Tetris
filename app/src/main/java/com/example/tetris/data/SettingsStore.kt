package com.example.tetris.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "tetris_settings")

class SettingsStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val HIGH_SCORE_KEY = intPreferencesKey("high_score")
        val UNLOCKED_ACHIEVEMENTS_KEY = stringSetPreferencesKey("unlocked_achievements")
        val VOLUME_KEY = floatPreferencesKey("volume")
        val THEME_KEY = stringPreferencesKey("theme")
        val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    val highScoreFlow: Flow<Int> = dataStore.data.map { it[HIGH_SCORE_KEY] ?: 0 }
    val unlockedAchievementsFlow: Flow<Set<String>> = dataStore.data.map { it[UNLOCKED_ACHIEVEMENTS_KEY] ?: emptySet() }

    val volumeFlow: Flow<Float> = dataStore.data.map { it[VOLUME_KEY] ?: 1.0f }
    // дефолтная тема Classic
    val themeFlow: Flow<String> = dataStore.data.map { it[THEME_KEY] ?: "NEON" }
    val languageFlow: Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: "EN" }

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { pref ->
            val current = pref[HIGH_SCORE_KEY] ?: 0
            if (score > current) pref[HIGH_SCORE_KEY] = score
        }
    }

    suspend fun saveUnlockedAchievement(id: Int) {
        dataStore.edit { pref ->
            val currentSet = pref[UNLOCKED_ACHIEVEMENTS_KEY] ?: emptySet()
            pref[UNLOCKED_ACHIEVEMENTS_KEY] = currentSet + id.toString()
        }
    }

    suspend fun saveVolume(volume: Float) {
        dataStore.edit { it[VOLUME_KEY] = volume }
    }

    suspend fun saveTheme(theme: String) {
        dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun saveLanguage(language: String) {
        dataStore.edit { it[LANGUAGE_KEY] = language }
    }
}