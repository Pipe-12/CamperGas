package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConnectBleDeviceUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    suspend operator fun invoke(deviceAddress: String) {
        // Conectamos al sensor unificado
        bleRepository.connectToSensor(deviceAddress)
        
        // Guardamos la dirección del último dispositivo conectado
        bleRepository.saveLastConnectedDevice(deviceAddress)
    }
    
    fun disconnect() {
        bleRepository.disconnectSensor()
    }
    
    fun getLastConnectedDevice(): Flow<String> {
        return bleRepository.lastConnectedDeviceAddress
    }
}
