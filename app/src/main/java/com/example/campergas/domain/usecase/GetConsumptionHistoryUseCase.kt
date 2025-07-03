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
        return consumptionRepository.getConsumptionsByCylinderAndDateRange(cylinderId, startDate, endDate)
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
}
