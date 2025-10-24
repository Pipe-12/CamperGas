package com.example.campergas.domain.model

/**
 * Modelo de dominio que representa un registro de consumo de combustible.
 * 
 * Esta clase de datos es una vista simplificada de FuelMeasurement, enfocada
 * específicamente en el historial de consumo de gas para su visualización en
 * gráficos y listas de consumo histórico.
 * 
 * Contiene información sobre:
 * - El cilindro de gas asociado (ID y nombre)
 * - La fecha/hora de la medición
 * - Los valores de combustible (kilogramos, porcentaje, peso total)
 * - Estado de calibración
 * - Origen de los datos (tiempo real vs histórico/offline)
 * 
 * @property id Identificador único del registro de consumo
 * @property cylinderId ID del cilindro de gas al que pertenece esta medición
 * @property cylinderName Nombre descriptivo del cilindro de gas
 * @property date Timestamp Unix (milisegundos) de cuándo se realizó la medición
 * @property fuelKilograms Cantidad de gas disponible en kilogramos
 * @property fuelPercentage Porcentaje de gas disponible respecto a la capacidad total (0-100%)
 * @property totalWeight Peso total medido incluyendo cilindro y gas (en kg)
 * @property isCalibrated Indica si la medición está calibrada con la tara del cilindro
 * @property isHistorical Indica si es un dato histórico/offline sincronizado del sensor o dato en tiempo real
 * @author Felipe García Gómez
 */
data class Consumption(
    val id: Long = 0,
    val cylinderId: Long,
    val cylinderName: String,
    val date: Long,
    val fuelKilograms: Float,
    val fuelPercentage: Float,
    val totalWeight: Float,
    val isCalibrated: Boolean = true,
    val isHistorical: Boolean = false
) {
    companion object {
        /**
         * Convierte una medición de combustible completa a un registro de consumo.
         * 
         * Este método de fábrica transforma un objeto FuelMeasurement (que contiene
         * información detallada de medición) en un objeto Consumption más simple
         * orientado a visualización de historial.
         * 
         * @param fuelMeasurement Medición de combustible completa a convertir
         * @return Objeto Consumption con los datos relevantes para historial
         */
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
