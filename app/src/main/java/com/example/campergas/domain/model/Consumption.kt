package com.example.campergas.domain.model

/**
 * Modelo que representa un registro de consumption de combustible
 * Es una vista simplificada de FuelMeasurement enfocada in the historial de consumption
 */
data class Consumption(
    val id: Long = 0,
    val cylinderId: Long,
    val cylinderName: String,
    val date: Long, // timestamp
    val fuelKilograms: Float,
    val fuelPercentage: Float,
    val totalWeight: Float,
    val isCalibrated: Boolean = true,
    val isHistorical: Boolean = false // Indica si es un dato historical/offline
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
                isCalibrated = fuelMeasurement.isCalibrated,
                isHistorical = fuelMeasurement.isHistorical
            )
        }
    }
}
