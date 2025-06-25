package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.BleDevice
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ScanBleDevicesUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke(): StateFlow<List<BleDevice>> {
        bleRepository.startScan()
        return bleRepository.scanResults
    }
    
    fun isScanning(): StateFlow<Boolean> = bleRepository.isScanning
    
    fun stopScan() {
        bleRepository.stopScan()
    }
    
    fun isBluetoothEnabled(): Boolean {
        return bleRepository.isBluetoothEnabled()
    }
}
