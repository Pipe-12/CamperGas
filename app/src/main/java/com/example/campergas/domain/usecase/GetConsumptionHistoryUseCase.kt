package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.ConsumptionRepository
import com.example.campergas.domain.model.Consumption
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

/**
 * Caso de uso para obtener y analizar el historial de consumo de gas.
 *
 * Este caso de uso encapsula la lógica de negocio para recuperar, filtrar y analizar
 * las mediciones históricas de consumo de gas. Proporciona múltiples formas de consultar
 * el historial:
 * - Por rangos de fechas personalizados
 * - Por cilindro específico
 * - Por períodos predefinidos (último día, semana, mes)
 *
 * Además, proporciona funciones de análisis para:
 * - Calcular el consumo total en un período
 * - Preparar datos para gráficos agrupados por día
 * - Detectar patrones de consumo
 *
 * El consumo se calcula como la diferencia entre la medición más antigua y la más
 * reciente del período, considerando recargas de cilindros que podrían generar
 * valores negativos (que se normalizan a cero).
 *
 * @property consumptionRepository Repositorio que proporciona acceso a datos de consumo
 * @author Felipe García Gómez
 */
class GetConsumptionHistoryUseCase @Inject constructor(
    private val consumptionRepository: ConsumptionRepository
) {
    /**
     * Obtiene el historial de consumo completo o filtrado por rango de fechas.
     *
     * Si se proporcionan fechas de inicio y fin, filtra las mediciones en ese rango.
     * Si no se proporcionan, devuelve todo el historial disponible.
     *
     * @param startDate Timestamp Unix del inicio del período (opcional)
     * @param endDate Timestamp Unix del fin del período (opcional)
     * @return Flow que emite la lista de registros de consumo en el rango especificado
     */
    operator fun invoke(startDate: Long? = null, endDate: Long? = null): Flow<List<Consumption>> {
        return if (startDate != null && endDate != null) {
            consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
        } else {
            consumptionRepository.getAllConsumptions()
        }
    }

    /**
     * Obtiene el consumo de la última semana (7 días).
     *
     * Calcula automáticamente las fechas de inicio y fin para el período
     * de los últimos 7 días desde el momento actual.
     *
     * @return Flow que emite las mediciones de la última semana
     */
    fun getLastWeekConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    /**
     * Obtiene el consumo del último mes (30 días).
     *
     * Calcula automáticamente las fechas de inicio y fin para el período
     * del último mes desde el momento actual.
     *
     * @return Flow que emite las mediciones del último mes
     */
    fun getLastMonthConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    /**
     * Obtiene el consumo del último día (24 horas).
     *
     * Calcula automáticamente las fechas de inicio y fin para el período
     * de las últimas 24 horas desde el momento actual.
     *
     * @return Flow que emite las mediciones del último día
     */
    fun getLastDayConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    /**
     * Calcula el total de gas consumido en una lista de mediciones.
     *
     * Para cada cilindro en la lista:
     * 1. Agrupa las mediciones por cilindro
     * 2. Ordena por fecha (más reciente primero)
     * 3. Calcula la diferencia entre la primera (más antigua) y última (más reciente)
     * 4. Suma los consumos de todos los cilindros
     *
     * El consumo se calcula como: medición_inicial - medición_final
     * Los valores negativos (recargas) se normalizan a cero.
     *
     * @param consumptions Lista de mediciones a analizar
     * @return Total de kilogramos de gas consumidos en el período
     */
    fun calculateTotalConsumption(consumptions: List<Consumption>): Float {
        if (consumptions.isEmpty()) return 0f

        // Agrupar por cilindro y calcular el consumption for cada uno
        return consumptions.groupBy { it.cylinderId }
            .map { (_, cylinderConsumptions) ->
                val sortedConsumptions = cylinderConsumptions.sortedByDescending { it.date }
                if (sortedConsumptions.size < 2) return@map 0f

                // Calculate difference between first and last measurement of period
                val firstMeasurement = sortedConsumptions.first()  // Most recent
                val lastMeasurement = sortedConsumptions.last()    // Oldest

                // El consumption es la diferencia: measurement inicial - measurement final
                val calculatedConsumption =
                    lastMeasurement.fuelKilograms - firstMeasurement.fuelKilograms

                // Evitar valores negativos (puede ocurrir durante recargas de cylinders)
                // En caso de recarga, el consumption se considera 0 for ese period
                kotlin.math.max(0f, calculatedConsumption)
            }
            .sum()
    }

    /**
     * Prepara datos para gráficos agrupando consumos por día.
     *
     * Agrupa todas las mediciones por día (eliminando horas, minutos, segundos)
     * y calcula el consumo total de cada día. Los datos resultantes están
     * ordenados cronológicamente y listos para visualización en gráficos.
     *
     * Cada punto de datos contiene:
     * - Fecha del día (timestamp normalizado a medianoche)
     * - Kilogramos totales consumidos ese día
     *
     * @param consumptions Lista de mediciones a agrupar y analizar
     * @return Lista de puntos de datos ordenados cronológicamente para gráficos
     */
    fun prepareChartData(consumptions: List<Consumption>): List<ChartDataPoint> {
        if (consumptions.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()

        // Group by day
        return consumptions.groupBy { consumption ->
            calendar.timeInMillis = consumption.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.map { (day, dayConsumptions) ->
            val totalConsumed = calculateTotalConsumption(dayConsumptions)
            ChartDataPoint(day, totalConsumed)
        }.sortedBy { it.date }
    }
}

/**
 * Representa un punto de datos para visualización en gráficos.
 *
 * Contiene el timestamp del día (normalizado a medianoche) y la cantidad
 * total de gas consumido durante ese día.
 *
 * @property date Timestamp Unix del día (normalizado a 00:00:00)
 * @property kilograms Kilogramos totales de gas consumidos en ese día
 */
data class ChartDataPoint(
    val date: Long,
    val kilograms: Float
)
