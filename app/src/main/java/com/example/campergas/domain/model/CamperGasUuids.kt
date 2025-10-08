package com.example.campergas.domain.model

object CamperGasUuids {
    // Un único servicio for the sensor de peso con inclinación
    const val SENSOR_SERVICE_UUID = "91bad492-b950-4226-aa2b-4ede9fa42f59"

    // Tres características dentro del mismo servicio
    const val WEIGHT_CHARACTERISTIC_UUID = "cba1d466-344c-4be3-ab3f-189f80dd7518"
    const val OFFLINE_CHARACTERISTIC_UUID = "87654321-4321-4321-4321-cba987654321"
    const val INCLINATION_CHARACTERISTIC_UUID = "fedcba09-8765-4321-fedc-ba0987654321"

    /**
     * Verifica si un dispositivo BLE es compatible con CamperGas
     * @param serviceUuids Lista de UUIDs de servicios of the device
     * @return true if the device es compatible
     */
    fun isCompatibleDevice(serviceUuids: List<String>): Boolean {
        return serviceUuids.any { uuid ->
            uuid.equals(SENSOR_SERVICE_UUID, ignoreCase = true)
        }
    }

    /**
     * Verifica si un UUID corresponde al servicio principal de CamperGas
     * @param uuid UUID a verificar
     * @return true si es el servicio principal
     */
    fun isSensorService(uuid: String): Boolean {
        return uuid.equals(SENSOR_SERVICE_UUID, ignoreCase = true)
    }
}
