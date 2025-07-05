package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.domain.model.FuelMeasurement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFuelMeasurementsUseCase @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository
) {
    
    /**
     * Obtiene todas las mediciones de combustible ordenadas por timestamp descendente
     */
    fun getAllMeasurements(): Flow<List<FuelMeasurement>> {
        return fuelMeasurementRepository.getAllMeasurements()
    }
    
    /**
     * Obtiene las mediciones de una bombona específica
     */
    fun getMeasurementsByCylinder(cylinderId: Long): Flow<List<FuelMeasurement>> {
        return fuelMeasurementRepository.getMeasurementsByCylinder(cylinderId)
    }
    
    /**
     * Obtiene la última medición en tiempo real
     */
    fun getLatestRealTimeMeasurement(): Flow<FuelMeasurement?> {
        return fuelMeasurementRepository.getLatestRealTimeMeasurement()
    }
    
    /**
     * Obtiene las mediciones históricas de una bombona
     */
    fun getHistoricalMeasurements(cylinderId: Long): Flow<List<FuelMeasurement>> {
        return fuelMeasurementRepository.getHistoricalMeasurements(cylinderId)
    }
    
    /**
     * Obtiene mediciones en un rango de tiempo específico
     */
    fun getMeasurementsByTimeRange(startTime: Long, endTime: Long): Flow<List<FuelMeasurement>> {
        return fuelMeasurementRepository.getMeasurementsByTimeRange(startTime, endTime)
    }
    
    /**
     * Obtiene mediciones de una bombona en un rango de tiempo específico
     */
    fun getMeasurementsByCylinderAndTimeRange(
        cylinderId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<FuelMeasurement>> {
        return fuelMeasurementRepository.getMeasurementsByCylinderAndTimeRange(cylinderId, startTime, endTime)
    }
    
    /**
     * Obtiene las mediciones más recientes (limitadas)
     */
    fun getRecentMeasurements(limit: Int = 50): Flow<List<FuelMeasurement>> {
        return fuelMeasurementRepository.getRecentMeasurements(limit)
    }
    
    /**
     * Obtiene el consumo promedio de una bombona en un periodo de tiempo
     */
    suspend fun getAverageFuelConsumption(cylinderId: Long, startTime: Long, endTime: Long): Float? {
        return fuelMeasurementRepository.getAverageFuelConsumption(cylinderId, startTime, endTime)
    }
    
    /**
     * Obtiene el conteo de mediciones para una bombona específica
     */
    suspend fun getMeasurementCountByCylinder(cylinderId: Long): Int {
        return fuelMeasurementRepository.getMeasurementCountByCylinder(cylinderId)
    }
    
    /**
     * Obtiene las últimas dos mediciones de una bombona para calcular tendencias
     */
    suspend fun getLastTwoMeasurements(cylinderId: Long): List<FuelMeasurement> {
        return fuelMeasurementRepository.getLastTwoMeasurements(cylinderId)
    }
    
    /**
     * Calcula la tendencia de consumo basada en las últimas dos mediciones
     */
    suspend fun getFuelConsumptionTrend(cylinderId: Long): FuelTrend? {
        val lastTwoMeasurements = getLastTwoMeasurements(cylinderId)
        
        if (lastTwoMeasurements.size < 2) {
            return null
        }
        
        val latest = lastTwoMeasurements[0]
        val previous = lastTwoMeasurements[1]
        
        val timeDifferenceHours = (latest.timestamp - previous.timestamp) / (1000 * 60 * 60).toFloat()
        val fuelDifference = previous.fuelKilograms - latest.fuelKilograms // Consumo positivo
        
        if (timeDifferenceHours <= 0) {
            return null
        }
        
        val consumptionRatePerHour = fuelDifference / timeDifferenceHours
        
        return FuelTrend(
            consumptionRatePerHour = consumptionRatePerHour,
            fuelRemaining = latest.fuelKilograms,
            percentageRemaining = latest.fuelPercentage,
            estimatedHoursRemaining = if (consumptionRatePerHour > 0) {
                latest.fuelKilograms / consumptionRatePerHour
            } else null,
            trend = when {
                consumptionRatePerHour > 0.1 -> TrendDirection.DECREASING_FAST
                consumptionRatePerHour > 0.01 -> TrendDirection.DECREASING
                consumptionRatePerHour > -0.01 -> TrendDirection.STABLE
                else -> TrendDirection.INCREASING
            }
        )
    }
    
    data class FuelTrend(
        val consumptionRatePerHour: Float, // kg/hora
        val fuelRemaining: Float, // kg
        val percentageRemaining: Float, // %
        val estimatedHoursRemaining: Float?, // horas hasta vacío
        val trend: TrendDirection
    )
    
    enum class TrendDirection {
        INCREASING,
        STABLE, 
        DECREASING,
        DECREASING_FAST
    }
}
