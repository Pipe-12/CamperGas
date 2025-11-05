package com.example.campergas.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.AppLanguage
import com.example.campergas.domain.model.ThemeMode
import com.example.campergas.domain.usecase.ConfigureReadingIntervalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for application settings management.
 *
 * Manages user preferences including:
 * - Theme mode (light, dark, system)
 * - Notification settings
 * - BLE sensor reading intervals
 * - Low fuel warning threshold
 *
 * Provides reactive state flows for settings values and handles
 * persistence through PreferencesDataStore.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val configureReadingIntervalsUseCase: ConfigureReadingIntervalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())

    /** Flow of UI state for the settings screen */
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /** Flow of weight sensor reading interval in minutes */
    val weightInterval: StateFlow<Int> =
        configureReadingIntervalsUseCase.getWeightReadIntervalSeconds()
            .map { it / 60 } // Convertir segundos a minutos
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 1 // 1 minuto por defecto
            )

    /** Flow of inclination sensor reading interval in seconds */
    val inclinationInterval: StateFlow<Int> =
        configureReadingIntervalsUseCase.getInclinationReadIntervalSeconds()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 15  // 15 segundos por defecto
            )

    private val _operationStatus = MutableStateFlow<String?>(null)

    /** Flow of BLE operation status messages for user feedback */
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Loads initial settings from user preferences.
     *
     * Combines multiple preference flows (theme, notifications, gas threshold)
     * into a single UI state that updates reactively.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesDataStore.themeMode,
                preferencesDataStore.areNotificationsEnabled,
                preferencesDataStore.gasLevelThreshold,
                preferencesDataStore.appLanguage
            ) { themeMode, notificationsEnabled, gasLevelThreshold, appLanguage ->
                SettingsUiState(
                    themeMode = themeMode,
                    notificationsEnabled = notificationsEnabled,
                    gasLevelThreshold = gasLevelThreshold,
                    appLanguage = appLanguage
                )
            }.collect { settings ->
                _uiState.value = settings
            }
        }
    }

    /**
     * Changes the application theme mode.
     *
     * Saves the new theme mode to user preferences.
     * The change applies immediately across the entire application.
     *
     * @param themeMode New theme mode (LIGHT, DARK, or SYSTEM)
     */
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesDataStore.setThemeMode(themeMode)
        }
    }

    /**
     * Toggles the notification state (enabled/disabled).
     *
     * Inverts the current notification state and saves it to preferences.
     */
    fun toggleNotifications() {
        viewModelScope.launch {
            preferencesDataStore.setNotificationsEnabled(!_uiState.value.notificationsEnabled)
        }
    }

    /**
     * Sets the gas level threshold for low fuel warnings.
     *
     * @param threshold Threshold percentage (0-100)
     */
    fun setGasLevelThreshold(threshold: Float) {
        viewModelScope.launch {
            preferencesDataStore.setGasLevelThreshold(threshold)
        }
    }

    /**
     * Configures the weight sensor reading interval.
     *
     * Converts the interval from minutes to seconds and applies it via the use case.
     * Displays status messages during the operation.
     *
     * @param intervalMinutes Interval in minutes (converted to seconds internally)
     */
    fun setWeightInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Configuring weight interval..."
                val intervalSeconds = intervalMinutes * 60 // Convert minutes to seconds
                configureReadingIntervalsUseCase.setWeightReadInterval(intervalSeconds)
                _operationStatus.value = "Weight interval configured: $intervalMinutes min"

                // Clear message after a while
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value =
                    "Error configuring weight interval: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }

    /**
     * Configures the inclination sensor reading interval.
     *
     * Applies the interval in seconds via the use case.
     * Displays status messages during the operation.
     *
     * @param intervalSeconds Interval in seconds
     */
    fun setInclinationInterval(intervalSeconds: Int) {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Configuring inclination interval..."
                configureReadingIntervalsUseCase.setInclinationReadInterval(intervalSeconds)
                _operationStatus.value = "Inclination interval configured: ${intervalSeconds}s"

                // Clear message after a while
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value =
                    "Error configuring inclination interval: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }

    /**
     * Changes the application language using per-app language APIs.
     *
     * Persists the new language and applies it immediately using AppCompatDelegate
     * so that all resources are refreshed across the app.
     */
    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesDataStore.setAppLanguage(language)

            // Apply locales at application level
            val locales = if (language == AppLanguage.SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language.tag)
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}

/**
 * UI state for the settings screen.
 *
 * @param themeMode Current theme mode (LIGHT, DARK, or SYSTEM)
 * @param notificationsEnabled Indicates if notifications are enabled
 * @param gasLevelThreshold Gas level threshold for warnings (percentage)
 * @param isLoading Indicates if an operation is in progress
 * @param error Error message if any
 */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val gasLevelThreshold: Float = 15.0f,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val isLoading: Boolean = false,
    val error: String? = null
)
