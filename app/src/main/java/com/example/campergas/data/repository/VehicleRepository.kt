package com.example.campergas.data.repository

import com.example.campergas.data.local.vehicle.VehicleConfigEntity
import com.example.campergas.data.local.vehicle.VehicleDao
import com.example.campergas.domain.model.VehicleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar la configuración del vehículo recreativo.
 *
 * Este repositorio proporciona acceso a la configuración geométrica del vehículo
 * (caravana o autocaravana), manejando la conversión entre entidades de base de
 * datos y modelos de dominio.
 *
 * Características:
 * - Solo existe una configuración de vehículo por aplicación
 * - Al guardar, actualiza la existente o crea una nueva si no existe
 * - Almacena dimensiones críticas para cálculos de nivelación y estabilidad
 *
 * La configuración incluye:
 * - Tipo de vehículo (caravana/autocaravana)
 * - Distancia entre ruedas traseras
 * - Distancia al punto de apoyo delantero (caravanas)
 * - Distancia entre ruedas delanteras (autocaravanas)
 * - Capacidad del sistema de gas
 *
 * Estos datos se utilizan para:
 * - Calcular distribución de peso en los ejes
 * - Determinar elevación necesaria en ruedas para nivelar
 * - Visualizar geometría del vehículo en la UI
 * - Generar recomendaciones de nivelación
 *
 * @property vehicleDao DAO de Room para acceso a la base de datos
 * @author Felipe García Gómez
 */
@Singleton
class VehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao
) {
    /**
     * Obtiene la configuración del vehículo.
     *
     * @return Flow que emite la configuración actual o null si no está configurado
     */
    fun getVehicleConfig(): Flow<VehicleConfig?> {
        return vehicleDao.getVehicleConfig().map { entity ->
            entity?.toDomainModel()
        }
    }

    /**
     * Guarda o actualiza la configuración del vehículo.
     *
     * Si ya existe una configuración, la actualiza. Si no existe, crea una nueva.
     * Solo puede haber una configuración de vehículo en el sistema.
     *
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @param config Configuración del vehículo a guardar
     */
    suspend fun saveVehicleConfig(config: VehicleConfig) {
        val entity = config.toEntity()
        val exists = vehicleDao.configExists()

        if (exists) {
            vehicleDao.updateVehicleConfig(entity)
        } else {
            vehicleDao.insertVehicleConfig(entity)
        }
    }

    /**
     * Convierte una entidad de base de datos a modelo de dominio.
     *
     * @return Objeto VehicleConfig del modelo de dominio
     */
    private fun VehicleConfigEntity.toDomainModel(): VehicleConfig {
        return VehicleConfig(
            type = this.type,
            distanceBetweenRearWheels = this.distanceBetweenRearWheels,
            distanceToFrontSupport = this.distanceToFrontSupport,
            distanceBetweenFrontWheels = this.distanceBetweenFrontWheels
        )
    }

    /**
     * Convierte un modelo de dominio a entidad de base de datos.
     *
     * @return Objeto VehicleConfigEntity para persistir en Room
     */
    private fun VehicleConfig.toEntity(): VehicleConfigEntity {
        return VehicleConfigEntity(
            type = this.type,
            distanceBetweenRearWheels = this.distanceBetweenRearWheels,
            distanceToFrontSupport = this.distanceToFrontSupport,
            distanceBetweenFrontWheels = this.distanceBetweenFrontWheels
        )
    }
}
