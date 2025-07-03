package com.example.campergas.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ConsumptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsumption(consumption: ConsumptionEntity)
    
    @Query("SELECT * FROM consumption_table ORDER BY date DESC")
    fun getAllConsumptions(): Flow<List<ConsumptionEntity>>
    
    @Query("SELECT * FROM consumption_table WHERE cylinderId = :cylinderId ORDER BY date DESC")
    fun getConsumptionsByCylinder(cylinderId: Long): Flow<List<ConsumptionEntity>>
    
    @Query("""
        SELECT * FROM consumption_table 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getConsumptionsByDateRange(startDate: Long, endDate: Long): Flow<List<ConsumptionEntity>>
    
    @Query("""
        SELECT * FROM consumption_table 
        WHERE cylinderId = :cylinderId AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getConsumptionsByCylinderAndDateRange(
        cylinderId: Long, 
        startDate: Long, 
        endDate: Long
    ): Flow<List<ConsumptionEntity>>
    
    @Query("DELETE FROM consumption_table")
    suspend fun deleteAllConsumptions()
    
    @Query("DELETE FROM consumption_table WHERE cylinderId = :cylinderId")
    suspend fun deleteConsumptionsByCylinder(cylinderId: Long)
}
