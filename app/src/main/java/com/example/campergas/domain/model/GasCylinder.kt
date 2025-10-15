package com.example.campergas.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gas_cylinders")
data class GasCylinder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val tare: Float, // Empty cylinder weight en kg
    val capacity: Float, // Capacidad de gas en kg
    val isActive: Boolean = false, // Solo una cylinder puede estar activa
    val createdAt: Long = System.currentTimeMillis()
) {

}
