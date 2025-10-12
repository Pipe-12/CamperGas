package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.FuelMeasurement
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SaveFuelMeasurementUseCase @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository,
    private val gasCylinderRepository: GasCylinderRepository
) {

    companion object {
        private const val MIN_TIME_BETWEEN_SAVES_MS = 2 * 60 * 1000L // 2 minutos en milisegundos
        private const val OUTLIER_THRESHOLD_PERCENTAGE = 30.0f // 30% difference considered outlier
        private const val MIN_MEASUREMENTS_FOR_OUTLIER_DETECTION = 3 // Need at least 3 measurements for pattern detection
    }

    // Variable to track the last time it was saved a real-time measurement
    @Volatile
    private var lastSaveTimestamp: Long = 0L

    /**
     * Saves a measurement de combustible en TIEMPO REAL
     * This data comes from the characteristic WEIGHT_CHARACTERISTIC_UUID
     * y se marcan como isHistorical = false
     *
     * IMPORTANTE: Solo se guardan mediciones cada 2 minutos for evitar spam en la database
     *
     * @form totalWeight Peso total medido by the sensor
     * @form timestamp Timestamp of when the measurement was taken (default now)
     * @return Result con SaveMeasurementResult o information about why it was not saved
     */
    suspend fun saveRealTimeMeasurement(
        totalWeight: Float,
        timestamp: Long = System.currentTimeMillis()
    ): Result<SaveMeasurementResult> {
        return try {
            // Verify if it has passed at least 2 minutes since the last saved measurement
            val currentTime = System.currentTimeMillis()
            val timeSinceLastSave = currentTime - lastSaveTimestamp

            if (lastSaveTimestamp > 0 && timeSinceLastSave < MIN_TIME_BETWEEN_SAVES_MS) {
                val remainingTimeMs = MIN_TIME_BETWEEN_SAVES_MS - timeSinceLastSave
                val remainingTimeMinutes = (remainingTimeMs / 1000 / 60).toInt()
                val remainingTimeSeconds = ((remainingTimeMs / 1000) % 60).toInt()

                return Result.success(
                    SaveMeasurementResult(
                        measurementId = -1L,
                        processed = false,
                        reason = "Measurement skipped: remaining ${remainingTimeMinutes}m ${remainingTimeSeconds}s"
                    )
                )
            }

            // Get la cylinder activa
            val activeCylinder = gasCylinderRepository.getActiveCylinder().first()
                ?: return Result.failure(Exception("No active cylinder configured"))

            // Calculatesr el combustible disponible
            val fuelKilograms = maxOf(0f, totalWeight - activeCylinder.tare)
            val fuelPercentage = if (activeCylinder.capacity > 0) {
                (fuelKilograms / activeCylinder.capacity * 100).coerceIn(0f, 100f)
            } else {
                0f
            }

            // Create the measurement
            val measurement = FuelMeasurement(
                cylinderId = activeCylinder.id,
                cylinderName = activeCylinder.name,
                timestamp = timestamp,
                fuelKilograms = fuelKilograms,
                fuelPercentage = fuelPercentage,
                totalWeight = totalWeight,
                isCalibrated = true,
                isHistorical = false
            )

            // Validate the measurement
            if (!measurement.isValid()) {
                return Result.failure(Exception("Measurement data is not valid"))
            }

            // Save the measurement
            val id = fuelMeasurementRepository.insertMeasurement(measurement)

            // Detect and remove erroneous measurements (outliers)
            detectAndRemoveOutliers(activeCylinder.id)

            // Update the timestamp of the last saved measurement
            lastSaveTimestamp = currentTime

            Result.success(
                SaveMeasurementResult(
                    measurementId = id,
                    processed = true,
                    reason = "Measurement saved successfully"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves a measurement HISTORICAL/OFFLINE (data from offline sensor)
     * This data comes from the characteristic OFFLINE_CHARACTERISTIC_UUID
     * y se marcan como isHistorical = true
     *
     * @form cylinderId Specific cylinder ID
     * @form totalWeight Peso total medido by the sensor
     * @form timestamp Timestamp historical of when the measurement was taken
     * @form isCalibrated If the measurement is calibrated
     */
    /**
     * Saves multiple measurements HISTORICAL de combustible
     * This data comes from characteristics HISTORY y se marcan como isHistorical = true
     *
     * @form cylinderId ID of the cylinder a la que pertenecen las mediciones
     * @form weightMeasurements Lista de pares (total weight, timestamp)
     */
    suspend fun saveHistoricalMeasurements(
        cylinderId: Long,
        weightMeasurements: List<Pair<Float, Long>> // Peso total y timestamp
    ): Result<Int> {
        return try {
            // Get cylinder information
            val cylinder = gasCylinderRepository.getCylinderById(cylinderId)
                ?: return Result.failure(Exception("Bombona no encontrada"))

            // Crear las mediciones
            val measurements = weightMeasurements.map { (totalWeight, timestamp) ->
                val fuelKilograms = maxOf(0f, totalWeight - cylinder.tare)
                val fuelPercentage = if (cylinder.capacity > 0) {
                    (fuelKilograms / cylinder.capacity * 100).coerceIn(0f, 100f)
                } else {
                    0f
                }

                FuelMeasurement(
                    cylinderId = cylinder.id,
                    cylinderName = cylinder.name,
                    timestamp = timestamp,
                    fuelKilograms = fuelKilograms,
                    fuelPercentage = fuelPercentage,
                    totalWeight = totalWeight,
                    isCalibrated = true,
                    isHistorical = true
                )
            }.filter { it.isValid() } // Filter only valid measurements

            // Guardar todas las mediciones
            fuelMeasurementRepository.insertMeasurements(measurements)

            Result.success(measurements.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Detects and removes erroneous measurements (outliers) based on weight patterns.
     * 
     * When a measurement deviates substantially from the trend and then returns to values
     * normales, la measurement desviada se considera un error y se elimina.
     * 
     * Detection pattern:
     * - Anterior: peso normal
     * - Outlier: peso sustancialmente diferente (>30% cambio)
     * - Actual: peso similar al anterior (vuelve a la normalidad)
     *
     * @form cylinderId ID of the cylinder for analizar
     */
    private suspend fun detectAndRemoveOutliers(cylinderId: Long) {
        try {
            // Get last 3 measurements for pattern analysis
            val recentMeasurements = fuelMeasurementRepository.getLastNMeasurements(cylinderId, MIN_MEASUREMENTS_FOR_OUTLIER_DETECTION)
            
            // We need at least 3 measurements to detect the pattern: previous -> outlier -> current
            if (recentMeasurements.size < MIN_MEASUREMENTS_FOR_OUTLIER_DETECTION) {
                return
            }

            // Measurements come sorted by timestamp DESC, so:
            // current = recentMeasurements[0] (most recent)
            // outlier = recentMeasurements[1] (medio)
            // previous = recentMeasurements[2] (anterior)
            val current = recentMeasurements[0]
            val outlier = recentMeasurements[1]
            val previous = recentMeasurements[2]

            // Verify if middle measurement is an outlier
            if (isOutlierMeasurement(previous, outlier, current)) {
                // Remove outlier measurement
                fuelMeasurementRepository.deleteMeasurementById(outlier.id)
            }
        } catch (e: Exception) {
            // If any error occurs in outlier detection, do not affect main flow
            // Solo registrar silenciosamente el error
        }
    }

    /**
     * Determines if a measurement is an outlier based on the pattern:
     * previous -> outlier -> current
     * 
     * Una measurement se considera outlier si:
     * 1. Deviates substantially (>30%) from previous value
     * 2. El valor actual regresa cerca del valor anterior (diferencia <30%)
     * 3. The outlier deviation is greater than the deviation between previous and current
     *
     * @form previous Previous measurement
     * @form outlier Candidate outlier measurement  
     * @form current Current measurement
     * @return true si la measurement del medio es un outlier
     */
    private fun isOutlierMeasurement(
        previous: FuelMeasurement,
        outlier: FuelMeasurement,
        current: FuelMeasurement
    ): Boolean {
        // Use total weight for analysis as it is most direct value from sensor
        val prevWeight = previous.totalWeight
        val outlierWeight = outlier.totalWeight
        val currentWeight = current.totalWeight

        // Avoid division by zero
        if (prevWeight <= 0f || outlierWeight <= 0f || currentWeight <= 0f) {
            return false
        }

        // Calculatesr porcentajes de cambio
        val outlierVsPrevious = kotlin.math.abs(outlierWeight - prevWeight) / prevWeight * 100f
        val currentVsPrevious = kotlin.math.abs(currentWeight - prevWeight) / prevWeight * 100f
        val currentVsOutlier = kotlin.math.abs(currentWeight - outlierWeight) / outlierWeight * 100f

        // Condiciones for considerar outlier:
        // 1. The outlier deviates substantially from previous (>30%)
        val isSignificantDeviation = outlierVsPrevious > OUTLIER_THRESHOLD_PERCENTAGE
        
        // 2. Current value is closer to previous than to outlier
        val returnsToNormal = currentVsPrevious < outlierVsPrevious
        
        // 3. Change from outlier to current is also significant (confirms outlier was anomalous)
        val significantCorrectionFromOutlier = currentVsOutlier > OUTLIER_THRESHOLD_PERCENTAGE

        return isSignificantDeviation && returnsToNormal && significantCorrectionFromOutlier
    }

    data class SaveMeasurementResult(
        val measurementId: Long,
        val processed: Boolean,
        val reason: String = ""
    )
}
