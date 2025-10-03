package com.example.campergas.data.repository

import com.example.campergas.data.local.vehicle.VehicleConfigEntity
import com.example.campergas.data.local.vehicle.VehicleDao
import com.example.campergas.domain.model.VehicleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao
) {
    fun getVehicleConfig(): Flow<VehicleConfig?> {
        return vehicleDao.getVehicleConfig().map { entity ->
            entity?.toDomainModel()
        }
    }

    suspend fun saveVehicleConfig(config: VehicleConfig) {
        val entity = config.toEntity()
        val exists = vehicleDao.configExists()

        if (exists) {
            vehicleDao.updateVehicleConfig(entity)
        } else {
            vehicleDao.insertVehicleConfig(entity)
        }
    }

    private fun VehicleConfigEntity.toDomainModel(): VehicleConfig {
        return VehicleConfig(
            type = this.type,
            distanceBetweenRearWheels = this.distanceBetweenRearWheels,
            distanceToFrontSupport = this.distanceToFrontSupport,
            distanceBetweenFrontWheels = this.distanceBetweenFrontWheels
        )
    }

    private fun VehicleConfig.toEntity(): VehicleConfigEntity {
        return VehicleConfigEntity(
            type = this.type,
            distanceBetweenRearWheels = this.distanceBetweenRearWheels,
            distanceToFrontSupport = this.distanceToFrontSupport,
            distanceBetweenFrontWheels = this.distanceBetweenFrontWheels
        )
    }
}
