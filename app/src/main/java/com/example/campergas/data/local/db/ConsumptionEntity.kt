package com.example.campergas.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.campergas.domain.model.GasCylinder

@Entity(
    tableName = "consumption_table",
    foreignKeys = [
        ForeignKey(
            entity = GasCylinder::class,
            parentColumns = ["id"],
            childColumns = ["cylinderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cylinderId"])]
)
data class ConsumptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cylinderId: Long, // ID de la bombona
    val cylinderName: String, // Nombre de la bombona para referencia
    val date: Long, // Timestamp
    val fuelPercentage: Float, // Porcentaje de combustible (0-100)
    val fuelKilograms: Float, // Kilogramos de combustible disponible
    val duration: Long // Duración en minutos
)
