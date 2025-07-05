package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.data.repository.GasCylinderRepository
import javax.inject.Inject

/**
 * Caso de uso para limpiar y mantener las mediciones de combustible
 */
class FuelMeasurementMaintenanceUseCase @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository,
    private val gasCylinderRepository: GasCylinderRepository
) {
    
    /**
     * Elimina mediciones antiguas para mantener el rendimiento de la base de datos
     * @param daysToKeep Número de días de datos a mantener (por defecto 30 días)
     */
    suspend fun cleanOldMeasurements(daysToKeep: Int = 30): Result<Int> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
            
            // Contar mediciones antes de borrar
            val totalBefore = fuelMeasurementRepository.getAllMeasurements()
            
            // Eliminar mediciones antiguas
            fuelMeasurementRepository.deleteOldMeasurements(cutoffTime)
            
            Result.success(daysToKeep)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Elimina todas las mediciones de una bombona específica
     */
    suspend fun deleteMeasurementsByCylinder(cylinderId: Long): Result<Unit> {
        return try {
            fuelMeasurementRepository.deleteMeasurementsByCylinder(cylinderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Elimina todas las mediciones
     */
    suspend fun deleteAllMeasurements(): Result<Unit> {
        return try {
            fuelMeasurementRepository.deleteAllMeasurements()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica la integridad de los datos y reporta estadísticas
     */
    suspend fun getDatabaseStats(): DatabaseStats {
        return try {
            val cylinders = gasCylinderRepository.getAllCylindersSync()
            val stats = mutableListOf<CylinderStats>()
            
            cylinders.forEach { cylinder ->
                val count = fuelMeasurementRepository.getMeasurementCountByCylinder(cylinder.id)
                val lastTwo = fuelMeasurementRepository.getLastTwoMeasurements(cylinder.id)
                
                stats.add(
                    CylinderStats(
                        cylinderId = cylinder.id,
                        cylinderName = cylinder.name,
                        measurementCount = count,
                        latestMeasurement = lastTwo.firstOrNull(),
                        hasValidData = lastTwo.isNotEmpty()
                    )
                )
            }
            
            DatabaseStats(
                cylinderStats = stats,
                totalCylinders = cylinders.size,
                cylindersWithData = stats.count { it.hasValidData }
            )
        } catch (e: Exception) {
            DatabaseStats(emptyList(), 0, 0)
        }
    }
    
    data class DatabaseStats(
        val cylinderStats: List<CylinderStats>,
        val totalCylinders: Int,
        val cylindersWithData: Int
    )
    
    data class CylinderStats(
        val cylinderId: Long,
        val cylinderName: String,
        val measurementCount: Int,
        val latestMeasurement: com.example.campergas.domain.model.FuelMeasurement?,
        val hasValidData: Boolean
    )
}
