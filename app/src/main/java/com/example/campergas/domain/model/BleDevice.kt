package com.example.campergas.domain.model

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int, // Intensidad de la señal
    val isConnected: Boolean = false,
    val services: List<String> = emptyList(), // UUIDs de servicios disponibles
    val isConnectable: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis()
) {
    val signalStrength: String
        get() = when {
            rssi >= -50 -> "Excelente"
            rssi >= -70 -> "Buena"
            rssi >= -85 -> "Regular"
            else -> "Débil"
        }
}
