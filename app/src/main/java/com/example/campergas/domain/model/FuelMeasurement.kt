package com.example.campergas.domain.model

data class FuelMeasurement(
    val id: Long = 0,
    val cylinderId: Long, // ID of the cylinder
    val cylinderName: String, // Nombre of the cylinder for referencia
    val timestamp: Long, // Timestamp of when measurement was taken
    val fuelKilograms: Float, // Kilogramos de combustible disponible (ya calculados)
    val fuelPercentage: Float, // Porcentaje de combustible (0-100)
    val totalWeight: Float, // Peso total medido (cylinder + combustible)
    val isCalibrated: Boolean = true, // Indicates if measurement is calibrated
    val isHistorical: Boolean = false // Indica si es un dato historical
) {
    /**
     * Formatea los kilogramos de combustible for mostrar en la UI
     */
    fun getFormattedFuelKilograms(): String = "%.2f kg".format(fuelKilograms)

    /**
     * Formatea el porcentaje for mostrar en la UI
     */
    fun getFormattedPercentage(): String = "%.1f%%".format(fuelPercentage)

    /**
     * Formatea el total weight medido for mostrar en la UI
     */
    fun getFormattedTotalWeight(): String = "%.2f kg".format(totalWeight)

    /**
     * Gets formatted date of the measurement
     */
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Gets la date y hora completa formatted
     */
    fun getFullFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val formatter =
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Verifies if la measurement es válida
     */
    fun isValid(): Boolean =
        !fuelKilograms.isNaN() && !fuelKilograms.isInfinite() && fuelKilograms >= 0 &&
                !fuelPercentage.isNaN() && !fuelPercentage.isInfinite() && fuelPercentage >= 0 && fuelPercentage <= 100
}
