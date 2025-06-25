package com.example.campergas.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "consumption_table")
data class ConsumptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // Timestamp
    val initialWeight: Float,
    val finalWeight: Float,
    val consumptionValue: Float, // En kg o litros
    val duration: Long // Duración en minutos
)
