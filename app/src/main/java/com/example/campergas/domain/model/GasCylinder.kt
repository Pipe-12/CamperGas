package com.example.campergas.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gas_cylinders")
data class GasCylinder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val tare: Float, // Peso of the bombona vacía en kg
    val capacity: Float, // Capacidad de gas en kg
    val isActive: Boolean = false, // Solo una bombona puede estar activa
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculates el contenido de gas basado in the total weight
     * @form totalWeight Peso total of the bombona (tara + gas)
     * @return Peso del gas en kg
     */
    fun calculateGasContent(totalWeight: Float): Float {
        val gasWeight = totalWeight - tare
        return if (gasWeight < 0) 0f else gasWeight
    }

    /**
     * Calculates el porcentaje de gas restante
     * @form totalWeight Peso total of the bombona
     * @return Porcentaje de gas (0-100)
     */
    fun calculateGasPercentage(totalWeight: Float): Float {
        val gasContent = calculateGasContent(totalWeight)
        return if (capacity > 0) (gasContent / capacity * 100).coerceIn(0f, 100f) else 0f
    }

    /**
     * Determina si la bombona está vacía (menos del 5% de gas)
     */
    fun isEmpty(totalWeight: Float): Boolean {
        return calculateGasPercentage(totalWeight) < 5f
    }

    /**
     * Determina si la bombona está casi vacía (menos del 20% de gas)
     */
    fun isLowGas(totalWeight: Float): Boolean {
        return calculateGasPercentage(totalWeight) < 20f
    }

    /**
     * Returns una description formatted of the bombona
     */
    fun getDisplayName(): String {
        return "$name (${tare}kg + ${capacity}kg)"
    }
}
