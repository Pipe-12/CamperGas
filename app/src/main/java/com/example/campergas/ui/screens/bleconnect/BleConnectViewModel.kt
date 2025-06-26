package com.example.campergas.ui.screens.bleconnect

import android.annotation.SuppressLint
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

    @SuppressLint("MissingPermission")
    fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = true,
                error = null
            )
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
                    error = e.message ?: "Error al escanear dispositivos"
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            scanBleDevicesUseCase.stopScan()
            _uiState.value = _uiState.value.copy(isScanning = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                error = e.message ?: "Error al detener el escaneo"
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BleDevice) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isConnecting = device.address,
                error = null
            )
            try {
                connectBleDeviceUseCase(device.address)
                val updatedDevice = device.copy(isConnected = true)
                _uiState.value = _uiState.value.copy(
                    connectedDevices = _uiState.value.connectedDevices + updatedDevice,
                    isConnecting = null,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConnecting = null,
                    error = e.message ?: "Error al conectar con el dispositivo"
                )
            }
        }
    }
    
    fun disconnectDevice(device: BleDevice) {
        viewModelScope.launch {
            try {
                // TODO: Implementar desconexión específica
                _uiState.value = _uiState.value.copy(
                    connectedDevices = _uiState.value.connectedDevices.filter { it.address != device.address }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al desconectar dispositivo"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun checkBluetoothPermissions(): Boolean {
        return scanBleDevicesUseCase.isBluetoothEnabled()
    }
    
    fun isBluetoothEnabled(): Boolean {
        return scanBleDevicesUseCase.isBluetoothEnabled()
    }
    
    fun requiresPermissions(): Boolean {
        return !scanBleDevicesUseCase.isBluetoothEnabled()
    }
}

data class BleConnectUiState(
    val availableDevices: List<BleDevice> = emptyList(),
    val connectedDevices: List<BleDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isConnecting: String? = null, // MAC address del dispositivo que se está conectando
    val error: String? = null
)
