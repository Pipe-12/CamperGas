package com.example.campergas.data.local.vehicle

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.campergas.domain.model.VehicleType

@Entity(tableName = "vehicle_config")
data class VehicleConfigEntity(
    @PrimaryKey
    val id: String = "default_config", // Solo necesitamos una configuración
    val type: VehicleType = VehicleType.CARAVAN,
    val distanceBetweenRearWheels: Float, // Distancia entre las ruedas traseras en cm
    val distanceToFrontSupport: Float, // Distancia entre ruedas traseras y punto de apoyo delantero en cm (solo para caravana)
    val distanceBetweenFrontWheels: Float? = null // Distancia entre ruedas delanteras en cm (solo para autocaravana)
)
