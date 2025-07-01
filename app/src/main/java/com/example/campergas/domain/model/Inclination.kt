package com.example.campergas.domain.model

data class Inclination(
    val pitch: Float, // Inclinación pitch (cabeceo) en grados
    val roll: Float,  // Inclinación roll (alabeo) en grados
    val timestamp: Long = System.currentTimeMillis() // Tiempo de la medición
) {
    /**
     * Calcula si el dispositivo está nivelado en pitch (tolerancia ±2°)
     */
    val isLevelPitch: Boolean
        get() = kotlin.math.abs(pitch) <= 2.0f
        
    /**
     * Calcula si el dispositivo está nivelado en roll (tolerancia ±2°)
     */
    val isLevelRoll: Boolean
        get() = kotlin.math.abs(roll) <= 2.0f
        
    /**
     * Calcula si el dispositivo está completamente nivelado
     */
    val isLevel: Boolean
        get() = isLevelPitch && isLevelRoll
        
    /**
     * Obtiene una descripción formateada de la inclinación
     */
    fun getFormattedValue(): String = "P: %.1f° R: %.1f°".format(pitch, roll)
    
    /**
     * Obtiene la fecha formateada de la medición
     */
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
