package com.example.campergas.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow

@Dao
interface GasCylinderDao {

    @Query("SELECT * FROM gas_cylinders ORDER BY createdAt DESC")
    fun getAllCylinders(): Flow<List<GasCylinder>>

    @Query("SELECT * FROM gas_cylinders ORDER BY createdAt DESC")
    suspend fun getAllCylindersSync(): List<GasCylinder>

    @Query("SELECT * FROM gas_cylinders WHERE isActive = 1 LIMIT 1")
    fun getActiveCylinder(): Flow<GasCylinder?>

    @Query("SELECT * FROM gas_cylinders WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCylinderSync(): GasCylinder?

    @Query("SELECT * FROM gas_cylinders WHERE id = :id")
    suspend fun getCylinderById(id: Long): GasCylinder?

    @Insert
    suspend fun insertCylinder(cylinder: GasCylinder): Long

    @Update
    suspend fun updateCylinder(cylinder: GasCylinder)

    @Query("UPDATE gas_cylinders SET isActive = 0")
    suspend fun deactivateAllCylinders()

    @Transaction
    suspend fun setActiveCylinder(cylinderId: Long) {
        deactivateAllCylinders()
        updateCylinderActiveStatus(cylinderId, true)
    }

    @Query("UPDATE gas_cylinders SET isActive = :isActive WHERE id = :id")
    suspend fun updateCylinderActiveStatus(id: Long, isActive: Boolean)
}
