package com.example.campergas.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "weight_measurements",
    foreignKeys = [
        ForeignKey(
            entity = GasCylinder::class,
            parentColumns = ["id"],
            childColumns = ["cylinderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("cylinderId"), Index("timestamp")]
)
data class Weight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val value: Float, // Valor en kg
    val timestamp: Long = System.currentTimeMillis(), // Tiempo de la medición
    val unit: String = "kg", // Unidad de medida
    val isCalibrated: Boolean = true, // Indica si la medición está calibrada
    val cylinderId: Long? = null, // ID de la bombona asociada
    val isHistorical: Boolean = false // Indica si es un dato histórico del sensor
) {
    /**
     * Formatea el peso para mostrar en la UI
     */
    fun getFormattedValue(): String = "%.1f %s".format(value, unit)
    
    /**
     * Verifica si la lectura es válida
     */
    fun isValid(): Boolean = !value.isNaN() && !value.isInfinite() && value >= 0
    
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
}
