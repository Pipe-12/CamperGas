package com.example.campergas.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.campergas.domain.model.GasCylinder

@Entity(
    tableName = "fuel_measurements",
    foreignKeys = [
        ForeignKey(
            entity = GasCylinder::class,
            parentColumns = ["id"],
            childColumns = ["cylinderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cylinderId"]),
        Index(value = ["timestamp"])
    ]
)
data class FuelMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cylinderId: Long, // ID de la bombona
    val cylinderName: String, // Nombre de la bombona para referencia
    val timestamp: Long, // Timestamp de cuando se tomó la medición
    val fuelKilograms: Float, // Kilogramos de combustible disponible (ya calculados)
    val fuelPercentage: Float, // Porcentaje de combustible (0-100)
    val totalWeight: Float, // Peso total medido (bombona + combustible)
    val isCalibrated: Boolean = true, // Indica si la medición está calibrada
    val isHistorical: Boolean = false // Indica si es un dato histórico
) {
    /**
     * Formatea los kilogramos de combustible para mostrar en la UI
     */
    fun getFormattedFuelKilograms(): String = "%.2f kg".format(fuelKilograms)
    
    /**
     * Formatea el porcentaje para mostrar en la UI
     */
    fun getFormattedPercentage(): String = "%.1f%%".format(fuelPercentage)
    
    /**
     * Obtiene la fecha formateada de la medición
     */
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Obtiene la fecha y hora completa formateada
     */
    fun getFullFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Verifica si la medición es válida
     */
    fun isValid(): Boolean = 
        !fuelKilograms.isNaN() && !fuelKilograms.isInfinite() && fuelKilograms >= 0 &&
        !fuelPercentage.isNaN() && !fuelPercentage.isInfinite() && fuelPercentage >= 0 && fuelPercentage <= 100
}
