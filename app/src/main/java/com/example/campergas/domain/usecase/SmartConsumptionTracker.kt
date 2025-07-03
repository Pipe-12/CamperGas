package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.ConsumptionRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.Consumption
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartConsumptionTracker @Inject constructor(
    private val consumptionRepository: ConsumptionRepository,
    private val gasCylinderRepository: GasCylinderRepository,
    private val saveConsumptionUseCase: SaveConsumptionUseCase
) {
    
    companion object {
        private const val FUEL_PERCENTAGE_THRESHOLD = 1.0f // 1% de cambio
        private const val TIME_THRESHOLD_MINUTES = 15L // 15 minutos
        private const val TIME_THRESHOLD_MILLIS = TIME_THRESHOLD_MINUTES * 60 * 1000L
    }
    
    /**
     * Evalúa si debe guardarse un nuevo registro de consumo basado en cambios significativos
     */
    suspend fun evaluateAndSaveConsumption(
        cylinderId: Long,
        totalWeight: Float,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean {
        try {
            // Obtener la bombona para calcular el porcentaje actual
            val cylinder = gasCylinderRepository.getGasCylinderById(cylinderId) ?: return false
            val currentFuelPercentage = cylinder.calculateGasPercentage(totalWeight)
            
            // Obtener la última medición de esta bombona
            val lastConsumption = getLastConsumptionForCylinder(cylinderId)
            
            val shouldSave = if (lastConsumption == null) {
                // Si no hay mediciones previas, guardar la primera
                true
            } else {
                // Verificar criterios de cambio significativo
                val percentageChange = kotlin.math.abs(currentFuelPercentage - lastConsumption.fuelPercentage)
                val timeDifference = timestamp - lastConsumption.date
                
                // Guardar si hay cambio >= 1% o han pasado >= 15 minutos
                percentageChange >= FUEL_PERCENTAGE_THRESHOLD || timeDifference >= TIME_THRESHOLD_MILLIS
            }
            
            if (shouldSave) {
                saveConsumptionUseCase.invoke(
                    cylinderId = cylinderId,
                    totalWeight = totalWeight,
                    durationMinutes = 0,
                    customTimestamp = timestamp
                )
                return true
            }
            
            return false
            
        } catch (e: Exception) {
            // En caso de error, no guardar y registrar el error
            return false
        }
    }
    
    /**
     * Procesa múltiples mediciones históricas aplicando la lógica inteligente
     */
    suspend fun processHistoricalMeasurements(
        cylinderId: Long,
        measurements: List<Pair<Float, Long>>
    ): Int {
        var savedCount = 0
        
        // Ordenar mediciones por tiempo
        val sortedMeasurements = measurements.sortedBy { it.second }
        
        for ((weight, timestamp) in sortedMeasurements) {
            if (evaluateAndSaveConsumption(cylinderId, weight, timestamp)) {
                savedCount++
            }
        }
        
        return savedCount
    }
    
    /**
     * Obtiene la última medición de consumo para una bombona específica
     */
    private suspend fun getLastConsumptionForCylinder(cylinderId: Long): Consumption? {
        return try {
            consumptionRepository.getConsumptionsByCylinder(cylinderId)
                .firstOrNull()
                ?.firstOrNull() // Tomar el primer elemento (más reciente)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Obtiene estadísticas del tracking inteligente
     */
    suspend fun getTrackingStats(cylinderId: Long): TrackingStats {
        val allConsumptions = consumptionRepository.getConsumptionsByCylinder(cylinderId)
            .firstOrNull() ?: emptyList()
        
        return TrackingStats(
            totalRecords = allConsumptions.size,
            lastRecordTime = allConsumptions.firstOrNull()?.date,
            oldestRecordTime = allConsumptions.lastOrNull()?.date
        )
    }
}

data class TrackingStats(
    val totalRecords: Int,
    val lastRecordTime: Long?,
    val oldestRecordTime: Long?
)
