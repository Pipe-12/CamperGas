package com.example.campergas.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.usecase.ConfigureReadingIntervalsUseCase
import com.example.campergas.domain.usecase.ReadSensorDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    /**
     * Configura el intervalo de lectura de peso
     */
    fun setWeightInterval(intervalSeconds: Int) {
        viewModelScope.launch {
            try {
                configureReadingIntervalsUseCase.setWeightReadInterval(intervalSeconds)
            } catch (e: Exception) {
                // Manejar error si es necesario
            }
        }
    }

    /**
     * Configura el intervalo de lectura de inclinación
     */
    fun setInclinationInterval(intervalSeconds: Int) {
        viewModelScope.launch {
            try {
                configureReadingIntervalsUseCase.setInclinationReadInterval(intervalSeconds)
            } catch (e: Exception) {
                // Manejar error si es necesario
            }
        }
    }

    /**
     * Configura ambos intervalos a la vez
     */
    fun setBothIntervals(weightSeconds: Int, inclinationSeconds: Int) {
        viewModelScope.launch {
            try {
                configureReadingIntervalsUseCase.setReadingIntervals(
                    weightSeconds,
                    inclinationSeconds
                )
            } catch (e: Exception) {
                // Manejar error si es necesario
            }
        }
    }

    /**
     * Resetea los intervalos a los valores por defecto
     */
    fun resetToDefault() {
        viewModelScope.launch {
            try {
                configureReadingIntervalsUseCase.resetToDefaultIntervals()
            } catch (e: Exception) {
                // Manejar error si es necesario
            }
        }
    }
}
