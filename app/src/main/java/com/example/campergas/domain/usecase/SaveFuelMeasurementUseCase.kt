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

    data class SaveMeasurementResult(
        val measurementId: Long,
        val processed: Boolean,
        val reason: String = ""
    )
}
