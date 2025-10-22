package com.example.campergas.domain.model

/**
 * Objeto singleton que contiene los UUIDs de servicios y características BLE de CamperGas.
 * 
 * Este objeto centraliza todas las constantes UUID necesarias para la comunicación
 * con los sensores CamperGas mediante Bluetooth Low Energy (BLE). Los sensores
 * CamperGas utilizan un único servicio BLE con tres características diferentes:
 * 
 * Arquitectura del sensor:
 * - 1 Servicio BLE principal (SENSOR_SERVICE_UUID)
 *   - Característica de peso en tiempo real (WEIGHT_CHARACTERISTIC_UUID)
 *   - Característica de datos históricos offline (OFFLINE_CHARACTERISTIC_UUID)
 *   - Característica de inclinación (INCLINATION_CHARACTERISTIC_UUID)
 * 
 * Estas constantes se utilizan para:
 * - Identificar dispositivos compatibles durante el escaneo BLE
 * - Suscribirse a las características correctas después de conectar
 * - Leer datos de peso, inclinación e históricos del sensor
 * 
 * @author Felipe García Gómez
 */
object CamperGasUuids {
    /**
     * UUID del servicio BLE principal del sensor CamperGas.
     * 
     * Este es el servicio único que contiene todas las características de medición
     * (peso, inclinación y datos históricos). Un dispositivo BLE debe anunciar este
     * UUID para ser considerado un sensor CamperGas compatible.
     */
    const val SENSOR_SERVICE_UUID = "91bad492-b950-4226-aa2b-4ede9fa42f59"

    /**
     * UUID de la característica BLE para mediciones de peso en tiempo real.
     * 
     * Esta característica proporciona el peso total actual del cilindro de gas
     * medido por el sensor. Los datos se obtienen mediante lectura (READ operation).
     * Formato de datos: JSON {"w": peso_en_kg}
     */
    const val WEIGHT_CHARACTERISTIC_UUID = "cba1d466-344c-4be3-ab3f-189f80dd7518"
    
    /**
     * UUID de la característica BLE para datos históricos offline.
     * 
     * Esta característica proporciona mediciones históricas almacenadas en el sensor
     * cuando estuvo desconectado. Los datos se obtienen mediante lecturas sucesivas
     * hasta que el sensor no tiene más datos que enviar.
     * Formato de datos: JSON array [{"w": peso_en_kg, "t": milisegundos_transcurridos}, ...]
     */
    const val OFFLINE_CHARACTERISTIC_UUID = "87654321-4321-4321-4321-cba987654321"
    
    /**
     * UUID de la característica BLE para mediciones de inclinación.
     * 
     * Esta característica proporciona los datos de inclinación del vehículo en dos ejes
     * (pitch y roll) obtenidos del acelerómetro/giroscopio integrado en el sensor.
     * Formato de datos: JSON {"p": pitch_en_grados, "r": roll_en_grados}
     */
    const val INCLINATION_CHARACTERISTIC_UUID = "fedcba09-8765-4321-fedc-ba0987654321"

    /**
     * Verifica si un dispositivo BLE es compatible con CamperGas.
     * 
     * Un dispositivo es compatible si anuncia el servicio UUID principal de CamperGas
     * entre sus servicios disponibles. Esta verificación se realiza durante el escaneo
     * BLE para filtrar dispositivos no compatibles.
     * 
     * @param serviceUuids Lista de UUIDs de servicio anunciados por el dispositivo BLE
     * @return true si el dispositivo es compatible con CamperGas, false en caso contrario
     */
    fun isCompatibleDevice(serviceUuids: List<String>): Boolean {
        return serviceUuids.any { uuid ->
            uuid.equals(SENSOR_SERVICE_UUID, ignoreCase = true)
        }
    }

    /**
     * Verifica si un UUID corresponde al servicio principal de sensores CamperGas.
     * 
     * Compara el UUID proporcionado con el UUID del servicio principal del sensor,
     * ignorando diferencias de mayúsculas/minúsculas para mayor robustez.
     * 
     * @param uuid UUID del servicio a verificar
     * @return true si es el servicio principal de CamperGas, false en caso contrario
     */
    fun isSensorService(uuid: String): Boolean {
        return uuid.equals(SENSOR_SERVICE_UUID, ignoreCase = true)
    }
}
