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
     * Obtiene el estado de conexión como StateFlow
     */
    fun getConnectionState(): StateFlow<Boolean> {
        return bleRepository.connectionState
    }
}
