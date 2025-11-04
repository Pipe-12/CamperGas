package com.example.campergas.data.repository

import com.example.campergas.data.local.db.FuelMeasurementDao
import com.example.campergas.data.local.db.FuelMeasurementEntity
import com.example.campergas.domain.model.FuelMeasurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar mediciones de combustible en la base de datos.
 *
 * Este repositorio actúa como una capa de abstracción entre la capa de dominio
 * y la capa de persistencia (Room Database). Proporciona operaciones CRUD
 * para mediciones de combustible y maneja la conversión entre entidades de
 * base de datos (FuelMeasurementEntity) y modelos de dominio (FuelMeasurement).
 *
 * Responsabilidades principales:
 * - Proveer acceso a mediciones de combustible con diferentes filtros
 * - Insertar nuevas mediciones (individuales o en lote)
 * - Eliminar mediciones antiguas o por criterios específicos
 * - Convertir entre modelos de dominio y entidades de base de datos
 * - Exponer datos como Flows reactivos para actualización automática de UI
 *
 * Tipos de mediciones soportadas:
 * - Tiempo real (isHistorical = false): Mediciones actuales del sensor
 * - Históricas/Offline (isHistorical = true): Datos sincronizados del sensor
 *
 * @property fuelMeasurementDao DAO de Room para acceso a la base de datos
 * @author Felipe García Gómez
 */
@Singleton
class FuelMeasurementRepository @Inject constructor(
    private val fuelMeasurementDao: FuelMeasurementDao
) {
    /**
     * Obtiene todas las mediciones de combustible ordenadas por timestamp descendente.
     *
     * @return Flow que emite la lista completa de mediciones
     */
    fun getAllMeasurements(): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getAllMeasurements().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Obtiene las mediciones de un cilindro específico.
     *
     * @param cylinderId ID del cilindro a consultar
     * @return Flow que emite la lista de mediciones del cilindro
     */
    fun getMeasurementsByCylinder(cylinderId: Long): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getMeasurementsByCylinder(cylinderId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Obtiene la medición en tiempo real más reciente.
     *
     * Solo devuelve mediciones marcadas como isHistorical = false.
     *
     * @return Flow que emite la medición más reciente o null si no hay ninguna
     */
    fun getLatestRealTimeMeasurement(): Flow<FuelMeasurement?> {
        return fuelMeasurementDao.getLatestRealTimeMeasurement().map { entity ->
            entity?.toDomainModel()
        }
    }

    /**
     * Obtiene mediciones en un rango de tiempo específico.
     *
     * @param startTime Timestamp Unix del inicio del período
     * @param endTime Timestamp Unix del fin del período
     * @return Flow que emite la lista de mediciones en el rango
     */
    fun getMeasurementsByTimeRange(startTime: Long, endTime: Long): Flow<List<FuelMeasurement>> {
        return fuelMeasurementDao.getMeasurementsByTimeRange(startTime, endTime).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Obtiene mediciones de un cilindro en un rango de tiempo.
     *
     * Combina filtros de cilindro y tiempo para consultas específicas.
     *
     * @param cylinderId ID del cilindro a consultar
     * @param startTime Timestamp Unix del inicio del período
     * @param endTime Timestamp Unix del fin del período
     * @return Flow que emite la lista de mediciones filtradas
     */
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

    /**
     * Inserta una nueva medición de combustible en la base de datos.
     *
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @param measurement Medición a insertar
     * @return ID asignado a la medición insertada
     */
    suspend fun insertMeasurement(measurement: FuelMeasurement): Long {
        return fuelMeasurementDao.insertMeasurement(measurement.toEntity())
    }

    /**
     * Inserta múltiples mediciones en la base de datos en lote.
     *
     * Optimizado para insertar grandes cantidades de datos históricos.
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @param measurements Lista de mediciones a insertar
     */
    suspend fun insertMeasurements(measurements: List<FuelMeasurement>) {
        fuelMeasurementDao.insertMeasurements(measurements.map { it.toEntity() })
    }


    /**
     * Obtiene las N últimas mediciones de un cilindro.
     *
     * Útil para análisis de patrones y detección de outliers.
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @param cylinderId ID del cilindro a consultar
     * @param limit Número máximo de mediciones a obtener
     * @return Lista con las N mediciones más recientes ordenadas por timestamp descendente
     */
    suspend fun getLastNMeasurements(cylinderId: Long, limit: Int): List<FuelMeasurement> {
        return fuelMeasurementDao.getLastNMeasurements(cylinderId, limit).map { it.toDomainModel() }
    }

    /**
     * Elimina una medición específica por su ID.
     *
     * Útil para eliminar outliers detectados.
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @param id ID de la medición a eliminar
     */
    suspend fun deleteMeasurementById(id: Long) {
        fuelMeasurementDao.deleteMeasurementById(id)
    }

    /**
     * Convierte una entidad de base de datos a modelo de dominio.
     *
     * @return Objeto FuelMeasurement del modelo de dominio
     */
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

    /**
     * Convierte un modelo de dominio a entidad de base de datos.
     *
     * @return Objeto FuelMeasurementEntity para persistir en Room
     */
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
