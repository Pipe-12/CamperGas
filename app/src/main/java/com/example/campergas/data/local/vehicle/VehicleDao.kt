package com.example.campergas.data.local.vehicle

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleConfig(config: VehicleConfigEntity)

    @Update
    suspend fun updateVehicleConfig(config: VehicleConfigEntity): Int

    @Query("SELECT * FROM vehicle_config WHERE id = :id")
    fun getVehicleConfig(id: String = "default_config"): Flow<VehicleConfigEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM vehicle_config WHERE id = :id)")
    suspend fun configExists(id: String = "default_config"): Boolean
}
