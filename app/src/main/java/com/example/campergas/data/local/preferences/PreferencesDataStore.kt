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

/**
 * DataStore-based preferences manager for application settings.
 *
 * Provides type-safe access to user preferences including theme mode, language,
 * BLE device connection info, notification settings, and sensor read intervals.
 * All preferences are exposed as Flows for reactive UI updates.
 */
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

    /**
     * Flow of the last connected BLE device address.
     *
     * @return Flow emitting device address, empty string if never connected
     */
    val lastConnectedDeviceAddress: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[lastConnectedDeviceKey] ?: ""
        }

    /**
     * Flow of the current theme mode.
     *
     * @return Flow always emitting ThemeMode.DARK (application uses only dark mode)
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { _ ->
            ThemeMode.DARK
        }

    /**
     * Flow of the selected application language.
     *
     * @return Flow emitting Language, defaults to SYSTEM if not set
     */
    val language: Flow<Language> = context.dataStore.data
        .map { preferences ->
            val languageCode = preferences[languageKey] ?: Language.SYSTEM.code
            Language.entries.find { it.code == languageCode } ?: Language.SYSTEM
        }

    /**
     * Flow indicating whether notifications are enabled.
     *
     * @return Flow emitting true if notifications enabled, defaults to true
     */
    val areNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[notificationsEnabledKey] != false
        }

    /**
     * Flow of the gas level threshold for low fuel warnings.
     *
     * @return Flow emitting threshold percentage, defaults to 15.0%
     */
    val gasLevelThreshold: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[gasLevelThresholdKey] ?: 15.0f // 15% por defecto
        }

    /**
     * Flow of the weight sensor read interval in milliseconds.
     *
     * @return Flow emitting interval in ms, defaults to 5000ms (5 seconds)
     */
    val weightReadInterval: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[weightReadIntervalKey] ?: 5000L // 5 segundos por defecto
        }

    /**
     * Flow of the inclination sensor read interval in milliseconds.
     *
     * @return Flow emitting interval in ms, defaults to 5000ms (5 seconds)
     */
    val inclinationReadInterval: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[inclinationReadIntervalKey] ?: 5000L // 5 segundos por defecto
        }

    /**
     * Saves the address of the last connected BLE device.
     *
     * @param address BLE device MAC address
     */
    suspend fun saveLastConnectedDevice(address: String) {
        context.dataStore.edit { preferences ->
            preferences[lastConnectedDeviceKey] = address
        }
    }

    /**
     * Sets the application theme mode.
     * Note: Application always uses dark mode, this method is kept for API compatibility.
     *
     * @param themeMode The theme mode (always DARK)
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        // No-op: application always uses dark mode
    }

    /**
     * Sets the application language.
     *
     * @param language The language to use
     */
    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { preferences ->
            preferences[languageKey] = language.code
        }
    }

    /**
     * Enables or disables notifications.
     *
     * @param enabled True to enable notifications, false to disable
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
        }
    }

    /**
     * Sets the gas level threshold for low fuel warnings.
     *
     * @param threshold Threshold percentage (0-100)
     */
    suspend fun setGasLevelThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[gasLevelThresholdKey] = threshold
        }
    }

    /**
     * Sets the weight sensor read interval.
     *
     * @param intervalMs Interval in milliseconds
     */
    suspend fun setWeightReadInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[weightReadIntervalKey] = intervalMs
        }
    }

    /**
     * Sets the inclination sensor read interval.
     *
     * @param intervalMs Interval in milliseconds
     */
    suspend fun setInclinationReadInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[inclinationReadIntervalKey] = intervalMs
        }
    }
}
