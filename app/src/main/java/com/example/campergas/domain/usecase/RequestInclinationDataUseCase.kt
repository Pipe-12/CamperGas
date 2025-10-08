package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import javax.inject.Inject

/**
 * UseCase para solicitar datos de inclinación from sensor BLE bajo demanda
 */
class RequestInclinationDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Ejecuta la solicitud de datos de inclinación
     */
    operator fun invoke() {
        bleRepository.readInclinationDataOnDemand()
    }
}
