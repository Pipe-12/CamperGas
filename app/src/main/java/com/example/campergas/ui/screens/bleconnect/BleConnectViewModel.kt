package com.example.campergas.ui.screens.bleconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.BleDevice
import com.example.campergas.domain.usecase.ScanBleDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleConnectViewModel @Inject constructor(
    private val scanBleDevicesUseCase: ScanBleDevicesUseCase,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BleConnectUiState())
    val uiState: StateFlow<BleConnectUiState> = _uiState.asStateFlow()

    // Observar estado de conexión
    val connectionState = bleRepository.connectionState

    // Observar datos del sensor
    val fuelMeasurementData = bleRepository.fuelMeasurementData
    val fuelData = bleRepository.fuelData
    val inclinationData = bleRepository.inclinationData
    val historyData = bleRepository.historyData
    val isLoadingHistory = bleRepository.isLoadingHistory

    init {
        // Observar cambios en el estado de conexión
        viewModelScope.launch {
            connectionState.collect { isConnected ->
                _uiState.value = _uiState.value.copy(
                    isConnected = isConnected,
                    isConnecting = if (isConnected) null else _uiState.value.isConnecting
                )
            }
        }
    }

    fun startScan() {
        // Verificar que tenemos permisos antes de escanear
        if (!bleRepository.isBluetoothEnabled()) {
            _uiState.value = _uiState.value.copy(
                error = "Bluetooth no está habilitado"
            )
            return
        }

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
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = "Permisos de Bluetooth requeridos para escanear dispositivos"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message ?: "Error al escanear dispositivos"
                )
            }
        }
    }

    fun stopScan() {
        try {
            scanBleDevicesUseCase.stopScan()
            _uiState.value = _uiState.value.copy(isScanning = false)
        } catch (e: SecurityException) {
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                error = "Permisos de Bluetooth requeridos para detener el escaneo"
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                error = e.message ?: "Error al detener el escaneo"
            )
        }
    }

    fun connectToDevice(device: BleDevice) {
        if (!bleRepository.isBluetoothEnabled()) {
            _uiState.value = _uiState.value.copy(
                error = "Bluetooth no está habilitado"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isConnecting = device.address,
                error = null
            )
            try {
                // Usar el repositorio unificado para conectar
                bleRepository.connectToSensor(device.address)

                // Guardar dispositivo como último conectado
                bleRepository.saveLastConnectedDevice(device.address)

                _uiState.value = _uiState.value.copy(
                    connectedDevice = device,
                    error = null
                )
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    isConnecting = null,
                    error = "Permisos de Bluetooth requeridos para conectar"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConnecting = null,
                    error = e.message ?: "Error al conectar con el dispositivo"
                )
            }
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            try {
                bleRepository.disconnectSensor()
                _uiState.value = _uiState.value.copy(
                    connectedDevice = null,
                    isConnecting = null
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

    // Gestión de filtros
    fun toggleCompatibleDevicesFilter() {
        scanBleDevicesUseCase.toggleCompatibleDevicesFilter()
        _uiState.value = _uiState.value.copy(
            showOnlyCompatibleDevices = scanBleDevicesUseCase.isCompatibleFilterEnabled()
        )
    }

    fun enableCompatibleDevicesFilter() {
        scanBleDevicesUseCase.enableCompatibleDevicesFilter()
        _uiState.value = _uiState.value.copy(showOnlyCompatibleDevices = true)
    }

    fun disableCompatibleDevicesFilter() {
        scanBleDevicesUseCase.disableCompatibleDevicesFilter()
        _uiState.value = _uiState.value.copy(showOnlyCompatibleDevices = false)
    }
}

data class BleConnectUiState(
    val availableDevices: List<BleDevice> = emptyList(),
    val connectedDevice: BleDevice? = null,
    val isConnected: Boolean = false,
    val isScanning: Boolean = false,
    val isConnecting: String? = null, // MAC address del dispositivo que se está conectando
    val error: String? = null,
    val showOnlyCompatibleDevices: Boolean = false
)
