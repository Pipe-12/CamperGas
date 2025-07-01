package com.example.campergas.data.repository

import android.Manifest
import androidx.annotation.RequiresPermission
import com.example.campergas.data.ble.BleDeviceScanner
import com.example.campergas.data.ble.BleManager
import com.example.campergas.data.ble.HistoryBleService
import com.example.campergas.data.ble.InclinationBleService
import com.example.campergas.data.ble.WeightBleService
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleRepository @Inject constructor(
    private val bleManager: BleManager,
    private val bleDeviceScanner: BleDeviceScanner,
    private val weightBleService: WeightBleService,
    private val inclinationBleService: InclinationBleService,
    private val historyBleService: HistoryBleService,
    private val preferencesDataStore: PreferencesDataStore
) {
    // Scan
    val scanResults: StateFlow<List<BleDevice>> = bleDeviceScanner.scanResults
    val isScanning: StateFlow<Boolean> = bleDeviceScanner.isScanning
    
    // Weight
    val weightData = weightBleService.weightData
    
    // Inclination
    val inclinationData = inclinationBleService.inclinationData

    
    // Preferences
    val lastConnectedDeviceAddress: Flow<String> = preferencesDataStore.lastConnectedDeviceAddress
    
    fun isBluetoothEnabled(): Boolean = bleManager.isBluetoothEnabled()
    
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() = bleDeviceScanner.startScan()
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() = bleDeviceScanner.stopScan()
    
    fun connectToWeightDevice(deviceAddress: String) = weightBleService.connect(deviceAddress)
    
    fun disconnectWeightDevice() = weightBleService.disconnect()
    
    fun connectToInclinationDevice(deviceAddress: String) = inclinationBleService.connect(deviceAddress)
    
    fun disconnectInclinationDevice() = inclinationBleService.disconnect()
    
    fun connectToHistoryDevice(deviceAddress: String) = historyBleService.connect(deviceAddress)
    
    fun disconnectHistoryDevice() = historyBleService.disconnect()
    
    suspend fun saveLastConnectedDevice(address: String) = preferencesDataStore.saveLastConnectedDevice(address)

    // Filtrado de dispositivos
    fun setCompatibleDevicesFilter(enabled: Boolean) = bleDeviceScanner.setCompatibleDevicesFilter(enabled)
    fun isCompatibleFilterEnabled(): Boolean = bleDeviceScanner.isCompatibleFilterEnabled()
}
