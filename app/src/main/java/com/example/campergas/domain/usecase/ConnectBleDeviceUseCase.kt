package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para conectar y desconectar dispositivos BLE sensores de CamperGas.
 * 
 * Este caso de uso encapsula la lógica de negocio para establecer y terminar
 * conexiones BLE con sensores CamperGas. Gestiona tanto la conexión física
 * como el almacenamiento de la dirección del último dispositivo conectado para
 * permitir reconexiones automáticas.
 * 
 * Funcionalidades:
 * - Conexión a un sensor BLE por su dirección MAC
 * - Desconexión del sensor actualmente conectado
 * - Guardado y recuperación del último dispositivo conectado
 * - Soporte para reconexión automática al último sensor usado
 * 
 * Proceso de conexión:
 * 1. Recibe la dirección MAC del dispositivo
 * 2. Establece conexión BLE con el sensor
 * 3. Descubre servicios y características disponibles
 * 4. Guarda la dirección para futuras reconexiones
 * 5. Inicia lectura automática de datos históricos (offline)
 * 6. Comienza lectura periódica de datos en tiempo real
 * 
 * @property bleRepository Repositorio BLE que gestiona las conexiones y comunicación
 * @author Felipe García Gómez
 */
class ConnectBleDeviceUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Conecta a un dispositivo BLE sensor de CamperGas.
     * 
     * Establece una conexión BLE con el sensor especificado por su dirección MAC.
     * Una vez conectado, el sensor comienza a enviar automáticamente:
     * - Datos históricos almacenados offline
     * - Mediciones en tiempo real de peso e inclinación
     * 
     * Guarda la dirección del dispositivo para permitir reconexión automática
     * la próxima vez que se abra la aplicación.
     * 
     * Esta función debe llamarse desde una coroutine o función suspend.
     * 
     * @param deviceAddress Dirección MAC del dispositivo BLE (formato: "XX:XX:XX:XX:XX:XX")
     */
    suspend operator fun invoke(deviceAddress: String) {
        // Conectamos al sensor unificado
        bleRepository.connectToSensor(deviceAddress)

        // Save address of last connected device
        bleRepository.saveLastConnectedDevice(deviceAddress)
    }

    /**
     * Desconecta del sensor BLE actualmente conectado.
     * 
     * Cierra la conexión BLE activa, detiene las lecturas periódicas de datos
     * y libera los recursos asociados. No borra la dirección del último
     * dispositivo conectado, permitiendo reconexión posterior.
     * 
     * Es seguro llamar este método aunque no haya dispositivo conectado.
     */
    fun disconnect() {
        bleRepository.disconnectSensor()
    }

    /**
     * Obtiene la dirección del último dispositivo BLE conectado.
     * 
     * Retorna un Flow que emite la dirección MAC del último sensor al que se
     * conectó la aplicación. Útil para implementar reconexión automática o
     * mostrar el dispositivo previamente usado.
     * 
     * @return Flow que emite la dirección MAC del último dispositivo conectado
     */
    fun getLastConnectedDevice(): Flow<String> {
        return bleRepository.lastConnectedDeviceAddress
    }
}
