package com.example.campergas.domain.model

data class VehicleConfig(
    val type: VehicleType = VehicleType.CARAVAN,
    val distanceBetweenRearWheels: Float, // Distancia entre las ruedas traseras en cm
    val distanceToFrontSupport: Float // Distancia entre ruedas traseras y punto de apoyo delantero en cm
)
