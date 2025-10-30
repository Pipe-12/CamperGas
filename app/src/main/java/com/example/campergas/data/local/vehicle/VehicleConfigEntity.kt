package com.example.campergas.data.local.vehicle

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.campergas.domain.model.VehicleType

/**
 * Entidad que representa la configuración del vehículo para cálculos de estabilidad.
 *
 * Almacena las dimensiones físicas del vehículo (caravana o autocaravana) requeridas
 * para calcular las elevaciones de las ruedas y la estabilidad general del vehículo
 * basándose en datos del sensor de inclinación.
 *
 * @property id Identificador único para la configuración (por defecto: "default_config")
 * @property type Tipo de vehículo (CARAVAN o AUTOCARAVANA)
 * @property distanceBetweenRearWheels Distancia entre ruedas traseras en centímetros
 * @property distanceToFrontSupport Distancia desde ruedas traseras hasta punto de apoyo delantero en cm (solo caravana)
 * @property distanceBetweenFrontWheels Distancia entre ruedas delanteras en cm (solo autocaravana)
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
