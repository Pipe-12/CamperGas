package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import javax.inject.Inject

/**
 * UseCase para solicitar datos de peso del sensor BLE bajo demanda
 */
class RequestWeightDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Ejecuta la solicitud de datos de peso
     */
    operator fun invoke() {
        bleRepository.readWeightDataOnDemand()
    }
}
