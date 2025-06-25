package com.example.campergas.domain.model

data class Inclination(
    val xAxis: Float, // Inclinación en eje X (longitudinal)
    val yAxis: Float, // Inclinación en eje Y (lateral)
    val timestamp: Long, // Tiempo de la medición
    val isLevelX: Boolean, // Indica si está nivelado en el eje X
    val isLevelY: Boolean  // Indica si está nivelado en el eje Y
)
