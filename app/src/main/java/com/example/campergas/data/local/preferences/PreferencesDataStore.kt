package com.example.campergas.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val weightReadIntervalKey = longPreferencesKey("weight_read_interval")
    private val inclinationReadIntervalKey = longPreferencesKey("inclination_read_interval")

    val lastConnectedDeviceAddress: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[lastConnectedDeviceKey] ?: ""
        }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val modeString = preferences[themeModeKey] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(modeString)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

    val areNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[notificationsEnabledKey] ?: true
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

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
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
