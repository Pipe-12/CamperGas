package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.FuelMeasurement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFuelDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Obtiene los datos de combustible en tiempo real from sensor BLE
     */
    operator fun invoke(): Flow<FuelMeasurement?> {
        return bleRepository.fuelData
    }
}
