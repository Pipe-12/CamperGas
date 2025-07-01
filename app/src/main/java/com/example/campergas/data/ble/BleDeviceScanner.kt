package com.example.campergas.data.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import com.example.campergas.domain.model.BleDevice
import com.example.campergas.domain.model.CamperGasUuids
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class BleDeviceScanner @Inject constructor(
    private val bleManager: BleManager
) {
    private val _scanResults = MutableStateFlow<List<BleDevice>>(emptyList())
    val scanResults: StateFlow<List<BleDevice>> = _scanResults
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    
    // Filtro para mostrar solo dispositivos compatibles
    private var showOnlyCompatibleDevices = false
    
    private val bluetoothAdapter: BluetoothAdapter? get() = bleManager.bluetoothAdapter
    private var scanner: BluetoothLeScanner? = null
    
    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: "Dispositivo desconocido"
            val deviceAddress = device.address
            val rssi = result.rssi
            
            // Obtener servicios de los datos del scan
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
        
        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
            // Podrías emitir un error aquí si necesitas manejar fallos de scan
        }
    }
    
    private fun addDeviceToList(device: BleDevice) {
        val currentList = _scanResults.value.toMutableList()
        
        // Comprueba si el dispositivo ya está en la lista
        val existingIndex = currentList.indexOfFirst { it.address == device.address }
        
        if (existingIndex >= 0) {
            // Actualizar dispositivo existente
            currentList[existingIndex] = device
        } else {
            // Añadir nuevo dispositivo solo si pasa el filtro o si el filtro está desactivado
            if (!showOnlyCompatibleDevices || device.isCompatibleWithCamperGas) {
                currentList.add(device)
            }
        }
        
        // Aplicar filtro a toda la lista si está activado
        val filteredList = if (showOnlyCompatibleDevices) {
            currentList.filter { it.isCompatibleWithCamperGas }
        } else {
            currentList
        }
        
        _scanResults.value = filteredList
    }
    
    /**
     * Establece el filtro para mostrar solo dispositivos compatibles con CamperGas
     */
    fun setCompatibleDevicesFilter(enabled: Boolean) {
        showOnlyCompatibleDevices = enabled
        updateFilteredResults()
    }
    
    /**
     * Obtiene el estado current del filtro
     */
    fun isCompatibleFilterEnabled(): Boolean = showOnlyCompatibleDevices
    
    /**
     * Actualiza los resultados aplicando el filtro si está activado
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
    
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        if (!_isScanning.value) return
        
        scanner?.stopScan(scanCallback)
        _isScanning.value = false
    }
}
