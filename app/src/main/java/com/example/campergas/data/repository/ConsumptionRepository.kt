package com.example.campergas.data.repository

import com.example.campergas.domain.model.Consumption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar los datos de consumo de combustible
 * Utiliza FuelMeasurementRepository como fuente de datos y convierte a modelo Consumption
 */
@Singleton
class ConsumptionRepository @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository
) {

    /**
     * Obtiene todos los registros de consumo ordenados por fecha descendente
     */
    fun getAllConsumptions(): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getAllMeasurements().map { measurements ->
            measurements.map { Consumption.fromFuelMeasurement(it) }
        }
    }

    /**
     * Obtiene los registros de consumo de una bombona específica
     */
    fun getConsumptionsByCylinder(cylinderId: Long): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getMeasurementsByCylinder(cylinderId).map { measurements ->
            measurements.map { Consumption.fromFuelMeasurement(it) }
        }
    }

    /**
     * Obtiene los registros de consumo en un rango de fechas
     */
    fun getConsumptionsByDateRange(startDate: Long, endDate: Long): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getMeasurementsByTimeRange(startDate, endDate)
            .map { measurements ->
                measurements.map { Consumption.fromFuelMeasurement(it) }
            }
    }

    /**
     * Obtiene los registros de consumo de una bombona en un rango de fechas
     */
    fun getConsumptionsByCylinderAndDateRange(
        cylinderId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getMeasurementsByCylinderAndTimeRange(
            cylinderId, startDate, endDate
        ).map { measurements ->
            measurements.map { Consumption.fromFuelMeasurement(it) }
        }
    }

}
