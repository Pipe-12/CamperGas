package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Caso de uso para configurar los intervalos de lectura periódica del sensor BLE.
 *
 * Este caso de uso encapsula la lógica de negocio para ajustar la frecuencia con
 * la que se solicitan datos al sensor BLE. Permite configurar intervalos diferentes
 * para mediciones de peso e inclinación de forma independiente.
 *
 * Intervalos configurables:
 * - Peso: Define cada cuántos segundos se lee el peso del cilindro
 * - Inclinación: Define cada cuántos segundos se lee la inclinación del vehículo
 *
 * Consideraciones de los intervalos:
 * - Intervalos más cortos: Mayor precisión y datos más actualizados, mayor consumo de batería
 * - Intervalos más largos: Menor consumo de batería, menor frecuencia de actualización
 *
 * Valores recomendados:
 * - Peso: 60 segundos (1 minuto) - El gas no cambia rápidamente
 * - Inclinación: 5-15 segundos - Permite monitoreo en tiempo real de estabilidad
 *
 * La configuración se persiste y se aplica inmediatamente al sensor conectado.
 * Si no hay sensor conectado, la configuración se aplicará en la próxima conexión.
 *
 * @property bleRepository Repositorio BLE que gestiona la comunicación con el sensor
 * @author Felipe García Gómez
 */
class ConfigureReadingIntervalsUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {

    /**
     * Configura el intervalo de lectura de peso del sensor.
     *
     * Establece cada cuántos segundos el sistema solicitará una nueva medición
     * de peso al sensor BLE. El cambio se aplica inmediatamente si hay sensor
     * conectado, y se guarda para futuras conexiones.
     *
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @param intervalSeconds Intervalo en segundos entre lecturas de peso (ej: 60 para 1 minuto)
     */
    suspend fun setWeightReadInterval(intervalSeconds: Int) {
        val intervalMs = intervalSeconds * 1000L
        bleRepository.saveWeightReadInterval(intervalMs)
        bleRepository.configureReadingIntervals(
            weightIntervalMs = intervalMs,
            inclinationIntervalMs = bleRepository.getInclinationReadInterval()
        )
    }

    /**
     * Configura el intervalo de lectura de inclinación del sensor.
     *
     * Establece cada cuántos segundos el sistema solicitará una nueva medición
     * de inclinación al sensor BLE. El cambio se aplica inmediatamente si hay
     * sensor conectado, y se guarda para futuras conexiones.
     *
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @param intervalSeconds Intervalo en segundos entre lecturas de inclinación (ej: 15 para 15 segundos)
     */
    suspend fun setInclinationReadInterval(intervalSeconds: Int) {
        val intervalMs = intervalSeconds * 1000L
        bleRepository.saveInclinationReadInterval(intervalMs)
        bleRepository.configureReadingIntervals(
            weightIntervalMs = bleRepository.getWeightReadInterval(),
            inclinationIntervalMs = intervalMs
        )
    }

    /**
     * Obtiene el intervalo actual de lectura de peso en segundos.
     *
     * Retorna un Flow que emite el intervalo configurado en segundos y se
     * actualiza cuando se modifica la configuración.
     *
     * @return Flow que emite el intervalo de peso en segundos
     */
    fun getWeightReadIntervalSeconds(): Flow<Int> {
        return bleRepository.weightReadInterval.map { it.toInt() / 1000 }
    }

    /**
     * Obtiene el intervalo actual de lectura de inclinación en segundos.
     *
     * Retorna un Flow que emite el intervalo configurado en segundos y se
     * actualiza cuando se modifica la configuración.
     *
     * @return Flow que emite el intervalo de inclinación en segundos
     */
    fun getInclinationReadIntervalSeconds(): Flow<Int> {
        return bleRepository.inclinationReadInterval.map { it.toInt() / 1000 }
    }

}
