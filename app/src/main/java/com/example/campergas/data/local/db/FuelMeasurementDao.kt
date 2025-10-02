package com.example.campergas.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelMeasurementDao {

    @Query("SELECT * FROM fuel_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<FuelMeasurementEntity>>

    @Query("SELECT * FROM fuel_measurements WHERE cylinderId = :cylinderId ORDER BY timestamp DESC")
    fun getMeasurementsByCylinder(cylinderId: Long): Flow<List<FuelMeasurementEntity>>

    @Query("SELECT * FROM fuel_measurements WHERE isHistorical = 0 ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRealTimeMeasurement(): Flow<FuelMeasurementEntity?>

    @Query("SELECT * FROM fuel_measurements WHERE isHistorical = 1 AND cylinderId = :cylinderId ORDER BY timestamp DESC")
    fun getHistoricalMeasurements(cylinderId: Long): Flow<List<FuelMeasurementEntity>>

    @Query("SELECT * FROM fuel_measurements WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMeasurementsByTimeRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<FuelMeasurementEntity>>

    @Query(
        """
        SELECT * FROM fuel_measurements 
        WHERE cylinderId = :cylinderId AND timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp DESC
    """
    )
    fun getMeasurementsByCylinderAndTimeRange(
        cylinderId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<FuelMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: FuelMeasurementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<FuelMeasurementEntity>)

    @Update
    suspend fun updateMeasurement(measurement: FuelMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: FuelMeasurementEntity)

    @Query("DELETE FROM fuel_measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)

    @Query("DELETE FROM fuel_measurements WHERE cylinderId = :cylinderId")
    suspend fun deleteMeasurementsByCylinder(cylinderId: Long)

    @Query("DELETE FROM fuel_measurements WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldMeasurements(beforeTimestamp: Long)

    @Query("DELETE FROM fuel_measurements")
    suspend fun deleteAllMeasurements()

    @Query("SELECT COUNT(*) FROM fuel_measurements WHERE cylinderId = :cylinderId")
    suspend fun getMeasurementCountByCylinder(cylinderId: Long): Int

    @Query("SELECT * FROM fuel_measurements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMeasurements(limit: Int): Flow<List<FuelMeasurementEntity>>

    @Query("SELECT AVG(fuelKilograms) FROM fuel_measurements WHERE cylinderId = :cylinderId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getAverageFuelConsumption(cylinderId: Long, startTime: Long, endTime: Long): Float?

    @Query(
        """
        SELECT * FROM fuel_measurements 
        WHERE cylinderId = :cylinderId 
        ORDER BY timestamp DESC 
        LIMIT 2
    """
    )
    suspend fun getLastTwoMeasurements(cylinderId: Long): List<FuelMeasurementEntity>

    @Query(
        """
        SELECT * FROM fuel_measurements 
        WHERE cylinderId = :cylinderId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """
    )
    suspend fun getLastNMeasurements(cylinderId: Long, limit: Int): List<FuelMeasurementEntity>
}
