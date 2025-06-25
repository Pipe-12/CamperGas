package com.example.campergas.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.Weight
import com.example.campergas.domain.usecase.ConnectBleDeviceUseCase
import com.example.campergas.domain.usecase.GetWeightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWeightUseCase: GetWeightUseCase,
    private val connectBleDeviceUseCase: ConnectBleDeviceUseCase
) : ViewModel() {
    
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState
    
    private val _weight = MutableStateFlow<Weight?>(null)
    val weight: StateFlow<Weight?> = _weight
    
    init {
        // Intentar conectar con el último dispositivo utilizado
        viewModelScope.launch {
            connectBleDeviceUseCase.getLastConnectedDevice().collectLatest { lastDeviceAddress ->
                if (lastDeviceAddress.isNotEmpty()) {
                    try {
                        connectBleDeviceUseCase.invoke(lastDeviceAddress)
                        _connectionState.value = true
                    } catch (e: Exception) {
                        _connectionState.value = false
                    }
                }
            }
        }
        
        // Observar los datos de peso
        viewModelScope.launch {
            getWeightUseCase().collectLatest {
                _weight.value = it
            }
        }
    }
    
    fun disconnectDevice() {
        viewModelScope.launch {
            try {
                connectBleDeviceUseCase.disconnect()
                _connectionState.value = false
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnectDevice()
    }
}
