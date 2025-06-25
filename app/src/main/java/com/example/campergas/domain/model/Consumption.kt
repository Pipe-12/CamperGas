package com.example.campergas.domain.model

data class Consumption(
    val id: Long = 0,
    val date: Long, // Timestamp
    val initialWeight: Float, // Peso inicial en kg
    val finalWeight: Float, // Peso final en kg
    val consumptionValue: Float, // En kg o litros
    val duration: Long // Duración en minutos
)
