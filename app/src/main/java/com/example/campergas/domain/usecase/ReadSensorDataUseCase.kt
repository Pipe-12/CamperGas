package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.model.Inclination
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Caso de uso para leer datos del sensor CamperGas bajo demanda
 * Permite solicitar lecturas específicas de peso e inclinación
 */
class ReadSensorDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {

    /**
     * Solicita lectura de datos de peso bajo demanda
     * Los datos se actualizarán en el StateFlow correspondiente
     */
    fun readWeightData() {
        bleRepository.readWeightDataOnDemand()
    }

    /**
     * Solicita lectura de datos de inclinación bajo demanda
     * Los datos se actualizarán en el StateFlow correspondiente
     */
    fun readInclinationData() {
        bleRepository.readInclinationDataOnDemand()
    }

    /**
     * Solicita ambos tipos de datos (peso e inclinación) bajo demanda
     */
    fun readAllSensorData() {
        readWeightData()
        readInclinationData()
    }

    /**
     * Obtiene el StateFlow de datos de combustible
     */
    fun getFuelData(): StateFlow<FuelMeasurement?> {
        return bleRepository.fuelData
    }

    /**
     * Obtiene el StateFlow de datos de inclinación
     */
    fun getInclinationData(): StateFlow<Inclination?> {
        return bleRepository.inclinationData
    }

    /**
     * Verifica si el sensor está conectado
     */
    fun isConnected(): Boolean {
        return bleRepository.isConnected()
    }

    /**
     * Obtiene el estado de conexión como StateFlow
     */
    fun getConnectionState(): StateFlow<Boolean> {
        return bleRepository.connectionState
    }

    /**
     * Configura los intervalos de lectura del sensor
     * @param weightIntervalMs Intervalo de peso en milisegundos
     * @param inclinationIntervalMs Intervalo de inclinación en milisegundos
     */
    fun configureReadingIntervals(weightIntervalMs: Long, inclinationIntervalMs: Long) {
        bleRepository.configureReadingIntervals(weightIntervalMs, inclinationIntervalMs)
    }

    /**
     * Obtiene los intervalos actuales de lectura
     * @return Pair<Long, Long> donde el primer valor es el intervalo de peso y el segundo el de inclinación
     */
    fun getCurrentReadingIntervals(): Pair<Long, Long> {
        return Pair(
            bleRepository.getWeightReadInterval(),
            bleRepository.getInclinationReadInterval()
        )
    }
}
