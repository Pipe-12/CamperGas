package com.example.campergas.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.campergas.domain.model.Language
import com.example.campergas.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val lastConnectedDeviceKey = stringPreferencesKey("last_connected_device")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("language")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val gasLevelThresholdKey = floatPreferencesKey("gas_level_threshold")
    private val weightReadIntervalKey = longPreferencesKey("weight_read_interval")
    private val inclinationReadIntervalKey = longPreferencesKey("inclination_read_interval")

    val lastConnectedDeviceAddress: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[lastConnectedDeviceKey] ?: ""
        }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val modeString = preferences[themeModeKey] ?: ThemeMode.LIGHT.name
            try {
                ThemeMode.valueOf(modeString)
            } catch (_: IllegalArgumentException) {
                ThemeMode.LIGHT
            }
        }

    val language: Flow<Language> = context.dataStore.data
        .map { preferences ->
            val languageCode = preferences[languageKey] ?: Language.SYSTEM.code
            Language.entries.find { it.code == languageCode } ?: Language.SYSTEM
        }

    val areNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[notificationsEnabledKey] != false
        }

    val gasLevelThreshold: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[gasLevelThresholdKey] ?: 15.0f // 15% por defecto
        }

    val weightReadInterval: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[weightReadIntervalKey] ?: 5000L // 5 segundos por defecto
        }

    val inclinationReadInterval: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[inclinationReadIntervalKey] ?: 5000L // 5 segundos por defecto
        }

    suspend fun saveLastConnectedDevice(address: String) {
        context.dataStore.edit { preferences ->
            preferences[lastConnectedDeviceKey] = address
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeModeKey] = themeMode.name
        }
    }

    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { preferences ->
            preferences[languageKey] = language.code
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
        }
    }

    suspend fun setGasLevelThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[gasLevelThresholdKey] = threshold
        }
    }

    suspend fun setWeightReadInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[weightReadIntervalKey] = intervalMs
        }
    }

    suspend fun setInclinationReadInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[inclinationReadIntervalKey] = intervalMs
        }
    }
}
