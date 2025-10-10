package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import javax.inject.Inject

/**
 * UseCase for solicitar data of inclination from sensor BLE bajo demanda
 */
class RequestInclinationDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Ejecuta la request of data of inclination
     */
    operator fun invoke() {
        bleRepository.readInclinationDataOnDemand()
    }
}
