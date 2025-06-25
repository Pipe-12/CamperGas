package com.example.campergas.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.VehicleConfig
import com.example.campergas.domain.model.Weight
import com.example.campergas.domain.usecase.GetVehicleConfigUseCase
import com.example.campergas.domain.usecase.GetWeightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val getWeightUseCase: GetWeightUseCase,
    private val getVehicleConfigUseCase: GetVehicleConfigUseCase
) : ViewModel() {
    
    private val _weightState = MutableStateFlow<Weight?>(null)
    val weightState: StateFlow<Weight?> = _weightState
    
    private val _vehicleState = MutableStateFlow<VehicleConfig?>(null)
    val vehicleState: StateFlow<VehicleConfig?> = _vehicleState
    
    init {
        // Obtener datos de peso en tiempo real
        viewModelScope.launch {
            getWeightUseCase().collectLatest { weight ->
                _weightState.value = weight
            }
        }
        
        // Obtener configuración del vehículo
        viewModelScope.launch {
            getVehicleConfigUseCase().collectLatest { vehicle ->
                _vehicleState.value = vehicle
            }
        }
    }
}
