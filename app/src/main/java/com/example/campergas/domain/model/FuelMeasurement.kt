package com.example.campergas.domain.model

/**
 * Modelo de dominio que representa una medición de combustible de gas.
 * 
 * Esta clase de datos encapsula toda la información relacionada con una medición
 * de nivel de gas en un cilindro específico. Incluye tanto los datos brutos del
 * sensor (peso total) como los valores calculados (kilogramos de gas, porcentaje).
 * 
 * Origen de los datos:
 * - Tiempo real: Mediciones tomadas en el momento desde el sensor BLE
 * - Histórico: Mediciones sincronizadas del almacenamiento offline del sensor
 * 
 * La clase proporciona métodos de utilidad para:
 * - Formatear valores para visualización en UI
 * - Validar la integridad de los datos medidos
 * - Distinguir entre datos en tiempo real e históricos
 * 
 * @property id Identificador único de la medición en la base de datos
 * @property cylinderId ID del cilindro de gas al que pertenece esta medición
 * @property cylinderName Nombre descriptivo del cilindro para referencia en UI
 * @property timestamp Momento Unix (milisegundos) en que se realizó la medición
 * @property fuelKilograms Kilogramos de gas disponible (peso total menos tara del cilindro)
 * @property fuelPercentage Porcentaje de gas disponible respecto a la capacidad total (0-100%)
 * @property totalWeight Peso total medido por el sensor incluyendo cilindro y gas (en kg)
 * @property isCalibrated Indica si la medición está calibrada con la tara correcta del cilindro
 * @property isHistorical Indica si es un dato histórico/offline sincronizado o medición en tiempo real
 * @author Felipe García Gómez
 */
data class FuelMeasurement(
    val id: Long = 0,
    val cylinderId: Long,
    val cylinderName: String,
    val timestamp: Long,
    val fuelKilograms: Float,
    val fuelPercentage: Float,
    val totalWeight: Float,
    val isCalibrated: Boolean = true,
    val isHistorical: Boolean = false
) {
    /**
     * Formatea los kilogramos de combustible para mostrar en la interfaz de usuario.
     * 
     * Devuelve el valor con 2 decimales de precisión seguido de la unidad "kg".
     * Ejemplo: "12.50 kg"
     * 
     * @return Cadena formateada con los kilogramos y la unidad
     */
    fun getFormattedFuelKilograms(): String = "%.2f kg".format(fuelKilograms)

    /**
     * Formatea el porcentaje de combustible para mostrar en la interfaz de usuario.
     * 
     * Devuelve el valor con 1 decimal de precisión seguido del símbolo "%".
     * Ejemplo: "75.5%"
     * 
     * @return Cadena formateada con el porcentaje y el símbolo
     */
    fun getFormattedPercentage(): String = "%.1f%%".format(fuelPercentage)

    /**
     * Formatea el peso total medido para mostrar en la interfaz de usuario.
     * 
     * Devuelve el valor con 2 decimales de precisión seguido de la unidad "kg".
     * Incluye tanto el peso del cilindro como del gas.
     * Ejemplo: "25.75 kg"
     * 
     * @return Cadena formateada con el peso total y la unidad
     */
    fun getFormattedTotalWeight(): String = "%.2f kg".format(totalWeight)

    /**
     * Obtiene la fecha y hora formateada de la medición.
     * 
     * Convierte el timestamp Unix a formato de hora legible "HH:mm:ss"
     * usando la configuración regional del dispositivo.
     * Ejemplo: "14:35:22"
     * 
     * @return Cadena con la hora formateada
     */
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Verifica si la medición contiene valores válidos.
     * 
     * Una medición es válida si:
     * - Los kilogramos de combustible son un número válido (no NaN ni infinito) y no negativos
     * - El porcentaje es un número válido, no negativo y no superior al 100%
     * 
     * Esta validación es importante para detectar errores de sensor o cálculos incorrectos.
     * 
     * @return true si todos los valores son válidos, false si alguno es inválido
     */
    fun isValid(): Boolean =
        !fuelKilograms.isNaN() && !fuelKilograms.isInfinite() && fuelKilograms >= 0 &&
                !fuelPercentage.isNaN() && !fuelPercentage.isInfinite() && fuelPercentage >= 0 && fuelPercentage <= 100
}
