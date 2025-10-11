package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Caso de uso for configurar los intervalos de lectura from sensor BLE
 */
class ConfigureReadingIntervalsUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {

    /**
     * Configurar el intervalo de lectura of weight
     * @form intervalSeconds Intervalo en segundos
     */
    suspend fun setWeightReadInterval(intervalSeconds: Int) {
        val intervalMs = intervalSeconds * 1000L
        bleRepository.saveWeightReadInterval(intervalMs)
        bleRepository.configureReadingIntervals(
            weightIntervalMs = intervalMs,
            inclinationIntervalMs = bleRepository.getInclinationReadInterval()
        )
    }

    /**
     * Configurar el intervalo de lectura of inclination
     * @form intervalSeconds Intervalo en segundos
     */
    suspend fun setInclinationReadInterval(intervalSeconds: Int) {
        val intervalMs = intervalSeconds * 1000L
        bleRepository.saveInclinationReadInterval(intervalMs)
        bleRepository.configureReadingIntervals(
            weightIntervalMs = bleRepository.getWeightReadInterval(),
            inclinationIntervalMs = intervalMs
        )
    }

    /**
     * Configurar ambos intervalos a la vez
     * @form weightIntervalSeconds Intervalo of weight en segundos
     * @form inclinationIntervalSeconds Intervalo of inclination en segundos
     */
    suspend fun setReadingIntervals(weightIntervalSeconds: Int, inclinationIntervalSeconds: Int) {
        val weightIntervalMs = weightIntervalSeconds * 1000L
        val inclinationIntervalMs = inclinationIntervalSeconds * 1000L

        bleRepository.saveWeightReadInterval(weightIntervalMs)
        bleRepository.saveInclinationReadInterval(inclinationIntervalMs)
        bleRepository.configureReadingIntervals(weightIntervalMs, inclinationIntervalMs)
    }

    /**
     * Get el intervalo actual de lectura of weight en segundos
     */
    fun getWeightReadIntervalSeconds(): Flow<Int> {
        return bleRepository.weightReadInterval.map { it.toInt() / 1000 }
    }

    /**
     * Get el intervalo actual de lectura of inclination en segundos
     */
    fun getInclinationReadIntervalSeconds(): Flow<Int> {
        return bleRepository.inclinationReadInterval.map { it.toInt() / 1000 }
    }

    /**
     * Resetear intervalos a valores por defecto (Weight: 1 minuto, Inclination: 5 segundos)
     */
    suspend fun resetToDefaultIntervals() {
        setReadingIntervals(60, 5) // Weight: 60 segundos (1 minuto), Inclination: 5 segundos
    }

    /**
     * Restarts periodic reading with current intervals
     */
    fun restartPeriodicReading() {
        bleRepository.restartPeriodicDataReading()
    }
}
