package com.example.campergas.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.ThemeMode
import com.example.campergas.domain.usecase.ConfigureReadingIntervalsUseCase
import com.example.campergas.domain.usecase.ReadSensorDataUseCase
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
    private val configureReadingIntervalsUseCase: ConfigureReadingIntervalsUseCase,
    private val readSensorDataUseCase: ReadSensorDataUseCase
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

    val isConnected: StateFlow<Boolean> = readSensorDataUseCase.getConnectionState()

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
                preferencesDataStore.areNotificationsEnabled
            ) { themeMode, notificationsEnabled ->
                SettingsUiState(
                    themeMode = themeMode,
                    notificationsEnabled = notificationsEnabled
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

    // Métodos para configurar intervalos de lectura BLE
    fun setWeightInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Configurando intervalo de peso..."
                val intervalSeconds = intervalMinutes * 60 // Convertir minutos a segundos
                configureReadingIntervalsUseCase.setWeightReadInterval(intervalSeconds)
                _operationStatus.value = "Intervalo de peso configurado: ${intervalMinutes} min"

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

    fun setBothIntervals(weightMinutes: Int, inclinationSeconds: Int) {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Configurando ambos intervalos..."
                val weightSeconds = weightMinutes * 60 // Convertir minutos a segundos
                configureReadingIntervalsUseCase.setReadingIntervals(
                    weightSeconds,
                    inclinationSeconds
                )
                _operationStatus.value =
                    "Intervalos configurados: Peso ${weightMinutes} min, Inclinación ${inclinationSeconds}s"

                // Limpiar el mensaje después de un tiempo
                kotlinx.coroutines.delay(3000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value = "Error al configurar intervalos: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }

    fun resetIntervalsToDefault() {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Restaurando valores por defecto..."
                configureReadingIntervalsUseCase.resetToDefaultIntervals()
                _operationStatus.value = "Intervalos restaurados: Peso 1 min, Inclinación 5s"

                // Limpiar el mensaje después de un tiempo
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value =
                    "Error al restaurar valores por defecto: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }

    fun restartPeriodicReading() {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Reiniciando lectura periódica..."
                configureReadingIntervalsUseCase.restartPeriodicReading()
                _operationStatus.value = "Lectura periódica reiniciada"

                // Limpiar el mensaje después de un tiempo
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: kotlinx.coroutines.CancellationException) {
                // Las cancelaciones son normales, no mostrar error
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value =
                    "Error al reiniciar lectura periódica: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
