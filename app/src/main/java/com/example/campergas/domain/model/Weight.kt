package com.example.campergas.domain.model

data class Weight(
    val value: Float, // Valor en kg
    val timestamp: Long, // Tiempo de la medición
    val isCalibrated: Boolean // Indica si la medición está calibrada
)
