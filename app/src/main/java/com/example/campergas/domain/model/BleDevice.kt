package com.example.campergas.domain.model

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int, // Signal strength
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
            else -> "Weak"
        }

    /**
     * Verifies if este device es compatible con CamperGas
     */
    val isCompatibleWithCamperGas: Boolean
        get() = CamperGasUuids.isCompatibleDevice(services)

    /**
     * Indica if the device tiene el servicio principal de CamperGas
     */
    val hasCamperGasService: Boolean
        get() = services.any { CamperGasUuids.isSensorService(it) }

    /**
     * Gets el tipo de device basado in the nombre
     */
    val deviceType: String
        get() = when {
            name.contains("CamperGas", ignoreCase = true) -> "Sensor CamperGas"
            name.contains("Weight", ignoreCase = true) -> "Sensor de Peso"
            name.contains("Inclination", ignoreCase = true) -> "Inclination Sensor"
            isCompatibleWithCamperGas -> "Dispositivo Compatible"
            else -> "Dispositivo BLE"
        }
}
