package com.example.campergas.domain.model

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int, // Intensidad de la señal
    val isConnected: Boolean = false
)
