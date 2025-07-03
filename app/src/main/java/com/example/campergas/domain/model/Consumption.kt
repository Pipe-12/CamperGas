package com.example.campergas.domain.model

data class Consumption(
    val id: Long = 0,
    val cylinderId: Long, // ID de la bombona
    val cylinderName: String, // Nombre de la bombona para referencia
    val date: Long, // Timestamp
    val fuelPercentage: Float, // Porcentaje de combustible (0-100)
    val fuelKilograms: Float, // Kilogramos de combustible disponible
    val duration: Long // Duración en minutos (opcional, para mediciones periódicas)
)
