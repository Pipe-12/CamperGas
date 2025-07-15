package com.example.campergas.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.usecase.ConfigureReadingIntervalsUseCase
import com.example.campergas.domain.usecase.ReadSensorDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingIntervalsSettingsViewModel @Inject constructor(
    private val configureReadingIntervalsUseCase: ConfigureReadingIntervalsUseCase,
    readSensorDataUseCase: ReadSensorDataUseCase
) : ViewModel() {

    // StateFlows para la UI
    val weightInterval: StateFlow<Int> =
        configureReadingIntervalsUseCase.getWeightReadIntervalSeconds()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 5
            )

    val inclinationInterval: StateFlow<Int> =
        configureReadingIntervalsUseCase.getInclinationReadIntervalSeconds()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 5
            )

    val isConnected: StateFlow<Boolean> = readSensorDataUseCase.getConnectionState()

    // Estado para feedback visual
    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    /**
     * Configura el intervalo de lectura de peso
     */
    fun setWeightInterval(intervalSeconds: Int) {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Configurando intervalo de peso..."
                configureReadingIntervalsUseCase.setWeightReadInterval(intervalSeconds)
                _operationStatus.value = "Intervalo de peso configurado: ${intervalSeconds}s"
                
                // Limpiar el mensaje después de un tiempo
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value = "Error al configurar intervalo de peso: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }

    /**
     * Configura el intervalo de lectura de inclinación
     */
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
                _operationStatus.value = "Error al configurar intervalo de inclinación: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }

    /**
     * Configura ambos intervalos a la vez
     */
    fun setBothIntervals(weightSeconds: Int, inclinationSeconds: Int) {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Configurando ambos intervalos..."
                configureReadingIntervalsUseCase.setReadingIntervals(
                    weightSeconds,
                    inclinationSeconds
                )
                _operationStatus.value = "Intervalos configurados: Peso ${weightSeconds}s, Inclinación ${inclinationSeconds}s"
                
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

    /**
     * Resetea los intervalos a los valores por defecto
     */
    fun resetToDefault() {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Restaurando valores por defecto..."
                configureReadingIntervalsUseCase.resetToDefaultIntervals()
                _operationStatus.value = "Intervalos restaurados a 5 segundos"
                
                // Limpiar el mensaje después de un tiempo
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value = "Error al restaurar valores por defecto: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }

    /**
     * Reinicia la lectura periódica
     */
    fun restartPeriodicReading() {
        viewModelScope.launch {
            try {
                _operationStatus.value = "Reiniciando lectura periódica..."
                configureReadingIntervalsUseCase.restartPeriodicReading()
                _operationStatus.value = "Lectura periódica reiniciada"
                
                // Limpiar el mensaje después de un tiempo
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            } catch (exception: Exception) {
                _operationStatus.value = "Error al reiniciar lectura periódica: ${exception.message}"
                kotlinx.coroutines.delay(2000)
                _operationStatus.value = null
            }
        }
    }
}
