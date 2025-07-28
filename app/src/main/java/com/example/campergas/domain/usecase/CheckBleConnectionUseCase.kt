package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * UseCase para verificar el estado de conexión BLE
 */
class CheckBleConnectionUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Retorna el StateFlow del estado de conexión BLE
     */
    operator fun invoke(): StateFlow<Boolean> {
        return bleRepository.connectionState
    }
    
    /**
     * Verifica si hay una conexión BLE activa en este momento
     */
    fun isConnected(): Boolean {
        return bleRepository.connectionState.value
    }
}
