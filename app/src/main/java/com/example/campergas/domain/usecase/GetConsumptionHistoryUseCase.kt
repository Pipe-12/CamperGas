package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.ConsumptionRepository
import com.example.campergas.domain.model.Consumption
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for getting and analyzing gas consumption history.
 *
 * This use case encapsulates the business logic for retrieving, filtering, and analyzing
 * historical gas consumption measurements. Provides multiple ways to query history:
 * - By custom date ranges
 * - By specific cylinder
 * - By predefined periods (last day, week, month)
 *
 * Additionally, provides analysis functions for:
 * - Calculating total consumption in a period
 * - Preparing data for charts grouped by day
 * - Detecting consumption patterns
 *
 * Consumption is calculated as the difference between the oldest and most
 * recent measurement in the period, considering cylinder refills that could
 * generate negative values (which are normalized to zero).
 *
 * @property consumptionRepository Repository that provides access to consumption data
 * @author Felipe García Gómez
 */
class GetConsumptionHistoryUseCase @Inject constructor(
    private val consumptionRepository: ConsumptionRepository
) {
    /**
     * Gets complete consumption history or filtered by date range.
     *
     * If start and end dates are provided, filters measurements in that range.
     * If not provided, returns all available history.
     *
     * @param startDate Unix timestamp of period start (optional)
     * @param endDate Unix timestamp of period end (optional)
     * @return Flow that emits the list of consumption records in the specified range
     */
    operator fun invoke(startDate: Long? = null, endDate: Long? = null): Flow<List<Consumption>> {
        return if (startDate != null && endDate != null) {
            consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
        } else {
            consumptionRepository.getAllConsumptions()
        }
    }

    /**
     * Gets consumption for the last week (7 days).
     *
     * Automatically calculates start and end dates for the period
     * of the last 7 days from the current moment.
     *
     * @return Flow that emits measurements from the last week
     */
    fun getLastWeekConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    /**
     * Gets consumption for the last month (30 days).
     *
     * Automatically calculates start and end dates for the period
     * of the last month from the current moment.
     *
     * @return Flow that emits measurements from the last month
     */
    fun getLastMonthConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    /**
     * Gets consumption for the last day (24 hours).
     *
     * Automatically calculates start and end dates for the period
     * of the last 24 hours from the current moment.
     *
     * @return Flow that emits measurements from the last day
     */
    fun getLastDayConsumption(): Flow<List<Consumption>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startDate = calendar.timeInMillis

        return consumptionRepository.getConsumptionsByDateRange(startDate, endDate)
    }

    /**
     * Calculates total gas consumed in a list of measurements.
     *
     * For each cylinder in the list:
     * 1. Groups measurements by cylinder
     * 2. Sorts by date (most recent first)
     * 3. Calculates difference between first (oldest) and last (most recent)
     * 4. Sums consumption from all cylinders
     *
     * Consumption is calculated as: initial_measurement - final_measurement
     * Negative values (refills) are normalized to zero.
     *
     * @param consumptions List of measurements to analyze
     * @return Total kilograms of gas consumed in the period
     */
    fun calculateTotalConsumption(consumptions: List<Consumption>): Float {
        if (consumptions.isEmpty()) return 0f

        // Group by cylinder and calculate consumption for each
        return consumptions.groupBy { it.cylinderId }
            .map { (_, cylinderConsumptions) ->
                val sortedConsumptions = cylinderConsumptions.sortedByDescending { it.date }
                if (sortedConsumptions.size < 2) return@map 0f

                // Calculate difference between first and last measurement of period
                val firstMeasurement = sortedConsumptions.first()  // Most recent
                val lastMeasurement = sortedConsumptions.last()    // Oldest

                // Consumption is the difference: initial measurement - final measurement
                val calculatedConsumption =
                    lastMeasurement.fuelKilograms - firstMeasurement.fuelKilograms

                // Avoid negative values (can occur during cylinder refills)
                // In case of refill, consumption is considered 0 for that period
                kotlin.math.max(0f, calculatedConsumption)
            }
            .sum()
    }

    /**
     * Prepares data for charts by grouping consumption by day.
     *
     * Groups all measurements by day (removing hours, minutes, seconds)
     * and calculates total consumption for each day. Resulting data is
     * sorted chronologically and ready for chart visualization.
     *
     * Each data point contains:
     * - Day date (timestamp normalized to midnight)
     * - Total kilograms consumed that day
     *
     * @param consumptions List of measurements to group and analyze
     * @return List of data points sorted chronologically for charts
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
 * Represents a data point for chart visualization.
 *
 * Contains the day timestamp (normalized to midnight) and the total
 * amount of gas consumed during that day.
 *
 * @property date Unix timestamp of the day (normalized to 00:00:00)
 * @property kilograms Total kilograms of gas consumed on that day
 */
data class ChartDataPoint(
    val date: Long,
    val kilograms: Float
)
