package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.Weight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveWeightMeasurementUseCase @Inject constructor(
    private val repository: GasCylinderRepository,
    private val smartConsumptionTracker: SmartConsumptionTracker
) {
    
    /**
     * Guarda una medición de peso en tiempo real y evalúa si registrar el consumo
     */
    suspend fun saveRealTimeMeasurement(
        value: Float,
        timestamp: Long = System.currentTimeMillis()
    ): Result<SaveMeasurementResult> {
        return try {
            val id = repository.saveWeightMeasurement(
                value = value,
                timestamp = timestamp,
                isHistorical = false
            )
            
            if (id != null) {
                // Obtener la bombona activa y evaluar si guardar el registro de consumo
                val activeCylinder = repository.getActiveCylinderSync()
                var consumptionSaved = false
                
                if (activeCylinder != null) {
                    // Usar el tracker inteligente para decidir si guardar
                    consumptionSaved = smartConsumptionTracker.evaluateAndSaveConsumption(
                        cylinderId = activeCylinder.id,
                        totalWeight = value,
                        timestamp = timestamp
                    )
                }
                
                Result.success(SaveMeasurementResult(id, consumptionSaved))
            } else {
                Result.failure(Exception("Error al guardar la medición"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Guarda mediciones históricas del sensor usando lógica inteligente
     */
    suspend fun saveHistoricalMeasurements(
        measurements: List<Pair<Float, Long>>
    ): Result<HistoricalSaveResult> {
        return try {
            repository.saveHistoricalMeasurements(measurements)
            
            // Procesar mediciones históricas con lógica inteligente
            val activeCylinder = repository.getActiveCylinderSync()
            var consumptionsSaved = 0
            
            if (activeCylinder != null) {
                consumptionsSaved = smartConsumptionTracker.processHistoricalMeasurements(
                    cylinderId = activeCylinder.id,
                    measurements = measurements
                )
            }
            
            Result.success(
                HistoricalSaveResult(
                    measurementsSaved = measurements.size,
                    consumptionsSaved = consumptionsSaved
                )
            )
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

/**
 * Resultado del guardado de una medición en tiempo real
 */
data class SaveMeasurementResult(
    val measurementId: Long,
    val consumptionSaved: Boolean
)

/**
 * Resultado del guardado de mediciones históricas
 */
data class HistoricalSaveResult(
    val measurementsSaved: Int,
    val consumptionsSaved: Int
)
