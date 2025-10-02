package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.VehicleRepository
import com.example.campergas.domain.model.VehicleConfig
import com.example.campergas.domain.model.VehicleType
import javax.inject.Inject

class SaveVehicleConfigUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    /**
     * Guarda o actualiza la configuración del vehículo.
     * Si ya existe una configuración, se actualizará en lugar de crear una nueva.
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
     * Guarda o actualiza un objeto de configuración completo.
     * Si ya existe una configuración, se actualizará en lugar de crear una nueva.
     */
    suspend fun saveVehicleConfig(config: VehicleConfig) {
        vehicleRepository.saveVehicleConfig(config)
    }
}
