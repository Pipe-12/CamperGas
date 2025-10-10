package com.example.campergas.data.repository

import com.example.campergas.data.local.db.GasCylinderDao
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GasCylinderRepository @Inject constructor(
    private val gasCylinderDao: GasCylinderDao
) {

    // CRUD operaciones for bombonas
    fun getAllCylinders(): Flow<List<GasCylinder>> = gasCylinderDao.getAllCylinders()

    fun getActiveCylinder(): Flow<GasCylinder?> = gasCylinderDao.getActiveCylinder()

    suspend fun getActiveCylinderSync(): GasCylinder? = gasCylinderDao.getActiveCylinderSync()

    suspend fun getCylinderById(id: Long): GasCylinder? = gasCylinderDao.getCylinderById(id)

    suspend fun getAllCylindersSync(): List<GasCylinder> = gasCylinderDao.getAllCylindersSync()

    suspend fun insertCylinder(cylinder: GasCylinder): Long {
        return gasCylinderDao.insertCylinder(cylinder)
    }


    suspend fun setActiveCylinder(cylinderId: Long) {
        gasCylinderDao.setActiveCylinder(cylinderId)
    }


}
