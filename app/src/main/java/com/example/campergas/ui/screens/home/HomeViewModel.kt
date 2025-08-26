package com.example.campergas.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.usecase.CheckBleConnectionUseCase
import com.example.campergas.domain.usecase.ConnectBleDeviceUseCase
import com.example.campergas.domain.usecase.GetFuelDataUseCase
import com.example.campergas.service.BleForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getFuelDataUseCase: GetFuelDataUseCase,
    private val connectBleDeviceUseCase: ConnectBleDeviceUseCase,
    private val checkBleConnectionUseCase: CheckBleConnectionUseCase
) : ViewModel() {

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    private val _fuelData = MutableStateFlow<FuelMeasurement?>(null)
    val fuelData: StateFlow<FuelMeasurement?> = _fuelData

    init {
        // Observar el estado de conexión desde CheckBleConnectionUseCase
        viewModelScope.launch {
            checkBleConnectionUseCase().collectLatest { isConnected ->
                _connectionState.value = isConnected
            }
        }

        // Intentar conectar con el último dispositivo utilizado
        viewModelScope.launch {
            connectBleDeviceUseCase.getLastConnectedDevice().collectLatest { lastDeviceAddress ->
                if (lastDeviceAddress.isNotEmpty() && !_connectionState.value) {
                    try {
                        connectBleDeviceUseCase.invoke(lastDeviceAddress)
                    } catch (_: Exception) {
                        _connectionState.value = false
                    }
                }
            }
        }

        // Observar los datos de combustible
        viewModelScope.launch {
            getFuelDataUseCase().collectLatest {
                _fuelData.value = it
            }
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            try {
                connectBleDeviceUseCase.disconnect()
                _connectionState.value = false
            } catch (_: Exception) {
                // Manejar error
            }
        }
    }

    /**
     * Solicita datos frescos del sensor a través del servicio centralizado
     * Se llama cada vez que se abre la pantalla Home
     */
    fun requestSensorDataOnScreenOpen() {
        viewModelScope.launch {
            // Esperar un poco para que la UI se establezca
            kotlinx.coroutines.delay(500)

            // Solo hacer la petición si hay conexión activa
            if (_connectionState.value) {
                try {
                    // Solicitar datos frescos a través del servicio centralizado
                    // que maneja deduplicación automáticamente
                    BleForegroundService.requestFreshSensorData(context)
                } catch (_: Exception) {
                    // Manejar error silenciosamente para no afectar la UI
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectDevice()
    }
}
