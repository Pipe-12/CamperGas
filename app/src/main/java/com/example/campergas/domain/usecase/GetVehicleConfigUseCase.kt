package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.VehicleRepository
import com.example.campergas.domain.model.VehicleConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener la configuración del vehículo recreativo.
 * 
 * Este caso de uso encapsula la lógica de negocio para recuperar la configuración
 * geométrica del vehículo (caravana o autocaravana) almacenada en el sistema.
 * 
 * La configuración del vehículo incluye:
 * - Tipo de vehículo (caravana o autocaravana)
 * - Distancia entre ruedas traseras
 * - Distancia al punto de apoyo delantero (caravanas)
 * - Distancia entre ruedas delanteras (autocaravanas)
 * - Capacidad total del sistema de gas
 * 
 * Esta información es crítica para:
 * - Calcular correctamente la distribución de peso en los ejes
 * - Determinar la elevación necesaria en las ruedas para nivelar
 * - Visualizar correctamente la geometría del vehículo en la UI
 * - Generar recomendaciones precisas de nivelación
 * 
 * @property vehicleRepository Repositorio de configuración de vehículos
 * @author Felipe García Gómez
 */
class GetVehicleConfigUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    /**
     * Obtiene la configuración del vehículo como un Flow reactivo.
     * 
     * Retorna un Flow que emite la configuración actual del vehículo y se
     * actualiza automáticamente cuando se modifica la configuración.
     * 
     * El valor puede ser null si el usuario aún no ha configurado su vehículo.
     * En ese caso, la aplicación debería mostrar la pantalla de configuración
     * inicial para que el usuario ingrese las dimensiones.
     * 
     * @return Flow que emite la configuración del vehículo o null si no está configurado
     */
    operator fun invoke(): Flow<VehicleConfig?> {
        return vehicleRepository.getVehicleConfig()
    }
}
