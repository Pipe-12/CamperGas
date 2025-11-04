package com.example.campergas.domain.model

/**
 * Modelo de dominio que representa la configuración geométrica de un vehículo recreativo.
 *
 * Esta clase de datos almacena las dimensiones físicas del vehículo (caravana o autocaravana)
 * necesarias para calcular correctamente:
 * - Distribución de peso en los ejes
 * - Nivelación del vehículo
 * - Elevación necesaria en las ruedas para corregir inclinación
 * - Consumo estimado de gas
 *
 * Las dimensiones son críticas para:
 * - Cálculos de estabilidad basados en inclinación del sensor
 * - Recomendaciones de nivelación mediante calzos
 * - Visualización correcta en los widgets de estabilidad
 *
 * Diferencias por tipo de vehículo:
 * - CARAVAN: Usa distanceToFrontSupport (punto de apoyo delantero en la lanza)
 * - AUTOCARAVANA: Usa distanceBetweenFrontWheels (eje delantero completo)
 *
 * @property type Tipo de vehículo (caravana o autocaravana)
 * @property distanceBetweenRearWheels Distancia entre las ruedas del eje trasero en centímetros
 * @property distanceToFrontSupport Distancia desde eje trasero hasta punto de apoyo delantero en cm (solo caravanas)
 * @property distanceBetweenFrontWheels Distancia entre las ruedas del eje delantero en cm (solo autocaravanas)
 * @property gasTankCapacity Capacidad total del sistema de gas en kilogramos
 * @author Felipe García Gómez
 */
data class VehicleConfig(
    val type: VehicleType = VehicleType.CARAVAN,
    val distanceBetweenRearWheels: Float,
    val distanceToFrontSupport: Float,
    val distanceBetweenFrontWheels: Float? = null,
    val gasTankCapacity: Float = 100.0f
)
