package com.example.campergas.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.model.VehicleConfig
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.usecase.GetActiveCylinderUseCase
import com.example.campergas.domain.usecase.GetVehicleConfigUseCase
import com.example.campergas.domain.usecase.GetFuelDataUseCase
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
    private val getActiveCylinderUseCase: GetActiveCylinderUseCase
) : ViewModel() {
    
    private val _fuelState = MutableStateFlow<FuelMeasurement?>(null)
    val fuelState: StateFlow<FuelMeasurement?> = _fuelState
    
    private val _vehicleState = MutableStateFlow<VehicleConfig?>(null)
    val vehicleState: StateFlow<VehicleConfig?> = _vehicleState
    
    private val _activeCylinder = MutableStateFlow<GasCylinder?>(null)
    val activeCylinder: StateFlow<GasCylinder?> = _activeCylinder
    
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
}
