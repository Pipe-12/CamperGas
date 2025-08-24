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
     * Calcula el total de gas consumido en una lista de mediciones
     */
    fun calculateTotalConsumption(consumptions: List<Consumption>): Float {
        if (consumptions.isEmpty()) return 0f
        
        // Agrupar por cilindro y calcular el consumo para cada uno
        return consumptions.groupBy { it.cylinderId }
            .map { (_, cylinderConsumptions) ->
                val sortedConsumptions = cylinderConsumptions.sortedByDescending { it.date }
                if (sortedConsumptions.size < 2) return@map 0f
                
                // Calcular la diferencia entre la primera y última medición del período
                val firstMeasurement = sortedConsumptions.first()  // Más reciente
                val lastMeasurement = sortedConsumptions.last()    // Más antigua
                
                // El consumo es la diferencia: medición inicial - medición final
                // Como se crea un nuevo cilindro al rellenar, no debería haber valores negativos
                lastMeasurement.fuelKilograms - firstMeasurement.fuelKilograms
            }
            .sum()
    }

    /**
     * Prepara datos para gráfico agrupando consumos por día
     */
    fun prepareChartData(consumptions: List<Consumption>): List<ChartDataPoint> {
        if (consumptions.isEmpty()) return emptyList()
        
        val calendar = Calendar.getInstance()
        
        // Agrupar por día
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
 * Representa un punto de datos para el gráfico
 */
data class ChartDataPoint(
    val date: Long,      // timestamp del día
    val kilograms: Float // total de kg consumidos ese día
)
