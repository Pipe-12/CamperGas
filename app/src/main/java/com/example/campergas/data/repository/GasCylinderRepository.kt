package com.example.campergas.data.repository

import com.example.campergas.data.local.db.GasCylinderDao
import com.example.campergas.data.local.db.WeightDao
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.model.Weight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GasCylinderRepository @Inject constructor(
    private val gasCylinderDao: GasCylinderDao,
    private val weightDao: WeightDao
) {
    
    // CRUD operaciones para bombonas
    fun getAllCylinders(): Flow<List<GasCylinder>> = gasCylinderDao.getAllCylinders()
    
    fun getActiveCylinder(): Flow<GasCylinder?> = gasCylinderDao.getActiveCylinder()
    
    suspend fun getActiveCylinderSync(): GasCylinder? = gasCylinderDao.getActiveCylinderSync()
    
    suspend fun getCylinderById(id: Long): GasCylinder? = gasCylinderDao.getCylinderById(id)
    
    suspend fun getGasCylinderById(id: Long): GasCylinder? = gasCylinderDao.getCylinderById(id)
    
    suspend fun insertCylinder(cylinder: GasCylinder): Long {
        return gasCylinderDao.insertCylinder(cylinder)
    }
    
    suspend fun updateCylinder(cylinder: GasCylinder) {
        gasCylinderDao.updateCylinder(cylinder)
    }
    
    suspend fun setActiveCylinder(cylinderId: Long) {
        gasCylinderDao.setActiveCylinder(cylinderId)
    }
    
    suspend fun deactivateAllCylinders() {
        gasCylinderDao.deactivateAllCylinders()
    }
    
    fun searchCylinders(searchTerm: String): Flow<List<GasCylinder>> {
        return gasCylinderDao.searchCylinders(searchTerm)
    }
    
    suspend fun getCylinderCount(): Int = gasCylinderDao.getCylinderCount()
    
    // Operaciones para mediciones de peso
    fun getAllMeasurements(): Flow<List<Weight>> = weightDao.getAllMeasurements()
    
    fun getMeasurementsByCylinder(cylinderId: Long): Flow<List<Weight>> {
        return weightDao.getMeasurementsByCylinder(cylinderId)
    }
    
    suspend fun insertWeightMeasurement(weight: Weight): Long {
        return weightDao.insertMeasurement(weight)
    }
    
    suspend fun insertHistoricalMeasurements(weights: List<Weight>) {
        weightDao.insertMeasurements(weights)
    }
    
    fun getLatestRealTimeMeasurement(): Flow<Weight?> {
        return weightDao.getLatestRealTimeMeasurement()
    }
    
    fun getHistoricalMeasurements(cylinderId: Long): Flow<List<Weight>> {
        return weightDao.getHistoricalMeasurements(cylinderId)
    }
    
    fun getRecentMeasurements(limit: Int = 50): Flow<List<Weight>> {
        return weightDao.getRecentMeasurements(limit)
    }
    
    suspend fun deleteOldMeasurements(beforeTimestamp: Long) {
        weightDao.deleteOldMeasurements(beforeTimestamp)
    }
    
    suspend fun doesTimestampExist(timestamp: Long): Boolean {
        return weightDao.doesTimestampExist(timestamp) > 0
    }
    
    /**
     * Guarda una medición de peso asociándola automáticamente con la bombona activa
     */
    suspend fun saveWeightMeasurement(
        value: Float,
        timestamp: Long = System.currentTimeMillis(),
        isHistorical: Boolean = false
    ): Long? {
        val activeCylinder = getActiveCylinderSync()
        return if (activeCylinder != null) {
            val weight = Weight(
                value = value,
                timestamp = timestamp,
                cylinderId = activeCylinder.id,
                isHistorical = isHistorical
            )
            insertWeightMeasurement(weight)
        } else {
            // Si no hay bombona activa, guardar sin asociación
            val weight = Weight(
                value = value,
                timestamp = timestamp,
                isHistorical = isHistorical
            )
            insertWeightMeasurement(weight)
        }
    }
    
    /**
     * Guarda múltiples mediciones históricas asociándolas con la bombona activa
     */
    suspend fun saveHistoricalMeasurements(measurements: List<Pair<Float, Long>>) {
        val activeCylinder = getActiveCylinderSync()
        val weights = measurements.map { (value, timestamp) ->
            Weight(
                value = value,
                timestamp = timestamp,
                cylinderId = activeCylinder?.id,
                isHistorical = true
            )
        }
        insertHistoricalMeasurements(weights)
    }
}
