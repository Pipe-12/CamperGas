package com.example.campergas.ui.screens.cylinders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.data.repository.BleRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.model.Weight
import com.example.campergas.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CylindersViewModel @Inject constructor(
    private val addGasCylinderUseCase: AddGasCylinderUseCase,
    private val getGasCylindersUseCase: GetGasCylindersUseCase,
    private val setActiveCylinderUseCase: SetActiveCylinderUseCase,
    private val gasCylinderRepository: GasCylinderRepository,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CylindersUiState())
    val uiState: StateFlow<CylindersUiState> = _uiState.asStateFlow()

    // Observar todas las bombonas
    val cylinders = getGasCylindersUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Observar la bombona activa
    val activeCylinder = getGasCylindersUseCase.getActiveCylinder().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    // Observar el peso actual del sensor BLE
    val currentWeight = bleRepository.weightData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun addCylinder(name: String, tare: Float, capacity: Float, setAsActive: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            addGasCylinderUseCase(name, tare, capacity, setAsActive)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al agregar la bombona"
                    )
                }
        }
    }
    
    fun setActiveCylinder(cylinderId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            setActiveCylinderUseCase(cylinderId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al activar la bombona"
                    )
                }
        }
    }
    
    fun deactivateCylinder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                gasCylinderRepository.deactivateAllCylinders()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al desactivar la bombona"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CylindersUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
