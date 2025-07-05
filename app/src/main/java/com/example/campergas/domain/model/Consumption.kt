package com.example.campergas.domain.model

import com.example.campergas.domain.model.FuelMeasurement

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
    
    /**
     * Formatea los kilogramos de combustible para mostrar en la UI
     */
    fun getFormattedFuelKilograms(): String = "%.2f kg".format(fuelKilograms)
    
    /**
     * Formatea el porcentaje de combustible para mostrar en la UI
     */
    fun getFormattedFuelPercentage(): String = "%.1f%%".format(fuelPercentage)
    
    /**
     * Determina si es un cambio significativo en el consumo
     */
    fun hasSignificantChange(previous: Consumption?): Boolean {
        return previous?.let { prev ->
            val percentageChange = kotlin.math.abs(fuelPercentage - prev.fuelPercentage)
            val timeDifference = date - prev.date
            val timeDifferenceMinutes = timeDifference / (60 * 1000)
            
            percentageChange >= 1.0f || timeDifferenceMinutes >= 15L
        } ?: true // Primera medición siempre es significativa
    }
}
