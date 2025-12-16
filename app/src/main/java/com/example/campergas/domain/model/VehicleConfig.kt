package com.example.campergas.domain.model

/**
 * Domain model representing the geometric configuration of a recreational vehicle.
 *
 * This data class stores the physical dimensions of the vehicle (caravan or motorhome)
 * needed to correctly calculate:
 * - Weight distribution on the axles
 * - Vehicle leveling
 * - Required wheel elevation to correct inclination
 * - Estimated gas consumption
 *
 * The dimensions are critical for:
 * - Stability calculations based on sensor inclination
 * - Leveling recommendations using wheel chocks
 * - Correct visualization in stability widgets
 *
 * Differences by vehicle type:
 * - CARAVAN: Uses distanceToFrontSupport (front support point on the drawbar)
 * - AUTOCARAVANA: Uses distanceBetweenFrontWheels (complete front axle)
 *
 * @property type Vehicle type (caravan or motorhome)
 * @property distanceBetweenRearWheels Distance between rear axle wheels in centimeters
 * @property distanceToFrontSupport Distance from rear axle to front support point in cm (caravans only)
 * @property distanceBetweenFrontWheels Distance between front axle wheels in cm (motorhomes only)
 * @property gasTankCapacity Total capacity of the gas system in kilograms
 * @author Felipe García Gómez
 */
data class VehicleConfig(
    val type: VehicleType = VehicleType.CARAVAN,
    val distanceBetweenRearWheels: Float,
    val distanceToFrontSupport: Float,
    val distanceBetweenFrontWheels: Float? = null,
    val gasTankCapacity: Float = 100.0f
)
