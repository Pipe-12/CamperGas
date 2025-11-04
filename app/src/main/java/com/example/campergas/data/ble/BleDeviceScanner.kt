package com.example.campergas.data.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import com.example.campergas.domain.model.BleDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Escáner de dispositivos Bluetooth Low Energy (BLE).
 *
 * Esta clase gestiona el escaneo de dispositivos BLE disponibles en el entorno,
 * permitiendo descubrir sensores compatibles con CamperGas. Proporciona funcionalidad
 * para iniciar y detener el escaneo, así como filtrar dispositivos compatibles.
 *
 * Características principales:
 * - Escaneo continuo de dispositivos BLE cercanos
 * - Actualización en tiempo real de la lista de dispositivos encontrados
 * - Filtrado opcional de dispositivos compatibles con CamperGas
 * - Detección de información RSSI (intensidad de señal) y servicios disponibles
 * - Gestión automática de duplicados actualizando dispositivos ya encontrados
 *
 * @property bleManager Gestor de Bluetooth que proporciona acceso al adaptador BLE
 * @author Felipe García Gómez
 */
class BleDeviceScanner @Inject constructor(
    private val bleManager: BleManager
) {
    /**
     * Lista de dispositivos BLE encontrados durante el escaneo.
     *
     * Se actualiza automáticamente cuando se encuentran nuevos dispositivos
     * o cuando se actualiza la información de dispositivos ya encontrados.
     * Los dispositivos se filtran según el estado del filtro de compatibilidad.
     */
    private val _scanResults = MutableStateFlow<List<BleDevice>>(emptyList())
    val scanResults: StateFlow<List<BleDevice>> = _scanResults

    /**
     * Estado actual del escaneo.
     *
     * Indica si el escáner está actualmente buscando dispositivos BLE (true)
     * o si el escaneo está detenido (false).
     */
    private val _isScanning = MutableStateFlow(false)

    /**
     * Filtro para mostrar solo dispositivos compatibles con CamperGas.
     *
     * Cuando está activo (true), solo se incluyen en los resultados aquellos
     * dispositivos que anuncian los servicios UUID específicos de CamperGas.
     */
    private var showOnlyCompatibleDevices = false

    /**
     * Adaptador de Bluetooth obtenido del gestor BLE.
     *
     * Proporciona acceso a las funcionalidades de Bluetooth del dispositivo.
     */
    private val bluetoothAdapter: BluetoothAdapter? get() = bleManager.bluetoothAdapter

    /**
     * Escáner BLE específico obtenido del adaptador de Bluetooth.
     *
     * Se utiliza para iniciar y detener operaciones de escaneo BLE.
     */
    private var scanner: BluetoothLeScanner? = null

    /**
     * Callback que procesa los resultados del escaneo BLE.
     *
     * Este callback se invoca cada vez que se encuentra un dispositivo BLE durante
     * el escaneo. Extrae la información del dispositivo (nombre, dirección, RSSI, servicios)
     * y lo añade o actualiza en la lista de resultados.
     */
    private val scanCallback = object : ScanCallback() {
        /**
         * Llamado cuando se encuentra un resultado de escaneo BLE.
         *
         * Procesa el dispositivo encontrado, extrae su información y lo añade
         * a la lista de resultados si pasa los filtros activos.
         *
         * @param callbackType Tipo de callback (match encontrado, perdido, o actualización)
         * @param result Resultado del escaneo conteniendo información del dispositivo
         */
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: "Unknown device"
            val deviceAddress = device.address
            val rssi = result.rssi

            // Get services from scan data
            val services = result.scanRecord?.serviceUuids?.map { it.toString() } ?: emptyList()

            val bleDevice = BleDevice(
                name = deviceName,
                address = deviceAddress,
                rssi = rssi,
                services = services,
                isConnectable = result.isConnectable
            )

            addDeviceToList(bleDevice)
        }

        /**
         * Llamado cuando falla el escaneo BLE.
         *
         * Detiene el estado de escaneo y registra el código de error.
         *
         * @param errorCode Código de error que indica por qué falló el escaneo
         */
        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
            // Could emit error here if you need to handle scan failures
        }
    }

    /**
     * Añade o actualiza un dispositivo en la lista de resultados.
     *
     * Si el dispositivo ya existe en la lista (mismo dirección MAC), se actualiza
     * su información. Si es nuevo, se añade solo si pasa el filtro de compatibilidad
     * (si está activo). Después de la operación, aplica el filtro a toda la lista.
     *
     * @param device Dispositivo BLE a añadir o actualizar
     */
    private fun addDeviceToList(device: BleDevice) {
        val currentList = _scanResults.value.toMutableList()

        // Check if device is already in the list
        val existingIndex = currentList.indexOfFirst { it.address == device.address }

        if (existingIndex >= 0) {
            // Updatesr device existente
            currentList[existingIndex] = device
        } else {
            // Add new device only if it passes filter or if filter is disabled
            if (!showOnlyCompatibleDevices || device.isCompatibleWithCamperGas) {
                currentList.add(device)
            }
        }

        // Apply filter to entire list if enabled
        val filteredList = if (showOnlyCompatibleDevices) {
            currentList.filter { it.isCompatibleWithCamperGas }
        } else {
            currentList
        }

        _scanResults.value = filteredList
    }

    /**
     * Establece el filtro para mostrar solo dispositivos compatibles con CamperGas.
     *
     * Cuando se habilita, solo se muestran dispositivos que anuncian los servicios
     * UUID específicos de CamperGas. Al cambiar el filtro, se reaplica automáticamente
     * a todos los dispositivos ya encontrados.
     *
     * @param enabled true para activar el filtro, false para mostrar todos los dispositivos
     */
    fun setCompatibleDevicesFilter(enabled: Boolean) {
        showOnlyCompatibleDevices = enabled
        updateFilteredResults()
    }

    /**
     * Obtiene el estado actual del filtro de compatibilidad.
     *
     * @return true si el filtro está activo, false si está desactivado
     */
    fun isCompatibleFilterEnabled(): Boolean = showOnlyCompatibleDevices

    /**
     * Actualiza los resultados aplicando el filtro si está habilitado.
     *
     * Recorre la lista de dispositivos encontrados y filtra aquellos que son
     * compatibles con CamperGas si el filtro está activo. Si está desactivado,
     * muestra todos los dispositivos.
     */
    private fun updateFilteredResults() {
        val allDevices = _scanResults.value
        val filteredDevices = if (showOnlyCompatibleDevices) {
            allDevices.filter { it.isCompatibleWithCamperGas }
        } else {
            allDevices
        }
        _scanResults.value = filteredDevices
    }

    /**
     * Inicia el escaneo de dispositivos BLE.
     *
     * Obtiene el escáner BLE del adaptador de Bluetooth y comienza a buscar
     * dispositivos cercanos. Limpia la lista de resultados previos y marca el
     * escaneo como activo. Si ya hay un escaneo en curso, no hace nada.
     *
     * Requiere el permiso BLUETOOTH_SCAN en Android 12+ o BLUETOOTH_ADMIN
     * en versiones anteriores.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        if (_isScanning.value) return

        scanner = bluetoothAdapter?.bluetoothLeScanner

        if (scanner != null) {
            _scanResults.value = emptyList()
            _isScanning.value = true
            scanner?.startScan(scanCallback)
        }
    }

    /**
     * Detiene el escaneo de dispositivos BLE.
     *
     * Cancela el escaneo en curso y marca el estado como no escaneando.
     * Si no hay escaneo activo, no hace nada.
     *
     * Requiere el permiso BLUETOOTH_SCAN en Android 12+ o BLUETOOTH_ADMIN
     * en versiones anteriores.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        if (!_isScanning.value) return

        scanner?.stopScan(scanCallback)
        _isScanning.value = false
    }
}
