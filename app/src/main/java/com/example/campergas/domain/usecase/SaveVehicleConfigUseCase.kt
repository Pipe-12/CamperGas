package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.VehicleRepository
import com.example.campergas.domain.model.VehicleConfig
import com.example.campergas.domain.model.VehicleType
import javax.inject.Inject

class SaveVehicleConfigUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    /**
     * Saves or updates vehicle configuration.
     * If configuration already exists, it will be updated instead of creating a new one.
     */
    suspend operator fun invoke(
        type: VehicleType,
        distanceBetweenRearWheels: Float,
        distanceToFrontSupport: Float,
        distanceBetweenFrontWheels: Float? = null
    ) {
        val config = VehicleConfig(
            type = type,
            distanceBetweenRearWheels = distanceBetweenRearWheels,
            distanceToFrontSupport = distanceToFrontSupport,
            distanceBetweenFrontWheels = distanceBetweenFrontWheels
        )

        vehicleRepository.saveVehicleConfig(config)
    }

    /**
     * Saves or updates a complete configuration object.
     * If configuration already exists, it will be updated instead of creating a new one.
     */
    suspend fun saveVehicleConfig(config: VehicleConfig) {
        vehicleRepository.saveVehicleConfig(config)
    }
}
