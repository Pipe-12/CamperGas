package com.example.campergas.domain.model

/**
 * Modelo de dominio que representa un dispositivo Bluetooth Low Energy (BLE).
 * 
 * Esta clase de datos encapsula toda la información relevante de un dispositivo BLE
 * descubierto durante el escaneo, incluyendo su identificación, estado de conexión,
 * servicios disponibles y calidad de señal.
 * 
 * Proporciona propiedades calculadas para:
 * - Evaluar la calidad de la señal (excelente, buena, regular, débil)
 * - Verificar compatibilidad con sensores CamperGas
 * - Identificar el tipo de dispositivo basado en su nombre y servicios
 * 
 * @property name Nombre visible del dispositivo BLE
 * @property address Dirección MAC única del dispositivo
 * @property rssi Indicador de intensidad de señal recibida (en dBm, valores negativos)
 * @property isConnected Indica si el dispositivo está actualmente conectado
 * @property services Lista de UUIDs de servicios BLE que el dispositivo anuncia
 * @property isConnectable Indica si el dispositivo acepta conexiones
 * @property lastSeen Timestamp de la última vez que se detectó el dispositivo
 * @author Felipe García Gómez
 */
data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val isConnected: Boolean = false,
    val services: List<String> = emptyList(),
    val isConnectable: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis()
) {
    /**
     * Calidad de la señal interpretada en forma legible.
     * 
     * Clasifica la intensidad de señal RSSI en cuatro categorías:
     * - "Excelente": RSSI >= -50 dBm (señal muy fuerte, dispositivo muy cerca)
     * - "Buena": RSSI >= -70 dBm (señal fuerte, dispositivo cerca)
     * - "Regular": RSSI >= -85 dBm (señal aceptable, dispositivo a distancia media)
     * - "Weak": RSSI < -85 dBm (señal débil, dispositivo lejos o con obstrucciones)
     */
    val signalStrength: String
        get() = when {
            rssi >= -50 -> "Excelente"
            rssi >= -70 -> "Buena"
            rssi >= -85 -> "Regular"
            else -> "Weak"
        }

    /**
     * Verifica si este dispositivo es compatible con CamperGas.
     * 
     * Un dispositivo es compatible si anuncia alguno de los UUIDs de servicio
     * definidos como compatibles con el sistema CamperGas.
     * 
     * @return true si el dispositivo es compatible con CamperGas, false en caso contrario
     */
    val isCompatibleWithCamperGas: Boolean
        get() = CamperGasUuids.isCompatibleDevice(services)

    /**
     * Indica si el dispositivo tiene el servicio principal de sensores CamperGas.
     * 
     * Verifica si entre los servicios anunciados se encuentra el servicio UUID
     * específico del sensor CamperGas para mediciones de peso e inclinación.
     * 
     * @return true si tiene el servicio de sensor CamperGas, false en caso contrario
     */
    val hasCamperGasService: Boolean
        get() = services.any { CamperGasUuids.isSensorService(it) }

    /**
     * Obtiene el tipo de dispositivo basado en su nombre y compatibilidad.
     * 
     * Clasifica el dispositivo analizando su nombre y servicios en las siguientes categorías:
     * - "Sensor CamperGas": Dispositivo con nombre que incluye "CamperGas"
     * - "Sensor de Peso": Dispositivo especializado en medición de peso
     * - "Inclination Sensor": Dispositivo especializado en medición de inclinación
     * - "Dispositivo Compatible": Dispositivo BLE compatible pero sin nombre específico
     * - "Dispositivo BLE": Dispositivo BLE genérico no compatible
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
