package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Caso de uso for leer data from sensor CamperGas bajo demanda
 * Permite solicitar readings específicas of weight e inclinación
 */
class ReadSensorDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {

    /**
     * Solicita lectura of data of weight bajo demanda
     * Los data se actualizarán in the StateFlow correspondiente
     */
    fun readWeightData() {
        bleRepository.readWeightDataOnDemand()
    }

    /**
     * Solicita lectura of data of inclination bajo demanda
     * Los data se actualizarán in the StateFlow correspondiente
     */
    fun readInclinationData() {
        bleRepository.readInclinationDataOnDemand()
    }

    /**
     * Solicita lectura de todos los data from sensor (peso e inclinación) bajo demanda
     * Los data se actualizarán en sus StateFlows correspondientes
     */
    fun readAllSensorData() {
        readWeightData()
        readInclinationData()
    }

    /**
     * Gets el state of conexión como StateFlow
     */
    fun getConnectionState(): StateFlow<Boolean> {
        return bleRepository.connectionState
    }
}
