package com.example.campergas.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.data.local.preferences.PreferencesDataStore
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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val configureReadingIntervalsUseCase: ConfigureReadingIntervalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // StateFlows para intervalos de lectura BLE
    val weightInterval: StateFlow<Int> =
        configureReadingIntervalsUseCase.getWeightReadIntervalSeconds()
            .map { it / 60 } // Convertir segundos a minutos
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 1 // 1 minuto por defecto
            )

    val inclinationInterval: StateFlow<Int> =
        configureReadingIntervalsUseCase.getInclinationReadIntervalSeconds()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 15  //1 segundo por defecto
            )

    // Estado para feedback visual de operaciones BLE
    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesDataStore.themeMode,
                preferencesDataStore.areNotificationsEnabled,
                preferencesDataStore.gasLevelThreshold
            ) { themeMode, notificationsEnabled, gasLevelThreshold ->
                SettingsUiState(
                    themeMode = themeMode,
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

    // Métodos para configurar intervalos de lectura BLE
    fun setWeightInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Configurando intervalo de peso..."
                val intervalSeconds = intervalMinutes * 60 // Convertir minutos a segundos
                configureReadingIntervalsUseCase.setWeightReadInterval(intervalSeconds)
                _operationStatus.value = "Intervalo de peso configurado: $intervalMinutes min"

                // Limpiar el mensaje después de un tiempo
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value =
                    "Error al configurar intervalo de peso: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }

    fun setInclinationInterval(intervalSeconds: Int) {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Configurando intervalo de inclinación..."
                configureReadingIntervalsUseCase.setInclinationReadInterval(intervalSeconds)
                _operationStatus.value = "Intervalo de inclinación configurado: ${intervalSeconds}s"

                // Limpiar el mensaje después de un tiempo
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value =
                    "Error al configurar intervalo de inclinación: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val gasLevelThreshold: Float = 15.0f,
    val isLoading: Boolean = false,
    val error: String? = null
)
