package com.example.campergas.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
    val cylinderId: Long, // ID of the cylinder
    val cylinderName: String, // Nombre of the cylinder for referencia
    val timestamp: Long, // Timestamp of when measurement was taken
    val fuelKilograms: Float, // Kilogramos de combustible disponible (ya calculados)
    val fuelPercentage: Float, // Porcentaje de combustible (0-100)
    val totalWeight: Float, // Peso total medido (cylinder + combustible)
    val isCalibrated: Boolean = true, // Indicates if measurement is calibrated
    val isHistorical: Boolean = false // Indica si es un dato historical
)
