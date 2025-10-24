package com.example.campergas.data.local.vehicle

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.campergas.domain.model.VehicleType

/**
 * Entity representing vehicle configuration for stability calculations.
 *
 * Stores physical dimensions of the vehicle (caravan or motorhome) required
 * for calculating wheel elevations and overall vehicle stability based on
 * inclination sensor data.
 *
 * @property id Unique identifier for the configuration (default: "default_config")
 * @property type Type of vehicle (CARAVAN or AUTOCARAVANA)
 * @property distanceBetweenRearWheels Distance between rear wheels in centimeters
 * @property distanceToFrontSupport Distance from rear wheels to front support point in cm (caravan only)
 * @property distanceBetweenFrontWheels Distance between front wheels in cm (motorhome only)
 */
@Entity(tableName = "vehicle_config")
data class VehicleConfigEntity(
    @PrimaryKey
    val id: String = "default_config", // We only need one configuration
    val type: VehicleType = VehicleType.CARAVAN,
    val distanceBetweenRearWheels: Float, // Distancia entre las ruedas traseras en cm
    val distanceToFrontSupport: Float, // Distancia entre ruedas traseras y punto de apoyo delantero en cm (solo for caravana)
    val distanceBetweenFrontWheels: Float? = null // Distancia entre ruedas delanteras en cm (solo for autocaravana)
)
