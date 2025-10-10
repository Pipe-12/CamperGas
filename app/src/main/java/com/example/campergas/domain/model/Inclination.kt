package com.example.campergas.domain.model

data class Inclination(
    val pitch: Float, // Pitch inclination (pitch) in degrees
    val roll: Float,  // Roll inclination (roll) in degrees
    val timestamp: Long = System.currentTimeMillis() // Measurement time
) {
    /**
     * Calculatestes if device is leveled en pitch (tolerance ±2°)
     */
    val isLevelPitch: Boolean
        get() = kotlin.math.abs(pitch) <= 2.0f

    /**
     * Calculatestes if device is leveled en roll (tolerance ±2°)
     */
    val isLevelRoll: Boolean
        get() = kotlin.math.abs(roll) <= 2.0f

    /**
     * Calculates if the device está completely nivelado
     */
    val isLevel: Boolean
        get() = isLevelPitch && isLevelRoll

    /**
     * Gets a formatted description of the inclination
     */
    fun getFormattedValue(): String = "P: %.1f° R: %.1f°".format(pitch, roll)

    /**
     * Gets formatted date of the measurement
     */
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
