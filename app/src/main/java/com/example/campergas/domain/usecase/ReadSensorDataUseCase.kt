package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Caso de uso para leer datos del sensor CamperGas bajo demanda.
 *
 * Este caso de uso encapsula la lógica de negocio para solicitar lecturas
 * inmediatas de datos del sensor BLE, fuera del ciclo automático de lecturas
 * periódicas. Permite al usuario o al sistema obtener mediciones actualizadas
 * en cualquier momento.
 *
 * Funcionalidades proporcionadas:
 * - Lectura bajo demanda de peso del cilindro
 * - Lectura bajo demanda de inclinación del vehículo
 * - Lectura combinada de todos los datos del sensor
 * - Consulta del estado de conexión
 *
 * Las lecturas bajo demanda son útiles para:
 * - Actualizar datos cuando el usuario lo solicita (botón "refrescar")
 * - Obtener mediciones antes de operaciones críticas
 * - Verificar valores sin esperar al próximo ciclo automático
 * - Responder a eventos de la aplicación que requieren datos actuales
 *
 * Los resultados se entregan de forma asíncrona a través de los StateFlows
 * correspondientes en el repositorio BLE.
 *
 * @property bleRepository Repositorio BLE que gestiona la comunicación con el sensor
 * @author Felipe García Gómez
 */
class ReadSensorDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {

    /**
     * Solicita lectura de datos de peso bajo demanda.
     *
     * Ejecuta una lectura BLE inmediata de la característica de peso.
     * El resultado se entregará a través del StateFlow de datos de combustible
     * del repositorio BLE.
     *
     * Esta función es privada porque se prefiere usar readAllSensorData()
     * para obtener una vista completa del estado del sensor.
     */
    private fun readWeightData() {
        bleRepository.readWeightDataOnDemand()
    }

    /**
     * Solicita lectura de datos de inclinación bajo demanda.
     *
     * Ejecuta una lectura BLE inmediata de la característica de inclinación.
     * El resultado se entregará a través del StateFlow de datos de inclinación
     * del repositorio BLE.
     *
     * Esta función es privada porque se prefiere usar readAllSensorData()
     * para obtener una vista completa del estado del sensor.
     */
    private fun readInclinationData() {
        bleRepository.readInclinationDataOnDemand()
    }

    /**
     * Solicita lectura de todos los datos del sensor (peso e inclinación) bajo demanda.
     *
     * Ejecuta lecturas BLE inmediatas de ambas características:
     * - Peso total del cilindro
     * - Inclinación del vehículo (pitch y roll)
     *
     * Los resultados se entregan de forma asíncrona a través de sus respectivos
     * StateFlows. Esta es la forma recomendada de obtener una actualización
     * completa del estado del sensor.
     *
     * Es seguro llamar este método incluso si no hay sensor conectado (no hace nada).
     */
    fun readAllSensorData() {
        readWeightData()
        readInclinationData()
    }

    /**
     * Obtiene el estado de conexión BLE como StateFlow.
     *
     * Permite observar el estado de conexión en tiempo real, útil para
     * habilitar/deshabilitar la funcionalidad de lectura de datos según
     * si hay un sensor conectado.
     *
     * @return StateFlow que emite true si hay conexión activa, false en caso contrario
     */
    fun getConnectionState(): StateFlow<Boolean> {
        return bleRepository.connectionState
    }
}
