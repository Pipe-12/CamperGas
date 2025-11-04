package com.example.campergas.data.repository

import com.example.campergas.domain.model.Consumption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar los datos de consumo de combustible.
 *
 * Este repositorio actúa como una capa de abstracción sobre FuelMeasurementRepository,
 * transformando las mediciones de combustible (FuelMeasurement) al modelo de dominio
 * más simple de Consumption, que se enfoca específicamente en datos de consumo
 * para visualización y análisis.
 *
 * Responsabilidades:
 * - Proveer acceso a registros de consumo en diferentes formatos (todos, por cilindro, por fechas)
 * - Convertir entre FuelMeasurement y Consumption
 * - Filtrar y ordenar datos de consumo
 *
 * La conversión de FuelMeasurement a Consumption simplifica los datos,
 * extrayendo solo la información relevante para análisis de consumo.
 *
 * @property fuelMeasurementRepository Repositorio fuente de mediciones de combustible
 * @author Felipe García Gómez
 */
@Singleton
class ConsumptionRepository @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository
) {

    /**
     * Obtiene todos los registros de consumo ordenados por fecha descendente.
     *
     * Recupera todas las mediciones de combustible y las convierte al modelo
     * Consumption para análisis y visualización de consumo histórico.
     *
     * @return Flow que emite la lista completa de registros de consumo
     */
    fun getAllConsumptions(): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getAllMeasurements().map { measurements ->
            measurements.map { Consumption.fromFuelMeasurement(it) }
        }
    }

    /**
     * Obtiene los registros de consumo en un rango de fechas.
     *
     * Filtra las mediciones para mostrar solo aquellas dentro del período
     * especificado, útil para análisis de consumo por períodos.
     *
     * @param startDate Timestamp Unix del inicio del período
     * @param endDate Timestamp Unix del fin del período
     * @return Flow que emite la lista de registros en el rango de fechas
     */
    fun getConsumptionsByDateRange(startDate: Long, endDate: Long): Flow<List<Consumption>> {
        return fuelMeasurementRepository.getMeasurementsByTimeRange(startDate, endDate)
            .map { measurements ->
                measurements.map { Consumption.fromFuelMeasurement(it) }
            }
    }

}
