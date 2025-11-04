package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.FuelMeasurement
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Caso de uso para guardar mediciones de combustible en la base de datos.
 *
 * Este caso de uso encapsula la lógica de negocio compleja para persistir mediciones
 * de nivel de gas, distinguiendo entre:
 * - Mediciones en tiempo real: Datos actuales del sensor (guardados cada 2 minutos)
 * - Mediciones históricas/offline: Datos sincronizados del almacenamiento del sensor
 *
 * Funcionalidades principales:
 * - Control de frecuencia de guardado (evita spam en BD con límite de 2 minutos)
 * - Cálculo automático de combustible disponible (peso total - tara)
 * - Validación de datos antes de guardar
 * - Detección y eliminación automática de mediciones erróneas (outliers)
 * - Soporte para guardado masivo de datos históricos
 *
 * Detección de outliers:
 * Identifica y elimina mediciones anómalas basándose en patrones:
 * - Una medición que se desvía >30% del valor anterior
 * - Y luego el valor regresa a la normalidad
 * - Se considera un error del sensor y se elimina
 *
 * @property fuelMeasurementRepository Repositorio de mediciones de combustible
 * @property gasCylinderRepository Repositorio de cilindros para obtener tara y capacidad
 * @author Felipe García Gómez
 */
class SaveFuelMeasurementUseCase @Inject constructor(
    private val fuelMeasurementRepository: FuelMeasurementRepository,
    private val gasCylinderRepository: GasCylinderRepository
) {

    companion object {
        /** Tiempo mínimo entre guardados de mediciones en tiempo real (2 minutos) */
        private const val MIN_TIME_BETWEEN_SAVES_MS = 2 * 60 * 1000L

        /** Porcentaje de cambio que se considera anómalo (30%) */
        private const val OUTLIER_THRESHOLD_PERCENTAGE = 30.0f

        /** Número mínimo de mediciones necesarias para detectar outliers */
        private const val MIN_MEASUREMENTS_FOR_OUTLIER_DETECTION = 3
    }

    /**
     * Timestamp de la última medición en tiempo real guardada.
     * Se utiliza para controlar la frecuencia de guardado.
     */
    @Volatile
    private var lastSaveTimestamp: Long = 0L

    /**
     * Guarda una medición de combustible en TIEMPO REAL.
     *
     * Esta función se invoca cuando llegan datos del sensor BLE en tiempo real
     * (característica WEIGHT_CHARACTERISTIC_UUID). Las mediciones se marcan como
     * isHistorical = false.
     *
     * Control de frecuencia:
     * Solo se guardan mediciones cada 2 minutos para evitar saturar la base de datos.
     * Si se intenta guardar antes, se devuelve un resultado indicando el tiempo restante.
     *
     * Proceso:
     * 1. Verifica que haya pasado el tiempo mínimo desde el último guardado
     * 2. Obtiene el cilindro activo y su configuración (tara, capacidad)
     * 3. Calcula combustible disponible y porcentaje
     * 4. Valida los datos calculados
     * 5. Guarda la medición en la base de datos
     * 6. Ejecuta detección de outliers para mantener calidad de datos
     *
     * @param totalWeight Peso total medido por el sensor (cilindro + gas) en kilogramos
     * @param timestamp Timestamp de cuándo se tomó la medición (default: ahora)
     * @return Result con SaveMeasurementResult indicando si se guardó o por qué no
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
     * Guarda múltiples mediciones HISTÓRICAS de combustible.
     *
     * Esta función se invoca cuando se sincronizan datos históricos almacenados
     * en el sensor BLE mientras estuvo desconectado (característica
     * OFFLINE_CHARACTERISTIC_UUID). Las mediciones se marcan como isHistorical = true.
     *
     * A diferencia de las mediciones en tiempo real, estas:
     * - NO tienen límite de frecuencia de guardado
     * - Se guardan en lotes completos
     * - Representan datos del pasado, no mediciones actuales
     *
     * Proceso:
     * 1. Obtiene información del cilindro (tara, capacidad)
     * 2. Para cada par (peso, timestamp):
     *    - Calcula combustible disponible y porcentaje
     *    - Crea objeto FuelMeasurement marcado como histórico
     *    - Valida los datos
     * 3. Filtra solo mediciones válidas
     * 4. Guarda todas las mediciones en lote
     *
     * @param cylinderId ID del cilindro al que pertenecen las mediciones
     * @param weightMeasurements Lista de pares (peso total en kg, timestamp Unix)
     * @return Result con el número de mediciones guardadas o error
     */
    suspend fun saveHistoricalMeasurements(
        cylinderId: Long,
        weightMeasurements: List<Pair<Float, Long>>
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
     * Detecta y elimina mediciones erróneas (outliers) basándose en patrones de peso.
     *
     * Un outlier es una medición que se desvía significativamente del patrón esperado
     * y probablemente representa un error del sensor. Esta función analiza las últimas
     * 3 mediciones buscando el patrón: anterior -> outlier -> actual
     *
     * Patrón de detección:
     * - Medición anterior: peso normal (ej: 25 kg)
     * - Outlier: peso sustancialmente diferente >30% (ej: 15 kg - error)
     * - Medición actual: peso vuelve a normal (ej: 24.8 kg)
     *
     * Si se detecta este patrón, la medición del medio se considera errónea
     * y se elimina de la base de datos para mantener la calidad de los datos.
     *
     * Esta función se ejecuta automáticamente después de cada guardado de
     * medición en tiempo real.
     *
     * @param cylinderId ID del cilindro a analizar
     */
    private suspend fun detectAndRemoveOutliers(cylinderId: Long) {
        try {
            // Get last 3 measurements for pattern analysis
            val recentMeasurements = fuelMeasurementRepository.getLastNMeasurements(
                cylinderId,
                MIN_MEASUREMENTS_FOR_OUTLIER_DETECTION
            )

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
     * Determina si una medición es un outlier analizando el patrón de tres mediciones.
     *
     * Analiza tres mediciones consecutivas (anterior -> candidata -> actual) y determina
     * si la medición del medio es anómala.
     *
     * Criterios para considerar outlier:
     * 1. Desviación significativa (>30%) respecto al valor anterior
     * 2. El valor actual regresa cerca del valor anterior (diferencia <30%)
     * 3. La desviación del outlier es mayor que la variación normal entre anterior y actual
     *
     * Ejemplo de outlier detectado:
     * - Anterior: 25.0 kg
     * - Outlier: 15.0 kg (40% cambio - anómalo)
     * - Actual: 24.8 kg (vuelve a normal)
     *
     * Ejemplo de cambio normal (NO outlier):
     * - Anterior: 25.0 kg
     * - Media: 24.0 kg (consumo gradual)
     * - Actual: 23.0 kg (tendencia consistente)
     *
     * @param previous Medición anterior (más antigua)
     * @param outlier Medición candidata a outlier (medio)
     * @param current Medición actual (más reciente)
     * @return true si la medición del medio es un outlier que debe eliminarse
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

    /**
     * Resultado del guardado de una medición.
     *
     * Contiene información sobre si la medición se procesó y guardó exitosamente,
     * o por qué no se guardó (ej: tiempo mínimo no transcurrido, datos inválidos).
     *
     * @property measurementId ID asignado a la medición guardada, o -1 si no se guardó
     * @property processed true si la medición se guardó, false si se omitió
     * @property reason Explicación de por qué se guardó o no se guardó la medición
     */
    data class SaveMeasurementResult(
        val measurementId: Long,
        val processed: Boolean,
        val reason: String = ""
    )
}
