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
}
