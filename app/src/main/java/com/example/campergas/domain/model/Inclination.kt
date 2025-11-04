package com.example.campergas.domain.model

/**
 * Modelo de dominio que representa la inclinación de un vehículo en dos ejes.
 *
 * Esta clase de datos encapsula las mediciones de inclinación obtenidas del sensor
 * acelerómetro/giroscopio integrado en el dispositivo BLE. Proporciona información
 * sobre la orientación del vehículo en los ejes pitch (cabeceo) y roll (balanceo).
 *
 * Proporciona métodos útiles para:
 * - Determinar si el vehículo está nivelado en cada eje
 * - Verificar si el vehículo está completamente nivelado
 * - Formatear el timestamp de la medición
 *
 * Usos principales:
 * - Monitorización de estabilidad del vehículo
 * - Detección de superficies irregulares o pendientes
 * - Alertas de seguridad cuando el vehículo no está nivelado
 * - Widgets de visualización de estabilidad
 *
 * @property pitch Inclinación en el eje pitch/cabeceo en grados (-180° a +180°). Positivo = inclinación hacia adelante
 * @property roll Inclinación en el eje roll/balanceo en grados (-180° a +180°). Positivo = inclinación hacia la derecha
 * @property timestamp Momento en que se realizó la medición (timestamp Unix en milisegundos)
 * @author Felipe García Gómez
 */
data class Inclination(
    val pitch: Float,
    val roll: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calcula si el vehículo está nivelado en el eje pitch (cabeceo).
     *
     * Se considera nivelado si la inclinación está dentro de la tolerancia
     * de ±2 grados respecto a la horizontal. Esta tolerancia permite pequeñas
     * variaciones que no afectan significativamente a la estabilidad.
     *
     * @return true si el pitch está entre -2° y +2°, false en caso contrario
     */
    val isLevelPitch: Boolean
        get() = kotlin.math.abs(pitch) <= 2.0f

    /**
     * Calcula si el vehículo está nivelado en el eje roll (balanceo).
     *
     * Se considera nivelado si la inclinación está dentro de la tolerancia
     * de ±2 grados respecto a la horizontal. Esta tolerancia permite pequeñas
     * variaciones que no afectan significativamente a la estabilidad.
     *
     * @return true si el roll está entre -2° y +2°, false en caso contrario
     */
    val isLevelRoll: Boolean
        get() = kotlin.math.abs(roll) <= 2.0f

    /**
     * Calcula si el vehículo está completamente nivelado en ambos ejes.
     *
     * El vehículo se considera completamente nivelado solo cuando tanto
     * el pitch como el roll están dentro de sus respectivas tolerancias
     * de ±2 grados. Esto indica que el vehículo está sobre una superficie
     * relativamente plana y horizontal.
     *
     * @return true si ambos ejes están nivelados, false en caso contrario
     */
    val isLevel: Boolean
        get() = isLevelPitch && isLevelRoll

    /**
     * Formatea el timestamp de la medición en formato de hora legible.
     *
     * Convierte el timestamp Unix en una cadena con formato "HH:mm:ss"
     * usando la configuración regional del dispositivo.
     *
     * @return Cadena con la hora formateada (ej: "14:35:22")
     */
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
