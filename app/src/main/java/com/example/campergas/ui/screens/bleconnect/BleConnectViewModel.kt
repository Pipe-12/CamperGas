package com.example.campergas.ui.screens.bleconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.BleDevice
import com.example.campergas.domain.usecase.ScanBleDevicesUseCase
import com.example.campergas.domain.usecase.ConnectBleDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleConnectViewModel @Inject constructor(
    private val scanBleDevicesUseCase: ScanBleDevicesUseCase,
    private val connectBleDeviceUseCase: ConnectBleDeviceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BleConnectUiState())
    val uiState: StateFlow<BleConnectUiState> = _uiState.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                scanBleDevicesUseCase().collect { devices ->
                    _uiState.value = _uiState.value.copy(
                        availableDevices = devices,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message
                )
            }
        }
    }

    fun stopScan() {
        _uiState.value = _uiState.value.copy(isScanning = false)
        // TODO: Implementar lógica para detener el scan
    }

    fun connectToDevice(device: BleDevice) {
        viewModelScope.launch {
            try {
                connectBleDeviceUseCase(device.address)
                _uiState.value = _uiState.value.copy(
                    connectedDevices = _uiState.value.connectedDevices + device,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // TODO: Implementar métodos adicionales para gestión BLE
}

data class BleConnectUiState(
    val availableDevices: List<BleDevice> = emptyList(),
    val connectedDevices: List<BleDevice> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
)
