package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.FuelMeasurement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener datos de combustible en tiempo real del sensor BLE.
 * 
 * Este caso de uso encapsula la lógica de negocio para acceder a las mediciones
 * de combustible (peso, porcentaje, kilogramos) calculadas en tiempo real a partir
 * de los datos del sensor de peso BLE.
 * 
 * Los datos de combustible incluyen:
 * - Peso total medido por el sensor (cilindro + gas)
 * - Kilogramos de gas disponible (peso total - tara del cilindro)
 * - Porcentaje de gas disponible respecto a la capacidad total
 * - Información del cilindro activo asociado
 * - Timestamp de la medición
 * 
 * Estos datos se utilizan para:
 * - Mostrar el nivel de gas actual en la pantalla principal
 * - Generar gráficos de consumo y tendencias
 * - Alertar cuando el nivel de gas es bajo
 * - Actualizar widgets de la pantalla de inicio
 * - Registrar historial de consumo en la base de datos
 * 
 * El Flow emite nuevas mediciones conforme el sensor BLE las proporciona,
 * típicamente cada minuto según la configuración de intervalos.
 * 
 * @property bleRepository Repositorio BLE que proporciona acceso a datos del sensor
 * @author Felipe García Gómez
 */
class GetFuelDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Obtiene los datos de combustible en tiempo real como un Flow reactivo.
     * 
     * Retorna un Flow que emite las mediciones de combustible más recientes
     * calculadas a partir de los datos del sensor de peso. El Flow se actualiza
     * automáticamente cuando llegan nuevas mediciones desde el sensor.
     * 
     * El valor puede ser null si:
     * - No hay sensor conectado
     * - No hay cilindro activo configurado
     * - El sensor aún no ha enviado mediciones de peso
     * - Se perdió la conexión con el sensor
     * 
     * @return Flow que emite objetos FuelMeasurement con datos calculados, o null si no hay datos
     */
    operator fun invoke(): Flow<FuelMeasurement?> {
        return bleRepository.fuelData
    }
}
