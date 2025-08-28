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

    // Variable para trackear la última vez que se guardó una medición en tiempo real
    @Volatile
    private var lastSaveTimestamp: Long = 0L

    /**
     * Guarda una medición de combustible en TIEMPO REAL
     * Estos datos provienen de la característica WEIGHT_CHARACTERISTIC_UUID
     * y se marcan como isHistorical = false
     *
     * IMPORTANTE: Solo se guardan mediciones cada 2 minutos para evitar spam en la base de datos
     *
     * @param totalWeight Peso total medido por el sensor
     * @param timestamp Timestamp de cuando se tomó la medición (por defecto ahora)
     * @return Result con SaveMeasurementResult o información de por qué no se guardó
     */
    suspend fun saveRealTimeMeasurement(
        totalWeight: Float,
        timestamp: Long = System.currentTimeMillis()
    ): Result<SaveMeasurementResult> {
        return try {
            // Verificar si han pasado al menos 2 minutos desde la última medición guardada
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
                        reason = "Medición omitida: faltan ${remainingTimeMinutes}m ${remainingTimeSeconds}s"
                    )
                )
            }

            // Obtener la bombona activa
            val activeCylinder = gasCylinderRepository.getActiveCylinder().first()
                ?: return Result.failure(Exception("No hay bombona activa configurada"))

            // Calcular el combustible disponible
            val fuelKilograms = maxOf(0f, totalWeight - activeCylinder.tare)
            val fuelPercentage = if (activeCylinder.capacity > 0) {
                (fuelKilograms / activeCylinder.capacity * 100).coerceIn(0f, 100f)
            } else {
                0f
            }

            // Crear la medición
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

            // Validar la medición
            if (!measurement.isValid()) {
                return Result.failure(Exception("Los datos de la medición no son válidos"))
            }

            // Guardar la medición
            val id = fuelMeasurementRepository.insertMeasurement(measurement)

            // Detectar y eliminar mediciones erróneas (outliers)
            detectAndRemoveOutliers(activeCylinder.id)

            // Actualizar el timestamp de la última medición guardada
            lastSaveTimestamp = currentTime

            Result.success(
                SaveMeasurementResult(
                    measurementId = id,
                    processed = true,
                    reason = "Medición guardada correctamente"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda una medición HISTÓRICA/OFFLINE (datos provenientes del sensor offline)
     * Estos datos provienen de la característica OFFLINE_CHARACTERISTIC_UUID
     * y se marcan como isHistorical = true
     *
     * @param cylinderId ID de la bombona específica
     * @param totalWeight Peso total medido por el sensor
     * @param timestamp Timestamp histórico de cuando se tomó la medición
     * @param isCalibrated Si la medición está calibrada
     */
    /**
     * Guarda múltiples mediciones HISTÓRICAS de combustible
     * Estos datos provienen de características HISTORY y se marcan como isHistorical = true
     *
     * @param cylinderId ID de la bombona a la que pertenecen las mediciones
     * @param weightMeasurements Lista de pares (peso total, timestamp)
     */
    suspend fun saveHistoricalMeasurements(
        cylinderId: Long,
        weightMeasurements: List<Pair<Float, Long>> // Peso total y timestamp
    ): Result<Int> {
        return try {
            // Obtener la información de la bombona
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
            }.filter { it.isValid() } // Filtrar solo mediciones válidas

            // Guardar todas las mediciones
            fuelMeasurementRepository.insertMeasurements(measurements)

            Result.success(measurements.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Detecta y elimina mediciones erróneas (outliers) basándose en patrones de peso.
     * 
     * Cuando una medición se desvía sustancialmente de la tendencia y luego vuelve a valores
     * normales, la medición desviada se considera un error y se elimina.
     * 
     * Patrón de detección:
     * - Anterior: peso normal
     * - Outlier: peso sustancialmente diferente (>30% cambio)
     * - Actual: peso similar al anterior (vuelve a la normalidad)
     *
     * @param cylinderId ID de la bombona para analizar
     */
    private suspend fun detectAndRemoveOutliers(cylinderId: Long) {
        try {
            // Obtener las últimas 3 mediciones para análisis de patrón
            val recentMeasurements = fuelMeasurementRepository.getLastNMeasurements(cylinderId, MIN_MEASUREMENTS_FOR_OUTLIER_DETECTION)
            
            // Necesitamos al menos 3 mediciones para detectar el patrón: anterior -> outlier -> actual
            if (recentMeasurements.size < MIN_MEASUREMENTS_FOR_OUTLIER_DETECTION) {
                return
            }

            // Las mediciones vienen ordenadas por timestamp DESC, así que:
            // current = recentMeasurements[0] (más reciente)
            // outlier = recentMeasurements[1] (medio)
            // previous = recentMeasurements[2] (anterior)
            val current = recentMeasurements[0]
            val outlier = recentMeasurements[1]
            val previous = recentMeasurements[2]

            // Verificar si la medición del medio es un outlier
            if (isOutlierMeasurement(previous, outlier, current)) {
                // Eliminar la medición outlier
                fuelMeasurementRepository.deleteMeasurementById(outlier.id)
            }
        } catch (e: Exception) {
            // Si ocurre algún error en la detección de outliers, no afectar el flujo principal
            // Solo registrar silenciosamente el error
        }
    }

    /**
     * Determina si una medición es un outlier basándose en el patrón:
     * previous -> outlier -> current
     * 
     * Una medición se considera outlier si:
     * 1. Se desvía sustancialmente (>30%) del valor anterior
     * 2. El valor actual regresa cerca del valor anterior (diferencia <30%)
     * 3. La desviación del outlier es mayor que la desviación entre anterior y actual
     *
     * @param previous Medición anterior
     * @param outlier Medición candidata a outlier  
     * @param current Medición actual
     * @return true si la medición del medio es un outlier
     */
    private fun isOutlierMeasurement(
        previous: FuelMeasurement,
        outlier: FuelMeasurement,
        current: FuelMeasurement
    ): Boolean {
        // Usar el peso total para el análisis ya que es el valor más directo del sensor
        val prevWeight = previous.totalWeight
        val outlierWeight = outlier.totalWeight
        val currentWeight = current.totalWeight

        // Evitar división por cero
        if (prevWeight <= 0f || outlierWeight <= 0f || currentWeight <= 0f) {
            return false
        }

        // Calcular porcentajes de cambio
        val outlierVsPrevious = kotlin.math.abs(outlierWeight - prevWeight) / prevWeight * 100f
        val currentVsPrevious = kotlin.math.abs(currentWeight - prevWeight) / prevWeight * 100f
        val currentVsOutlier = kotlin.math.abs(currentWeight - outlierWeight) / outlierWeight * 100f

        // Condiciones para considerar outlier:
        // 1. El outlier se desvía sustancialmente del anterior (>30%)
        val isSignificantDeviation = outlierVsPrevious > OUTLIER_THRESHOLD_PERCENTAGE
        
        // 2. El valor actual está más cerca del anterior que del outlier
        val returnsToNormal = currentVsPrevious < outlierVsPrevious
        
        // 3. El cambio del outlier al actual también es significativo (confirma que el outlier era anómalo)
        val significantCorrectionFromOutlier = currentVsOutlier > OUTLIER_THRESHOLD_PERCENTAGE

        return isSignificantDeviation && returnsToNormal && significantCorrectionFromOutlier
    }

    data class SaveMeasurementResult(
        val measurementId: Long,
        val processed: Boolean,
        val reason: String = ""
    )
}
