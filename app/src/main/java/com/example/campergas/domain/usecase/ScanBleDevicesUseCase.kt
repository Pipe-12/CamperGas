package com.example.campergas.domain.usecase

import android.Manifest
import androidx.annotation.RequiresPermission
import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.BleDevice
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ScanBleDevicesUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    operator fun invoke(): StateFlow<List<BleDevice>> {
        bleRepository.startScan()
        return bleRepository.scanResults
    }
    
    fun isScanning(): StateFlow<Boolean> = bleRepository.isScanning
    
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        bleRepository.stopScan()
    }
    
    fun isBluetoothEnabled(): Boolean {
        return bleRepository.isBluetoothEnabled()
    }
    
    /**
     * Verifica si se tienen todos los permisos necesarios para operaciones BLE
     */
    fun hasAllBluetoothPermissions(): Boolean {
        return bleRepository.hasAllBluetoothPermissions()
    }
    
    /**
     * Verifica si se tienen permisos para escanear dispositivos BLE
     */
    fun hasBluetoothScanPermission(): Boolean {
        return bleRepository.hasBluetoothScanPermission()
    }
    
    /**
     * Activa el filtro para mostrar solo dispositivos compatibles con CamperGas
     */
    fun enableCompatibleDevicesFilter() {
        bleRepository.setCompatibleDevicesFilter(true)
    }
    
    /**
     * Desactiva el filtro y muestra todos los dispositivos BLE
     */
    fun disableCompatibleDevicesFilter() {
        bleRepository.setCompatibleDevicesFilter(false)
    }
    
    /**
     * Verifica si el filtro está activado
     */
    fun isCompatibleFilterEnabled(): Boolean {
        return bleRepository.isCompatibleFilterEnabled()
    }
    
    /**
     * Alterna el estado del filtro
     */
    fun toggleCompatibleDevicesFilter() {
        val currentState = bleRepository.isCompatibleFilterEnabled()
        bleRepository.setCompatibleDevicesFilter(!currentState)
    }
}
