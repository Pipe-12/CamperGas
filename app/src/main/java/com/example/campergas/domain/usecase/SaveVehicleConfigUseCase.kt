package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.VehicleRepository
import com.example.campergas.domain.model.VehicleConfig
import com.example.campergas.domain.model.VehicleType
import javax.inject.Inject

/**
 * Caso de uso para guardar o actualizar la configuración del vehículo recreativo.
 *
 * Este caso de uso encapsula la lógica de negocio para persistir la configuración
 * geométrica del vehículo (caravana o autocaravana) en la base de datos.
 *
 * Comportamiento:
 * - Si no existe configuración previa, crea una nueva
 * - Si ya existe configuración, la actualiza (solo hay una configuración por app)
 *
 * Proporciona dos formas de guardar:
 * 1. Mediante parámetros individuales (tipo, distancias)
 * 2. Mediante un objeto VehicleConfig completo
 *
 * La configuración guardada se utiliza para:
 * - Cálculos de nivelación y estabilidad
 * - Visualizaciones de geometría del vehículo
 * - Recomendaciones de elevación de ruedas
 * - Distribución de peso en los ejes
 *
 * Validaciones recomendadas (no implementadas aquí, delegar a la UI):
 * - Distancias deben ser positivas
 * - Distancias deben estar en rangos realistas (ej: 100-300 cm)
 * - Para autocaravanas, distanceBetweenFrontWheels no debe ser null
 *
 * @property vehicleRepository Repositorio de configuración de vehículos
 * @author Felipe García Gómez
 */
class SaveVehicleConfigUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    /**
     * Guarda o actualiza la configuración del vehículo mediante parámetros individuales.
     *
     * Crea un objeto VehicleConfig con los parámetros proporcionados y lo persiste
     * en la base de datos. Si ya existe una configuración, la reemplaza.
     *
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @param type Tipo de vehículo (CARAVAN o AUTOCARAVANA)
     * @param distanceBetweenRearWheels Distancia entre ruedas traseras en centímetros
     * @param distanceToFrontSupport Distancia desde eje trasero al apoyo delantero en cm (caravanas)
     * @param distanceBetweenFrontWheels Distancia entre ruedas delanteras en cm (autocaravanas, opcional)
     */
    suspend operator fun invoke(
        type: VehicleType,
        distanceBetweenRearWheels: Float,
        distanceToFrontSupport: Float,
        distanceBetweenFrontWheels: Float? = null
    ) {
        val config = VehicleConfig(
            type = type,
            distanceBetweenRearWheels = distanceBetweenRearWheels,
            distanceToFrontSupport = distanceToFrontSupport,
            distanceBetweenFrontWheels = distanceBetweenFrontWheels
        )

        vehicleRepository.saveVehicleConfig(config)
    }

}
