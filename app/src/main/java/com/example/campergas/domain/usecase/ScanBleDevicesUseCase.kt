package com.example.campergas.domain.usecase

import android.Manifest
import androidx.annotation.RequiresPermission
import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.BleDevice
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Caso de uso para escanear dispositivos Bluetooth Low Energy (BLE) disponibles.
 *
 * Este caso de uso encapsula la lógica de negocio para el escaneo de dispositivos BLE
 * cercanos, incluyendo:
 * - Inicio y detención del escaneo
 * - Filtrado de dispositivos compatibles con CamperGas
 * - Verificación del estado de Bluetooth
 * - Acceso a los resultados del escaneo en tiempo real
 *
 * Requisitos de permisos:
 * - Android 12+: BLUETOOTH_SCAN
 * - Android 11 y anteriores: BLUETOOTH_ADMIN + ACCESS_FINE_LOCATION
 *
 * Funcionalidades del filtro:
 * - Habilitado: Solo muestra dispositivos con servicios CamperGas
 * - Deshabilitado: Muestra todos los dispositivos BLE encontrados
 *
 * Casos de uso principales:
 * - Buscar sensores CamperGas cercanos para conectar
 * - Descubrir dispositivos BLE en el entorno
 * - Filtrar dispositivos no compatibles para simplificar la UI
 *
 * @property bleRepository Repositorio BLE que gestiona el escaneo y conexión
 * @author Felipe García Gómez
 */
class ScanBleDevicesUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Inicia el escaneo de dispositivos BLE y retorna el estado de resultados.
     *
     * Comienza a buscar dispositivos BLE cercanos y devuelve un StateFlow que
     * emite la lista de dispositivos encontrados. La lista se actualiza
     * automáticamente conforme se descubren nuevos dispositivos o se actualiza
     * información de dispositivos ya encontrados.
     *
     * Requiere permisos de escaneo BLE según la versión de Android.
     *
     * @return StateFlow que emite la lista actualizada de dispositivos encontrados
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    operator fun invoke(): StateFlow<List<BleDevice>> {
        bleRepository.startScan()
        return bleRepository.scanResults
    }

    /**
     * Detiene el escaneo de dispositivos BLE.
     *
     * Cancela el escaneo activo para ahorrar batería. Los dispositivos ya
     * encontrados permanecen en la lista de resultados.
     *
     * Requiere permisos de escaneo BLE según la versión de Android.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        bleRepository.stopScan()
    }

    /**
     * Verifica si el Bluetooth está habilitado en el dispositivo.
     *
     * @return true si el Bluetooth está encendido, false si está apagado o no disponible
     */
    fun isBluetoothEnabled(): Boolean {
        return bleRepository.isBluetoothEnabled()
    }

    /**
     * Verifica si el filtro de compatibilidad está actualmente activo.
     *
     * @return true si solo se muestran dispositivos compatibles, false si se muestran todos
     */
    fun isCompatibleFilterEnabled(): Boolean {
        return bleRepository.isCompatibleFilterEnabled()
    }

    /**
     * Alterna el estado del filtro de compatibilidad.
     *
     * Si el filtro está activo, lo desactiva. Si está inactivo, lo activa.
     * Proporciona una forma conveniente de cambiar entre modos de visualización.
     */
    fun toggleCompatibleDevicesFilter() {
        val currentState = bleRepository.isCompatibleFilterEnabled()
        bleRepository.setCompatibleDevicesFilter(!currentState)
    }
}
