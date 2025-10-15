package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Caso de uso for leer data from sensor CamperGas bajo demanda
 * Allows requesting specific readings of weight and inclination
 */
class ReadSensorDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {

    /**
     * Solicita lectura of data of weight bajo demanda
     * Data will be updated in corresponding StateFlow
     */
    private fun readWeightData() {
        bleRepository.readWeightDataOnDemand()
    }

    /**
     * Solicita lectura of data of inclination bajo demanda
     * Data will be updated in corresponding StateFlow
     */
    private fun readInclinationData() {
        bleRepository.readInclinationDataOnDemand()
    }

    /**
     * Solicita lectura of all data from sensor (peso e inclination) bajo demanda
     * Data will be updated in their corresponding StateFlows
     */
    fun readAllSensorData() {
        readWeightData()
        readInclinationData()
    }

    /**
     * Gets connection state as StateFlow
     */
    fun getConnectionState(): StateFlow<Boolean> {
        return bleRepository.connectionState
    }
}
