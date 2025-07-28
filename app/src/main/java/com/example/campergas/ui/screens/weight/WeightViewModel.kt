package com.example.campergas.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.model.VehicleConfig
import com.example.campergas.domain.usecase.CheckBleConnectionUseCase
import com.example.campergas.domain.usecase.GetActiveCylinderUseCase
import com.example.campergas.domain.usecase.GetFuelDataUseCase
import com.example.campergas.domain.usecase.GetVehicleConfigUseCase
import com.example.campergas.domain.usecase.RequestWeightDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val getFuelDataUseCase: GetFuelDataUseCase,
    private val getVehicleConfigUseCase: GetVehicleConfigUseCase,
    private val getActiveCylinderUseCase: GetActiveCylinderUseCase,
    private val requestWeightDataUseCase: RequestWeightDataUseCase,
    private val checkBleConnectionUseCase: CheckBleConnectionUseCase
) : ViewModel() {

    private val _fuelState = MutableStateFlow<FuelMeasurement?>(null)
    val fuelState: StateFlow<FuelMeasurement?> = _fuelState

    private val _vehicleState = MutableStateFlow<VehicleConfig?>(null)
    val vehicleState: StateFlow<VehicleConfig?> = _vehicleState

    private val _activeCylinder = MutableStateFlow<GasCylinder?>(null)
    val activeCylinder: StateFlow<GasCylinder?> = _activeCylinder

    // Control de peticiones para evitar spam
    private var lastRequestTime = 0L
    private val requestCooldownMs = 2000L // 2 segundos entre peticiones

    private val _isRequestingData = MutableStateFlow(false)
    val isRequestingData: StateFlow<Boolean> = _isRequestingData

    init {
        // Obtener configuración del vehículo
        viewModelScope.launch {
            getVehicleConfigUseCase().collectLatest { vehicle ->
                _vehicleState.value = vehicle
            }
        }

        // Obtener bombona activa
        viewModelScope.launch {
            getActiveCylinderUseCase().collectLatest { cylinder ->
                _activeCylinder.value = cylinder
            }
        }

        // Obtener datos de combustible
        viewModelScope.launch {
            getFuelDataUseCase().collectLatest { fuel ->
                _fuelState.value = fuel
            }
        }
    }

    /**
     * Solicita una lectura manual de datos de peso del sensor BLE
     * Incluye protección contra múltiples peticiones seguidas
     */
    fun requestWeightDataManually() {
        val currentTime = System.currentTimeMillis()
        
        // Verificar si ha pasado suficiente tiempo desde la última petición
        if (currentTime - lastRequestTime < requestCooldownMs) {
            android.util.Log.d("WeightViewModel", "⏱️ Petición bloqueada - cooldown activo")
            return
        }
        
        // Verificar si ya hay una petición en curso
        if (_isRequestingData.value) {
            android.util.Log.d("WeightViewModel", "⏱️ Petición bloqueada - ya hay una en curso")
            return
        }
        
        android.util.Log.d("WeightViewModel", "📊 Solicitando datos de peso manualmente")
        _isRequestingData.value = true
        lastRequestTime = currentTime
        
        requestWeightDataUseCase()
        
        // Resetear el estado después de un tiempo razonable
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // 1.5 segundos
            _isRequestingData.value = false
        }
    }

    /**
     * Verifica si hay una conexión BLE activa
     */
    fun isConnected(): Boolean {
        return checkBleConnectionUseCase.isConnected()
    }

    /**
     * Verifica si se puede hacer una nueva petición (no está en cooldown)
     */
    fun canMakeRequest(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastRequestTime >= requestCooldownMs) && !_isRequestingData.value
    }
}
