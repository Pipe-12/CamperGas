package com.example.campergas.data.repository

import com.example.campergas.domain.model.Consumption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio for gestionar los data de consumption de combustible
 * Utiliza FuelMeasurementRepository como fuente of data y convierte a modelo Consumption
 */
@Singleton
class ConsumptionRepository @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository
) {

    /**
     * Gets todos los registros de consumption ordenados por date descendente
     */
    fun getAllConsumptions(): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getAllMeasurements().map { measurements ->
            measurements.map { Consumption.fromFuelMeasurement(it) }
        }
    }

    /**
     * Gets los registros de consumption de una cylinder específica
     */
    fun getConsumptionsByCylinder(cylinderId: Long): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getMeasurementsByCylinder(cylinderId).map { measurements ->
            measurements.map { Consumption.fromFuelMeasurement(it) }
        }
    }

    /**
     * Gets los registros de consumption en un rango de dates
     */
    fun getConsumptionsByDateRange(startDate: Long, endDate: Long): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getMeasurementsByTimeRange(startDate, endDate)
            .map { measurements ->
                measurements.map { Consumption.fromFuelMeasurement(it) }
            }
    }

    /**
     * Gets los registros de consumption de una cylinder en un rango de dates
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
