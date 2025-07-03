package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.ConsumptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetConsumptionStatsUseCase @Inject constructor(
    private val consumptionRepository: ConsumptionRepository,
    private val smartConsumptionTracker: SmartConsumptionTracker
) {
    
    /**
     * Obtiene estadísticas de consumo para todas las bombonas
     */
    fun getAllConsumptionStats(): Flow<List<CylinderConsumptionStats>> {
        return consumptionRepository.getAllConsumptions().map { consumptions ->
            val groupedByCylinder = consumptions.groupBy { it.cylinderId }
            
            groupedByCylinder.map { (cylinderId, cylinderConsumptions) ->
                val sortedConsumptions = cylinderConsumptions.sortedByDescending { it.date }
                val latest = sortedConsumptions.firstOrNull()
                val oldest = sortedConsumptions.lastOrNull()
                
                CylinderConsumptionStats(
                    cylinderId = cylinderId,
                    cylinderName = latest?.cylinderName ?: "Bombona desconocida",
                    totalRecords = cylinderConsumptions.size,
                    latestFuelPercentage = latest?.fuelPercentage,
                    latestFuelKilograms = latest?.fuelKilograms,
                    lastRecordTime = latest?.date,
                    oldestRecordTime = oldest?.date,
                    fuelTrend = calculateFuelTrend(sortedConsumptions)
                )
            }.sortedByDescending { it.lastRecordTime }
        }
    }
    
    /**
     * Obtiene estadísticas de una bombona específica
     */
    suspend fun getCylinderStats(cylinderId: Long): CylinderDetailedStats? {
        val consumptions = consumptionRepository.getConsumptionsByCylinder(cylinderId)
            .firstOrNull() ?: return null
            
        if (consumptions.isEmpty()) return null
        
        val sorted = consumptions.sortedByDescending { it.date }
        val trackingStats = smartConsumptionTracker.getTrackingStats(cylinderId)
        
        return CylinderDetailedStats(
            cylinderId = cylinderId,
            cylinderName = sorted.first().cylinderName,
            totalRecords = consumptions.size,
            latestRecord = sorted.firstOrNull(),
            oldestRecord = sorted.lastOrNull(),
            averageFuelPercentage = consumptions.map { it.fuelPercentage }.average().toFloat(),
            fuelTrend = calculateFuelTrend(sorted),
            recordingFrequency = calculateRecordingFrequency(sorted),
            trackingStats = trackingStats
        )
    }
    
    /**
     * Calcula la tendencia del combustible (INCREASING, DECREASING, STABLE)
     */
    private fun calculateFuelTrend(sortedConsumptions: List<com.example.campergas.domain.model.Consumption>): FuelTrend {
        if (sortedConsumptions.size < 2) return FuelTrend.STABLE
        
        val recent = sortedConsumptions.take(5) // Últimos 5 registros
        if (recent.size < 2) return FuelTrend.STABLE
        
        val newest = recent.first().fuelPercentage
        val older = recent.last().fuelPercentage
        val difference = newest - older
        
        return when {
            difference > 2.0f -> FuelTrend.INCREASING
            difference < -2.0f -> FuelTrend.DECREASING
            else -> FuelTrend.STABLE
        }
    }
    
    /**
     * Calcula la frecuencia promedio de registros
     */
    private fun calculateRecordingFrequency(sortedConsumptions: List<com.example.campergas.domain.model.Consumption>): Long {
        if (sortedConsumptions.size < 2) return 0L
        
        val intervals = mutableListOf<Long>()
        for (i in 0 until sortedConsumptions.size - 1) {
            val current = sortedConsumptions[i].date
            val next = sortedConsumptions[i + 1].date
            intervals.add(current - next)
        }
        
        return if (intervals.isNotEmpty()) {
            intervals.average().toLong() / (60 * 1000) // Convertir a minutos
        } else 0L
    }
}

data class CylinderConsumptionStats(
    val cylinderId: Long,
    val cylinderName: String,
    val totalRecords: Int,
    val latestFuelPercentage: Float?,
    val latestFuelKilograms: Float?,
    val lastRecordTime: Long?,
    val oldestRecordTime: Long?,
    val fuelTrend: FuelTrend
)

data class CylinderDetailedStats(
    val cylinderId: Long,
    val cylinderName: String,
    val totalRecords: Int,
    val latestRecord: com.example.campergas.domain.model.Consumption?,
    val oldestRecord: com.example.campergas.domain.model.Consumption?,
    val averageFuelPercentage: Float,
    val fuelTrend: FuelTrend,
    val recordingFrequency: Long, // En minutos
    val trackingStats: TrackingStats
)

enum class FuelTrend {
    INCREASING,
    DECREASING,
    STABLE
}
