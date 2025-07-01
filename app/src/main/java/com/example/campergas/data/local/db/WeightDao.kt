package com.example.campergas.data.local.db

import androidx.room.*
import com.example.campergas.domain.model.Weight
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    
    @Query("SELECT * FROM weight_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<Weight>>
    
    @Query("SELECT * FROM weight_measurements WHERE cylinderId = :cylinderId ORDER BY timestamp DESC")
    fun getMeasurementsByCylinder(cylinderId: Long): Flow<List<Weight>>
    
    @Query("SELECT * FROM weight_measurements WHERE isHistorical = 0 ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRealTimeMeasurement(): Flow<Weight?>
    
    @Query("SELECT * FROM weight_measurements WHERE isHistorical = 1 AND cylinderId = :cylinderId ORDER BY timestamp DESC")
    fun getHistoricalMeasurements(cylinderId: Long): Flow<List<Weight>>
    
    @Query("SELECT * FROM weight_measurements WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMeasurementsByTimeRange(startTime: Long, endTime: Long): Flow<List<Weight>>
    
    @Insert
    suspend fun insertMeasurement(weight: Weight): Long
    
    @Insert
    suspend fun insertMeasurements(weights: List<Weight>)
    
    @Update
    suspend fun updateMeasurement(weight: Weight)
    
    @Delete
    suspend fun deleteMeasurement(weight: Weight)
    
    @Query("DELETE FROM weight_measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)
    
    // Función para limpiar mediciones antiguas (útil para mantener el rendimiento)
    @Query("DELETE FROM weight_measurements WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldMeasurements(beforeTimestamp: Long)
    
    @Query("SELECT COUNT(*) FROM weight_measurements WHERE cylinderId = :cylinderId")
    suspend fun getMeasurementCountByCylinder(cylinderId: Long): Int
    
    @Query("SELECT * FROM weight_measurements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMeasurements(limit: Int): Flow<List<Weight>>
}
