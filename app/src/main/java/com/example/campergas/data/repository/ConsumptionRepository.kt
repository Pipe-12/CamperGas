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
    
    fun getConsumptionsByCylinder(cylinderId: Long): Flow<List<Consumption>> {
        return consumptionDao.getConsumptionsByCylinder(cylinderId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getConsumptionsByDateRange(startDate: Long, endDate: Long): Flow<List<Consumption>> {
        return consumptionDao.getConsumptionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getConsumptionsByCylinderAndDateRange(
        cylinderId: Long, 
        startDate: Long, 
        endDate: Long
    ): Flow<List<Consumption>> {
        return consumptionDao.getConsumptionsByCylinderAndDateRange(cylinderId, startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun saveConsumption(consumption: Consumption) {
        consumptionDao.insertConsumption(consumption.toEntity())
    }
    
    suspend fun clearAllConsumptions() {
        consumptionDao.deleteAllConsumptions()
    }
    
    suspend fun clearConsumptionsByCylinder(cylinderId: Long) {
        consumptionDao.deleteConsumptionsByCylinder(cylinderId)
    }
    
    private fun ConsumptionEntity.toDomainModel(): Consumption {
        return Consumption(
            id = this.id,
            cylinderId = this.cylinderId,
            cylinderName = this.cylinderName,
            date = this.date,
            fuelPercentage = this.fuelPercentage,
            fuelKilograms = this.fuelKilograms,
            duration = this.duration
        )
    }
    
    private fun Consumption.toEntity(): ConsumptionEntity {
        return ConsumptionEntity(
            id = this.id,
            cylinderId = this.cylinderId,
            cylinderName = this.cylinderName,
            date = this.date,
            fuelPercentage = this.fuelPercentage,
            fuelKilograms = this.fuelKilograms,
            duration = this.duration
        )
    }
}
