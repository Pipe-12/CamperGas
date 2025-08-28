package com.example.campergas.data.repository

import com.example.campergas.data.local.db.FuelMeasurementDao
import com.example.campergas.data.local.db.FuelMeasurementEntity
import com.example.campergas.domain.model.FuelMeasurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FuelMeasurementRepository @Inject constructor(
    private val fuelMeasurementDao: FuelMeasurementDao
) {
    fun getAllMeasurements(): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getAllMeasurements().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getMeasurementsByCylinder(cylinderId: Long): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getMeasurementsByCylinder(cylinderId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getLatestRealTimeMeasurement(): Flow<FuelMeasurement?> {
        return fuelMeasurementDao.getLatestRealTimeMeasurement().map { entity ->
            entity?.toDomainModel()
        }
    }

    fun getHistoricalMeasurements(cylinderId: Long): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getHistoricalMeasurements(cylinderId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getMeasurementsByTimeRange(startTime: Long, endTime: Long): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getMeasurementsByTimeRange(startTime, endTime).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getMeasurementsByCylinderAndTimeRange(
        cylinderId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getMeasurementsByCylinderAndTimeRange(
            cylinderId,
            startTime,
            endTime
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getRecentMeasurements(limit: Int): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getRecentMeasurements(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun insertMeasurement(measurement: FuelMeasurement): Long {
        return fuelMeasurementDao.insertMeasurement(measurement.toEntity())
    }

    suspend fun insertMeasurements(measurements: List<FuelMeasurement>) {
        fuelMeasurementDao.insertMeasurements(measurements.map { it.toEntity() })
    }


    suspend fun deleteMeasurementsByCylinder(cylinderId: Long) {
        fuelMeasurementDao.deleteMeasurementsByCylinder(cylinderId)
    }

    suspend fun deleteOldMeasurements(beforeTimestamp: Long) {
        fuelMeasurementDao.deleteOldMeasurements(beforeTimestamp)
    }

    suspend fun deleteAllMeasurements() {
        fuelMeasurementDao.deleteAllMeasurements()
    }

    suspend fun getMeasurementCountByCylinder(cylinderId: Long): Int {
        return fuelMeasurementDao.getMeasurementCountByCylinder(cylinderId)
    }


    suspend fun getAverageFuelConsumption(
        cylinderId: Long,
        startTime: Long,
        endTime: Long
    ): Float? {
        return fuelMeasurementDao.getAverageFuelConsumption(cylinderId, startTime, endTime)
    }

    suspend fun getLastTwoMeasurements(cylinderId: Long): List<FuelMeasurement> {
        return fuelMeasurementDao.getLastTwoMeasurements(cylinderId).map { it.toDomainModel() }
    }

    suspend fun getLastNMeasurements(cylinderId: Long, limit: Int): List<FuelMeasurement> {
        return fuelMeasurementDao.getLastNMeasurements(cylinderId, limit).map { it.toDomainModel() }
    }

    suspend fun deleteMeasurementById(id: Long) {
        fuelMeasurementDao.deleteMeasurementById(id)
    }

    private fun FuelMeasurementEntity.toDomainModel(): FuelMeasurement {
        return FuelMeasurement(
            id = this.id,
            cylinderId = this.cylinderId,
            cylinderName = this.cylinderName,
            timestamp = this.timestamp,
            fuelKilograms = this.fuelKilograms,
            fuelPercentage = this.fuelPercentage,
            totalWeight = this.totalWeight,
            isCalibrated = this.isCalibrated,
            isHistorical = this.isHistorical
        )
    }

    private fun FuelMeasurement.toEntity(): FuelMeasurementEntity {
        return FuelMeasurementEntity(
            id = this.id,
            cylinderId = this.cylinderId,
            cylinderName = this.cylinderName,
            timestamp = this.timestamp,
            fuelKilograms = this.fuelKilograms,
            fuelPercentage = this.fuelPercentage,
            totalWeight = this.totalWeight,
            isCalibrated = this.isCalibrated,
            isHistorical = this.isHistorical
        )
    }
}
