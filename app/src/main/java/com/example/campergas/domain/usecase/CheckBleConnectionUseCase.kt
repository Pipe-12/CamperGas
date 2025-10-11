package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * UseCase to verify BLE connection state
 */
class CheckBleConnectionUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Returns StateFlow of BLE connection state
     */
    operator fun invoke(): StateFlow<Boolean> {
        return bleRepository.connectionState
    }

    /**
     * Verifies if there is an active BLE connection at this moment
     */
    fun isConnected(): Boolean {
        return bleRepository.connectionState.value
    }
}
