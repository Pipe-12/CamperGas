package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.FuelMeasurement
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

/**
 * Use case to generate test fuel measurements for the active gas cylinder.
 *
 * Generates realistic test data with timestamps up to one month old to populate
 * the consumption screen with sample data. This is useful for testing and
 * demonstrating the application without actual sensor data.
 *
 * @property fuelMeasurementRepository Repository for fuel measurements
 * @property gasCylinderRepository Repository for gas cylinders
 */
class GenerateTestDataUseCase @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository,
    private val gasCylinderRepository: GasCylinderRepository
) {
    companion object {
        /** Number of test measurements to generate */
        private const val TEST_MEASUREMENTS_COUNT = 100

        /** Maximum age of test data in milliseconds (30 days) */
        private const val MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000

        /** Average consumption rate per day in kg */
        private const val AVG_CONSUMPTION_PER_DAY = 0.3f
    }

    /**
     * Generates test fuel measurements for the active cylinder.
     *
     * Creates measurements with:
     * - Timestamps distributed over the last 30 days
     * - Realistic decreasing fuel levels (simulating consumption)
     * - Some random variation to simulate real sensor data
     * - All marked as historical data
     *
     * @return Result with number of measurements generated or error
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            // Get the active cylinder
            val activeCylinder = gasCylinderRepository.getActiveCylinder().first()
                ?: return Result.failure(Exception("No active cylinder configured"))

            val currentTime = System.currentTimeMillis()
            val measurements = mutableListOf<FuelMeasurement>()

            // Starting fuel percentage (80-95%)
            var currentFuelPercentage = Random.nextFloat() * 15f + 80f

            // Generate measurements from oldest to newest
            for (i in 0 until TEST_MEASUREMENTS_COUNT) {
                // Calculate timestamp (distributed over last 30 days)
                val ageMs = MAX_AGE_MS - (i * MAX_AGE_MS / TEST_MEASUREMENTS_COUNT)
                val timestamp = currentTime - ageMs

                // Simulate gradual consumption with some randomness
                val consumptionRate = AVG_CONSUMPTION_PER_DAY / TEST_MEASUREMENTS_COUNT * 30
                currentFuelPercentage -= consumptionRate + Random.nextFloat() * 0.5f - 0.25f

                // Keep within valid range (5% to 100%)
                currentFuelPercentage = currentFuelPercentage.coerceIn(5f, 100f)

                // Calculate fuel kilograms from percentage
                val fuelKilograms = activeCylinder.capacity * currentFuelPercentage / 100f

                // Calculate total weight
                val totalWeight = activeCylinder.tare + fuelKilograms

                val measurement = FuelMeasurement(
                    cylinderId = activeCylinder.id,
                    cylinderName = activeCylinder.name,
                    timestamp = timestamp,
                    fuelKilograms = fuelKilograms,
                    fuelPercentage = currentFuelPercentage,
                    totalWeight = totalWeight,
                    isCalibrated = true,
                    isHistorical = true // Mark as historical test data
                )

                measurements.add(measurement)
            }

            // Save all measurements
            fuelMeasurementRepository.insertMeasurements(measurements)

            Result.success(measurements.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
