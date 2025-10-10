package com.example.campergas.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.model.VehicleConfig
import com.example.campergas.domain.usecase.ConnectBleDeviceUseCase
import com.example.campergas.domain.usecase.GetConsumptionHistoryUseCase
import com.example.campergas.domain.usecase.GetFuelDataUseCase
import com.example.campergas.domain.usecase.GetInclinationUseCase
import com.example.campergas.domain.usecase.GetVehicleConfigUseCase
import com.example.campergas.domain.usecase.ReadSensorDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFuelDataUseCase: GetFuelDataUseCase,
    private val connectBleDeviceUseCase: ConnectBleDeviceUseCase,
    private val readSensorDataUseCase: ReadSensorDataUseCase,
    private val getVehicleConfigUseCase: GetVehicleConfigUseCase,
    private val getConsumptionHistoryUseCase: GetConsumptionHistoryUseCase,
    private val getInclinationUseCase: GetInclinationUseCase
) : ViewModel() {

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    private val _fuelData = MutableStateFlow<FuelMeasurement?>(null)
    val fuelData: StateFlow<FuelMeasurement?> = _fuelData

    private val _vehicleConfig = MutableStateFlow<VehicleConfig?>(null)
    val vehicleConfig: StateFlow<VehicleConfig?> = _vehicleConfig

    private val _lastDayConsumption = MutableStateFlow(0f)
    val lastDayConsumption: StateFlow<Float> = _lastDayConsumption

    private val _lastWeekConsumption = MutableStateFlow(0f)
    val lastWeekConsumption: StateFlow<Float> = _lastWeekConsumption

    private val _inclinationPitch = MutableStateFlow(0f)
    val inclinationPitch: StateFlow<Float> = _inclinationPitch

    private val _inclinationRoll = MutableStateFlow(0f)
    val inclinationRoll: StateFlow<Float> = _inclinationRoll

    init {
        // Observar el state of conexión from ReadSensorDataUseCase
        viewModelScope.launch {
            readSensorDataUseCase.getConnectionState().collectLatest { isConnected ->
                _connectionState.value = isConnected
            }
        }

        // Intentar conectar with the último device utilizado
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

        // Observar los data de combustible
        viewModelScope.launch {
            getFuelDataUseCase().collectLatest {
                _fuelData.value = it
            }
        }

        // Observar configuración del vehículo
        viewModelScope.launch {
            getVehicleConfigUseCase().collectLatest { config ->
                _vehicleConfig.value = config
            }
        }

        // Observar data of inclination
        viewModelScope.launch {
            getInclinationUseCase().collectLatest { inclination ->
                inclination?.let {
                    _inclinationPitch.value = it.pitch
                    _inclinationRoll.value = it.roll
                }
            }
        }

        // Loadr data de consumption
        loadConsumptionSummaries()
    }

    private fun loadConsumptionSummaries() {
        viewModelScope.launch {
            try {
                getConsumptionHistoryUseCase.getLastDayConsumption().collectLatest { dayConsumptions ->
                    val dayTotal = getConsumptionHistoryUseCase.calculateTotalConsumption(dayConsumptions)
                    _lastDayConsumption.value = dayTotal
                }
            } catch (e: Exception) {
                // Silently handle summary loading errors
            }
        }
        
        viewModelScope.launch {
            try {
                getConsumptionHistoryUseCase.getLastWeekConsumption().collectLatest { weekConsumptions ->
                    val weekTotal = getConsumptionHistoryUseCase.calculateTotalConsumption(weekConsumptions)
                    _lastWeekConsumption.value = weekTotal
                }
            } catch (e: Exception) {
                // Silently handle summary loading errors
            }
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            try {
                connectBleDeviceUseCase.disconnect()
                _connectionState.value = false
            } catch (_: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Solicita una lectura única de todos los data from sensor
     * Se llama cada vez que se abre la screen Home
     */
    fun requestSensorDataOnScreenOpen() {
        viewModelScope.launch {
            // Esperar un poco for que la UI se establezca
            kotlinx.coroutines.delay(500)

            // Solo hacer la petición si hay conexión activa
            if (_connectionState.value) {
                try {
                    readSensorDataUseCase.readAllSensorData()
                } catch (_: Exception) {
                    // Handle error silenciosamente for no afectar la UI
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectDevice()
    }
}
