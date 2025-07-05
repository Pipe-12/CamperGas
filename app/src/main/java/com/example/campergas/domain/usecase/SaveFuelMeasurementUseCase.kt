package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SaveFuelMeasurementUseCase @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository,
    private val gasCylinderRepository: GasCylinderRepository
) {
    
    /**
     * Guarda una medición de combustible calculando automáticamente los kilogramos
     * y porcentaje basándose en el peso total y la bombona activa
     */
    suspend fun saveRealTimeMeasurement(
        totalWeight: Float,
        timestamp: Long = System.currentTimeMillis(),
        isCalibrated: Boolean = true
    ): Result<SaveMeasurementResult> {
        return try {
            // Obtener la bombona activa
            val activeCylinder = gasCylinderRepository.getActiveCylinder().first()
                ?: return Result.failure(Exception("No hay bombona activa configurada"))
            
            // Calcular el combustible disponible
            val fuelKilograms = maxOf(0f, totalWeight - activeCylinder.tare)
            val fuelPercentage = if (activeCylinder.capacity > 0) {
                (fuelKilograms / activeCylinder.capacity * 100).coerceIn(0f, 100f)
            } else {
                0f
            }
            
            // Crear la medición
            val measurement = FuelMeasurement(
                cylinderId = activeCylinder.id,
                cylinderName = activeCylinder.name,
                timestamp = timestamp,
                fuelKilograms = fuelKilograms,
                fuelPercentage = fuelPercentage,
                totalWeight = totalWeight,
                isCalibrated = isCalibrated,
                isHistorical = false
            )
            
            // Validar la medición
            if (!measurement.isValid()) {
                return Result.failure(Exception("Los datos de la medición no son válidos"))
            }
            
            // Guardar la medición
            val id = fuelMeasurementRepository.insertMeasurement(measurement)
            
            Result.success(SaveMeasurementResult(id, true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Guarda una medición histórica (datos provenientes del sensor)
     */
    suspend fun saveHistoricalMeasurement(
        cylinderId: Long,
        totalWeight: Float,
        timestamp: Long,
        isCalibrated: Boolean = true
    ): Result<SaveMeasurementResult> {
        return try {
            // Obtener la información de la bombona
            val cylinder = gasCylinderRepository.getCylinderById(cylinderId)
                ?: return Result.failure(Exception("Bombona no encontrada"))
            
            // Calcular el combustible disponible
            val fuelKilograms = maxOf(0f, totalWeight - cylinder.tare)
            val fuelPercentage = if (cylinder.capacity > 0) {
                (fuelKilograms / cylinder.capacity * 100).coerceIn(0f, 100f)
            } else {
                0f
            }
            
            // Crear la medición
            val measurement = FuelMeasurement(
                cylinderId = cylinder.id,
                cylinderName = cylinder.name,
                timestamp = timestamp,
                fuelKilograms = fuelKilograms,
                fuelPercentage = fuelPercentage,
                totalWeight = totalWeight,
                isCalibrated = isCalibrated,
                isHistorical = true
            )
            
            // Validar la medición
            if (!measurement.isValid()) {
                return Result.failure(Exception("Los datos de la medición no son válidos"))
            }
            
            // Guardar la medición
            val id = fuelMeasurementRepository.insertMeasurement(measurement)
            
            Result.success(SaveMeasurementResult(id, true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Guarda múltiples mediciones históricas de forma eficiente
     */
    suspend fun saveHistoricalMeasurements(
        cylinderId: Long,
        weightMeasurements: List<Pair<Float, Long>>, // Peso total y timestamp
        isCalibrated: Boolean = true
    ): Result<Int> {
        return try {
            // Obtener la información de la bombona
            val cylinder = gasCylinderRepository.getCylinderById(cylinderId)
                ?: return Result.failure(Exception("Bombona no encontrada"))
            
            // Crear las mediciones
            val measurements = weightMeasurements.map { (totalWeight, timestamp) ->
                val fuelKilograms = maxOf(0f, totalWeight - cylinder.tare)
                val fuelPercentage = if (cylinder.capacity > 0) {
                    (fuelKilograms / cylinder.capacity * 100).coerceIn(0f, 100f)
                } else {
                    0f
                }
                
                FuelMeasurement(
                    cylinderId = cylinder.id,
                    cylinderName = cylinder.name,
                    timestamp = timestamp,
                    fuelKilograms = fuelKilograms,
                    fuelPercentage = fuelPercentage,
                    totalWeight = totalWeight,
                    isCalibrated = isCalibrated,
                    isHistorical = true
                )
            }.filter { it.isValid() } // Filtrar solo mediciones válidas
            
            // Guardar todas las mediciones
            fuelMeasurementRepository.insertMeasurements(measurements)
            
            Result.success(measurements.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica si un timestamp específico ya existe en la base de datos
     */
    suspend fun doesTimestampExist(timestamp: Long): Boolean {
        return fuelMeasurementRepository.doesTimestampExist(timestamp)
    }
    
    data class SaveMeasurementResult(
        val measurementId: Long,
        val processed: Boolean
    )
}
