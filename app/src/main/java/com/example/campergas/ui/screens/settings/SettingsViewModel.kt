package com.example.campergas.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.Language
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
 * - Application language
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
            .map { it / 60 } // Convert seconds to minutes
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
                initialValue = 15  //1 segundo por defecto
            )

    private val _operationStatus = MutableStateFlow<String?>(null)
    /** Flow of BLE operation status messages for user feedback */
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesDataStore.themeMode,
                preferencesDataStore.language,
                preferencesDataStore.areNotificationsEnabled,
                preferencesDataStore.gasLevelThreshold
            ) { themeMode, language, notificationsEnabled, gasLevelThreshold ->
                SettingsUiState(
                    themeMode = themeMode,
                    language = language,
                    notificationsEnabled = notificationsEnabled,
                    gasLevelThreshold = gasLevelThreshold
                )
            }.collect { settings ->
                _uiState.value = settings
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesDataStore.setThemeMode(themeMode)
        }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            preferencesDataStore.setLanguage(language)
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            preferencesDataStore.setNotificationsEnabled(!_uiState.value.notificationsEnabled)
        }
    }

    fun setGasLevelThreshold(threshold: Float) {
        viewModelScope.launch {
            preferencesDataStore.setGasLevelThreshold(threshold)
        }
    }

    // Methods to configure BLE reading intervals
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
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: Language = Language.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val gasLevelThreshold: Float = 15.0f,
    val isLoading: Boolean = false,
    val error: String? = null
)
