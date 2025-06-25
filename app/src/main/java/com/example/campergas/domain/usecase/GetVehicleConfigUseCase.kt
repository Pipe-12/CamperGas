package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.VehicleRepository
import com.example.campergas.domain.model.VehicleConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVehicleConfigUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(): Flow<VehicleConfig?> {
        return vehicleRepository.getVehicleConfig()
    }
}
