package com.example.campergas.domain.model

/**
 * Modelo que representa un registro de consumo de combustible
 * Es una vista simplificada de FuelMeasurement enfocada en el historial de consumo
 */
data class Consumption(
    val id: Long = 0,
    val cylinderId: Long,
    val cylinderName: String,
    val date: Long, // timestamp
    val fuelKilograms: Float,
    val fuelPercentage: Float,
    val totalWeight: Float,
    val isCalibrated: Boolean = true
) {
    /**
     * Convierte un FuelMeasurement a Consumption
     */
    companion object {
        fun fromFuelMeasurement(fuelMeasurement: FuelMeasurement): Consumption {
            return Consumption(
                id = fuelMeasurement.id,
                cylinderId = fuelMeasurement.cylinderId,
                cylinderName = fuelMeasurement.cylinderName,
                date = fuelMeasurement.timestamp,
                fuelKilograms = fuelMeasurement.fuelKilograms,
                fuelPercentage = fuelMeasurement.fuelPercentage,
                totalWeight = fuelMeasurement.totalWeight,
                isCalibrated = fuelMeasurement.isCalibrated
            )
        }
    }
}
