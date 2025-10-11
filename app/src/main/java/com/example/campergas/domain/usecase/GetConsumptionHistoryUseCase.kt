package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.ConsumptionRepository
import com.example.campergas.domain.model.Consumption
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class GetConsumptionHistoryUseCase @Inject constructor(
    private val consumptionRepository: ConsumptionRepository
) {
    operator fun invoke(startDate: Long? = null, endDate: Long? = null): Flow<List<Consumption>> {
        return if (startDate != null && endDate != null) {
            consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
        } else {
            consumptionRepository.getAllConsumptions()
        }
    }

    fun getConsumptionsByCylinder(cylinderId: Long): Flow<List<Consumption>> {
        return consumptionRepository.getConsumptionsByCylinder(cylinderId)
    }

    fun getConsumptionsByCylinderAndDateRange(
        cylinderId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<Consumption>> {
        return consumptionRepository.getConsumptionsByCylinderAndDateRange(
            cylinderId,
            startDate,
            endDate
        )
    }

    fun getLastWeekConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    fun getLastMonthConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    fun getLastDayConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    /**
     * Calculates el total de gas consumido en una lista de mediciones
     */
    fun calculateTotalConsumption(consumptions: List<Consumption>): Float {
        if (consumptions.isEmpty()) return 0f
        
        // Agrupar por cilindro y calcular el consumption for cada uno
        return consumptions.groupBy { it.cylinderId }
            .map { (_, cylinderConsumptions) ->
                val sortedConsumptions = cylinderConsumptions.sortedByDescending { it.date }
                if (sortedConsumptions.size < 2) return@map 0f
                
                // Calculate difference between first and last measurement of period
                val firstMeasurement = sortedConsumptions.first()  // Most recent
                val lastMeasurement = sortedConsumptions.last()    // Oldest
                
                // El consumption es la diferencia: measurement inicial - measurement final
                val calculatedConsumption = lastMeasurement.fuelKilograms - firstMeasurement.fuelKilograms
                
                // Evitar valores negativos (puede ocurrir durante recargas de cylinders)
                // En caso de recarga, el consumption se considera 0 for ese period
                kotlin.math.max(0f, calculatedConsumption)
            }
            .sum()
    }

    /**
     * Prepare data for chart grouping consumptions by day
     */
    fun prepareChartData(consumptions: List<Consumption>): List<ChartDataPoint> {
        if (consumptions.isEmpty()) return emptyList()
        
        val calendar = Calendar.getInstance()
        
        // Group by day
        return consumptions.groupBy { consumption ->
            calendar.timeInMillis = consumption.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.map { (day, dayConsumptions) ->
            val totalConsumed = calculateTotalConsumption(dayConsumptions)
            ChartDataPoint(day, totalConsumed)
        }.sortedBy { it.date }
    }
}

/**
 * Representa un punto of data for the chart
 */
data class ChartDataPoint(
    val date: Long,      // day timestamp
    val kilograms: Float // total kg consumed that day
)
