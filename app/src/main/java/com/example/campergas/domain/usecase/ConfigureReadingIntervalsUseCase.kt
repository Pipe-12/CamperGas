package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Caso de uso para configurar los intervalos de lectura del sensor BLE
 */
class ConfigureReadingIntervalsUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {

    /**
     * Configurar el intervalo de lectura de peso
     * @param intervalSeconds Intervalo en segundos
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
     * Configurar el intervalo de lectura de inclinación
     * @param intervalSeconds Intervalo en segundos
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
     * @param weightIntervalSeconds Intervalo de peso en segundos
     * @param inclinationIntervalSeconds Intervalo de inclinación en segundos
     */
    suspend fun setReadingIntervals(weightIntervalSeconds: Int, inclinationIntervalSeconds: Int) {
        val weightIntervalMs = weightIntervalSeconds * 1000L
        val inclinationIntervalMs = inclinationIntervalSeconds * 1000L

        bleRepository.saveWeightReadInterval(weightIntervalMs)
        bleRepository.saveInclinationReadInterval(inclinationIntervalMs)
        bleRepository.configureReadingIntervals(weightIntervalMs, inclinationIntervalMs)
    }

    /**
     * Obtener el intervalo actual de lectura de peso en segundos
     */
    fun getWeightReadIntervalSeconds(): Flow<Int> {
        return bleRepository.weightReadInterval.map { it.toInt() / 1000 }
    }

    /**
     * Obtener el intervalo actual de lectura de inclinación en segundos
     */
    fun getInclinationReadIntervalSeconds(): Flow<Int> {
        return bleRepository.inclinationReadInterval.map { it.toInt() / 1000 }
    }

    /**
     * Resetear intervalos a valores por defecto (Peso: 1 minuto, Inclinación: 5 segundos)
     */
    suspend fun resetToDefaultIntervals() {
        setReadingIntervals(60, 5) // Peso: 60 segundos (1 minuto), Inclinación: 5 segundos
    }

    /**
     * Reinicia la lectura periódica con los intervalos actuales
     */
    fun restartPeriodicReading() {
        bleRepository.restartPeriodicDataReading()
    }
}
