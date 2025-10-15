package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import javax.inject.Inject

/**
 * UseCase for solicitar data of weight from sensor BLE bajo demanda
 */
class RequestWeightDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Ejecuta la request of data of weight
     */
    operator fun invoke() {
        bleRepository.readWeightDataOnDemand()
    }
}
