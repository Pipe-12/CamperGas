package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.Inclination
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener datos de inclinación del sensor BLE.
 *
 * Este caso de uso encapsula la lógica de negocio para acceder a las mediciones
 * de inclinación (pitch y roll) en tiempo real del sensor acelerómetro/giroscopio
 * integrado en el dispositivo BLE CamperGas.
 *
 * Los datos de inclinación se utilizan para:
 * - Monitorear la estabilidad del vehículo en tiempo real
 * - Detectar superficies irregulares o pendientes pronunciadas
 * - Calcular la nivelación necesaria para estabilizar el vehículo
 * - Generar alertas de seguridad cuando el vehículo no está nivelado
 * - Mostrar visualizaciones de estabilidad en pantallas y widgets
 *
 * El Flow emite nuevas mediciones conforme el sensor BLE las proporciona,
 * típicamente cada pocos segundos según la configuración de intervalos.
 *
 * @property bleRepository Repositorio BLE que proporciona acceso a datos del sensor
 * @author Felipe García Gómez
 */
class GetInclinationUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Obtiene los datos de inclinación como un Flow reactivo.
     *
     * Retorna un Flow que emite las mediciones de inclinación más recientes
     * del sensor BLE. El Flow se actualiza automáticamente cuando llegan
     * nuevas mediciones desde el sensor.
     *
     * El valor puede ser null si:
     * - No hay sensor conectado
     * - El sensor aún no ha enviado mediciones de inclinación
     * - Se perdió la conexión con el sensor
     *
     * @return Flow que emite objetos Inclination con pitch y roll, o null si no hay datos
     */
    operator fun invoke(): Flow<Inclination?> {
        return bleRepository.inclinationData
    }
}
