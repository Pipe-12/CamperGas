package com.example.campergas.data.repository

import com.example.campergas.data.local.db.ConsumptionDao
import com.example.campergas.data.local.db.ConsumptionEntity
import com.example.campergas.domain.model.Consumption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsumptionRepository @Inject constructor(
    private val consumptionDao: ConsumptionDao
) {
    fun getAllConsumptions(): Flow<List<Consumption>> {
        return consumptionDao.getAllConsumptions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getConsumptionsByDateRange(startDate: Long, endDate: Long): Flow<List<Consumption>> {
        return consumptionDao.getConsumptionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun saveConsumption(consumption: Consumption) {
        consumptionDao.insertConsumption(consumption.toEntity())
    }
    
    suspend fun clearAllConsumptions() {
        consumptionDao.deleteAllConsumptions()
    }
    
    private fun ConsumptionEntity.toDomainModel(): Consumption {
        return Consumption(
            id = this.id,
            date = this.date,
            initialWeight = this.initialWeight,
            finalWeight = this.finalWeight,
            consumptionValue = this.consumptionValue,
            duration = this.duration
        )
    }
    
    private fun Consumption.toEntity(): ConsumptionEntity {
        return ConsumptionEntity(
            id = this.id,
            date = this.date,
            initialWeight = this.initialWeight,
            finalWeight = this.finalWeight,
            consumptionValue = this.consumptionValue,
            duration = this.duration
        )
    }
}
