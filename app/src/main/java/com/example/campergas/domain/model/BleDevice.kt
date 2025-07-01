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
    
    /**
     * Verifica si este dispositivo es compatible con CamperGas
     */
    val isCompatibleWithCamperGas: Boolean
        get() = CamperGasUuids.isCompatibleDevice(services)
    
    /**
     * Indica si el dispositivo tiene el servicio principal de CamperGas
     */
    val hasCamperGasService: Boolean
        get() = services.any { CamperGasUuids.isSensorService(it) }
        
    /**
     * Obtiene el tipo de dispositivo basado en el nombre
     */
    val deviceType: String
        get() = when {
            name.contains("CamperGas", ignoreCase = true) -> "Sensor CamperGas"
            name.contains("Weight", ignoreCase = true) -> "Sensor de Peso"
            name.contains("Inclination", ignoreCase = true) -> "Sensor de Inclinación"
            isCompatibleWithCamperGas -> "Dispositivo Compatible"
            else -> "Dispositivo BLE"
        }
}
