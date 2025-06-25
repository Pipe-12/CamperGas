package com.example.campergas.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.example.campergas.domain.model.BleDevice
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
    
    private val bluetoothAdapter: BluetoothAdapter? get() = bleManager.bluetoothAdapter
    private var scanner: BluetoothLeScanner? = null
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: "Dispositivo desconocido"
            val deviceAddress = device.address
            val rssi = result.rssi
            
            val bleDevice = BleDevice(
                name = deviceName,
                address = deviceAddress,
                rssi = rssi
            )
            
            addDeviceToList(bleDevice)
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
            // Añadir nuevo dispositivo
            currentList.add(device)
        }
        
        _scanResults.value = currentList
    }
    
    fun startScan() {
        if (_isScanning.value) return
        
        scanner = bluetoothAdapter?.bluetoothLeScanner
        
        if (scanner != null) {
            _scanResults.value = emptyList()
            _isScanning.value = true
            scanner?.startScan(scanCallback)
        }
    }
    
    fun stopScan() {
        if (!_isScanning.value) return
        
        scanner?.stopScan(scanCallback)
        _isScanning.value = false
    }
}
