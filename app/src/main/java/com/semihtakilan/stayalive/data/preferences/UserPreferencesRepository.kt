package com.semihtakilan.stayalive.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

class UserPreferencesRepository(
    private val context: Context,
    externalScope: CoroutineScope,
) {

    private val dataStore = context.userPrefsDataStore

    /**
     * One DataStore collector for the process; [replay] so new subscribers get the last value immediately.
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map(::mapPreferences)
        .distinctUntilChanged()
        .shareIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            replay = 1,
        )

    /**
     * Single read from disk (bypasses [shareIn]). Used once at process start so the first UI frame matches saved theme/locale.
     */
    suspend fun readFirstSnapshot(): UserPreferences =
        dataStore.data.map(::mapPreferences).first()

    private fun mapPreferences(prefs: Preferences): UserPreferences =
        UserPreferences(
            age = prefs[KEY_AGE] ?: 0,
            weightKg = prefs[KEY_WEIGHT_KG] ?: 0f,
            heightCm = prefs[KEY_HEIGHT_CM] ?: 0f,
            languagePref = prefs[KEY_LANGUAGE] ?: UserPreferences.PREF_LANGUAGE_SYSTEM,
            themePref = prefs[KEY_THEME] ?: UserPreferences.PREF_THEME_SYSTEM,
            unitPref = prefs[KEY_UNITS] ?: UserPreferences.PREF_UNIT_METRIC,
        )

    suspend fun updateAge(age: Int) {
        dataStore.edit { it[KEY_AGE] = age.coerceAtLeast(0) }
    }

    suspend fun updateWeightKg(weightKg: Float) {
        dataStore.edit { it[KEY_WEIGHT_KG] = weightKg.coerceAtLeast(0f) }
    }

    suspend fun updateHeightCm(heightCm: Float) {
        dataStore.edit { it[KEY_HEIGHT_CM] = heightCm.coerceAtLeast(0f) }
    }

    suspend fun updateLanguagePref(value: String) {
        dataStore.edit { it[KEY_LANGUAGE] = value }
    }

    suspend fun updateThemePref(value: String) {
        dataStore.edit { it[KEY_THEME] = value }
    }

    suspend fun updateUnitPref(value: String) {
        dataStore.edit { it[KEY_UNITS] = value }
    }

    companion object {
        private val KEY_AGE = intPreferencesKey("age")
        private val KEY_WEIGHT_KG = floatPreferencesKey("weight_kg")
        private val KEY_HEIGHT_CM = floatPreferencesKey("height_cm")
        private val KEY_LANGUAGE = stringPreferencesKey("language_pref")
        private val KEY_THEME = stringPreferencesKey("theme_pref")
        private val KEY_UNITS = stringPreferencesKey("unit_pref")
    }
}
