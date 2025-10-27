package com.example.campergas.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de Acceso a Datos (DAO) para operaciones de mediciones de combustible.
 *
 * Proporciona métodos para insertar, actualizar, eliminar y consultar mediciones de combustible
 * desde la base de datos local. Soporta seguimiento de datos en tiempo real e históricos,
 * consultas por rangos de tiempo y mediciones específicas por cilindro.
 */
@Dao
interface FuelMeasurementDao {

    /**
     * Recupera todas las mediciones de combustible ordenadas por timestamp descendente.
     *
     * @return Flow que emite una lista de todas las mediciones, las más recientes primero
     */
    @Query("SELECT * FROM fuel_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<FuelMeasurementEntity>>

    /**
     * Recupera mediciones para un cilindro de gas específico.
     *
     * @param cylinderId ID del cilindro de gas
     * @return Flow que emite mediciones para el cilindro especificado, las más recientes primero
     */
    @Query("SELECT * FROM fuel_measurements WHERE cylinderId = :cylinderId ORDER BY timestamp DESC")
    fun getMeasurementsByCylinder(cylinderId: Long): Flow<List<FuelMeasurementEntity>>

    /**
     * Recupera la medición en tiempo real (no histórica) más reciente.
     *
     * @return Flow que emite la última medición en tiempo real, o null si no existe ninguna
     */
    @Query("SELECT * FROM fuel_measurements WHERE isHistorical = 0 ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRealTimeMeasurement(): Flow<FuelMeasurementEntity?>

    /**
     * Recupera todas las mediciones históricas para un cilindro específico.
     *
     * @param cylinderId ID del cilindro de gas
     * @return Flow que emite mediciones históricas para el cilindro, las más recientes primero
     */
    @Query("SELECT * FROM fuel_measurements WHERE isHistorical = 1 AND cylinderId = :cylinderId ORDER BY timestamp DESC")
    fun getHistoricalMeasurements(cylinderId: Long): Flow<List<FuelMeasurementEntity>>

    /**
     * Recupera mediciones dentro de un rango de tiempo específico.
     *
     * @param startTime Inicio del rango de tiempo (milisegundos desde epoch)
     * @param endTime Fin del rango de tiempo (milisegundos desde epoch)
     * @return Flow que emite mediciones dentro del rango de tiempo, las más recientes primero
     */
    @Query("SELECT * FROM fuel_measurements WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMeasurementsByTimeRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<FuelMeasurementEntity>>

    /**
     * Recupera mediciones para un cilindro específico dentro de un rango de tiempo.
     *
     * @param cylinderId ID del cilindro de gas
     * @param startTime Inicio del rango de tiempo (milisegundos desde epoch)
     * @param endTime Fin del rango de tiempo (milisegundos desde epoch)
     * @return Flow que emite mediciones coincidentes, las más recientes primero
     */
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

    /**
     * Inserta una nueva medición, reemplazando si ocurre un conflicto.
     *
     * @param measurement La medición a insertar
     * @return ID de fila de la medición insertada
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: FuelMeasurementEntity): Long

    /**
     * Inserta múltiples mediciones, reemplazando en caso de conflictos.
     *
     * @param measurements Lista de mediciones a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<FuelMeasurementEntity>)

    /**
     * Actualiza una medición existente.
     *
     * @param measurement La medición a actualizar
     */
    @Update
    suspend fun updateMeasurement(measurement: FuelMeasurementEntity)

    /**
     * Elimina una medición específica.
     *
     * @param measurement La medición a eliminar
     */
    @Delete
    suspend fun deleteMeasurement(measurement: FuelMeasurementEntity)

    /**
     * Elimina una medición por su ID.
     *
     * @param id ID de la medición a eliminar
     */
    @Query("DELETE FROM fuel_measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)

    /**
     * Elimina todas las mediciones para un cilindro específico.
     *
     * @param cylinderId ID del cilindro de gas
     */
    @Query("DELETE FROM fuel_measurements WHERE cylinderId = :cylinderId")
    suspend fun deleteMeasurementsByCylinder(cylinderId: Long)

    /**
     * Elimina mediciones anteriores a un timestamp especificado.
     *
     * Útil para mantener el tamaño de la base de datos y eliminar datos obsoletos.
     *
     * @param beforeTimestamp Umbral de timestamp (milisegundos desde epoch)
     */
    @Query("DELETE FROM fuel_measurements WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldMeasurements(beforeTimestamp: Long)

    /**
     * Elimina todas las mediciones de la base de datos.
     */
    @Query("DELETE FROM fuel_measurements")
    suspend fun deleteAllMeasurements()

    /**
     * Recupera las mediciones más recientes hasta un límite especificado.
     *
     * @param limit Número máximo de mediciones a recuperar
     * @return Flow que emite las mediciones más recientes
     */
    @Query("SELECT * FROM fuel_measurements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMeasurements(limit: Int): Flow<List<FuelMeasurementEntity>>

    /**
     * Recupera las dos últimas mediciones para un cilindro específico.
     *
     * Se usa para calcular la tasa de consumo y tendencias.
     *
     * @param cylinderId ID del cilindro de gas
     * @return Lista de las dos mediciones más recientes para el cilindro
     */
    @Query(
        """
        SELECT * FROM fuel_measurements 
        WHERE cylinderId = :cylinderId 
        ORDER BY timestamp DESC 
        LIMIT 2
    """
    )
    suspend fun getLastTwoMeasurements(cylinderId: Long): List<FuelMeasurementEntity>

    /**
     * Recupera las últimas N mediciones para un cilindro específico.
     *
     * @param cylinderId ID del cilindro de gas
     * @param limit Número de mediciones a recuperar
     * @return Lista de las N mediciones más recientes para el cilindro
     */
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
