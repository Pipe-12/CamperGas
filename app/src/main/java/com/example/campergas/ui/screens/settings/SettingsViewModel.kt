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

/**
 * ViewModel para la gestión de configuración de la aplicación.
 *
 * Gestiona las preferencias del usuario incluyendo:
 * - Modo de tema (claro, oscuro, sistema)
 * - Configuración de notificaciones
 * - Intervalos de lectura de sensores BLE
 * - Umbral de advertencia de combustible bajo
 *
 * Proporciona flujos de estado reactivos para los valores de configuración
 * y maneja la persistencia a través de PreferencesDataStore.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val configureReadingIntervalsUseCase: ConfigureReadingIntervalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    /** Flujo del estado de UI para la pantalla de configuración */
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /** Flujo del intervalo de lectura del sensor de peso en minutos */
    val weightInterval: StateFlow<Int> =
        configureReadingIntervalsUseCase.getWeightReadIntervalSeconds()
            .map { it / 60 } // Convertir segundos a minutos
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 1 // 1 minuto por defecto
            )

    /** Flujo del intervalo de lectura del sensor de inclinación en segundos */
    val inclinationInterval: StateFlow<Int> =
        configureReadingIntervalsUseCase.getInclinationReadIntervalSeconds()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 15  // 15 segundos por defecto
            )

    private val _operationStatus = MutableStateFlow<String?>(null)
    /** Flujo de mensajes de estado de operaciones BLE para retroalimentación al usuario */
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Carga la configuración inicial desde las preferencias del usuario.
     * 
     * Combina múltiples flujos de preferencias (tema, notificaciones, umbral de gas)
     * en un único estado de UI que se actualiza reactivamente.
     */
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

    /**
     * Cambia el modo de tema de la aplicación.
     * 
     * Guarda el nuevo modo de tema en las preferencias del usuario.
     * El cambio se aplica inmediatamente en toda la aplicación.
     * 
     * @param themeMode Nuevo modo de tema (LIGHT, DARK o SYSTEM)
     */
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesDataStore.setThemeMode(themeMode)
        }
    }

    /**
     * Alterna el estado de las notificaciones (activadas/desactivadas).
     * 
     * Invierte el estado actual de las notificaciones y lo guarda en las preferencias.
     */
    fun toggleNotifications() {
        viewModelScope.launch {
            preferencesDataStore.setNotificationsEnabled(!_uiState.value.notificationsEnabled)
        }
    }

    /**
     * Establece el umbral de nivel de gas para las advertencias de bajo combustible.
     * 
     * @param threshold Umbral en porcentaje (0-100)
     */
    fun setGasLevelThreshold(threshold: Float) {
        viewModelScope.launch {
            preferencesDataStore.setGasLevelThreshold(threshold)
        }
    }

    /**
     * Configura el intervalo de lectura del sensor de peso.
     * 
     * Convierte el intervalo de minutos a segundos y lo aplica mediante el caso de uso.
     * Muestra mensajes de estado durante la operación.
     * 
     * @param intervalMinutes Intervalo en minutos (se convierte a segundos internamente)
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
     * Configura el intervalo de lectura del sensor de inclinación.
     * 
     * Aplica el intervalo en segundos mediante el caso de uso.
     * Muestra mensajes de estado durante la operación.
     * 
     * @param intervalSeconds Intervalo en segundos
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
}

/**
 * Estado de UI para la pantalla de configuración.
 * 
 * @param themeMode Modo de tema actual (LIGHT, DARK o SYSTEM)
 * @param notificationsEnabled Indica si las notificaciones están activadas
 * @param gasLevelThreshold Umbral de nivel de gas para advertencias (porcentaje)
 * @param isLoading Indica si hay una operación en curso
 * @param error Mensaje de error si hay alguno
 */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val gasLevelThreshold: Float = 15.0f,
    val isLoading: Boolean = false,
    val error: String? = null
)
