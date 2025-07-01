package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.Weight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveWeightMeasurementUseCase @Inject constructor(
    private val repository: GasCylinderRepository
) {
    
    /**
     * Guarda una medición de peso en tiempo real
     */
    suspend fun saveRealTimeMeasurement(
        value: Float,
        timestamp: Long = System.currentTimeMillis()
    ): Result<Long> {
        return try {
            val id = repository.saveWeightMeasurement(
                value = value,
                timestamp = timestamp,
                isHistorical = false
            )
            if (id != null) {
                Result.success(id)
            } else {
                Result.failure(Exception("Error al guardar la medición"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Guarda mediciones históricas del sensor
     */
    suspend fun saveHistoricalMeasurements(
        measurements: List<Pair<Float, Long>>
    ): Result<Unit> {
        return try {
            repository.saveHistoricalMeasurements(measurements)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las mediciones más recientes
     */
    fun getRecentMeasurements(limit: Int = 50): Flow<List<Weight>> {
        return repository.getRecentMeasurements(limit)
    }
    
    /**
     * Obtiene las mediciones de una bombona específica
     */
    fun getMeasurementsByCylinder(cylinderId: Long): Flow<List<Weight>> {
        return repository.getMeasurementsByCylinder(cylinderId)
    }
}
