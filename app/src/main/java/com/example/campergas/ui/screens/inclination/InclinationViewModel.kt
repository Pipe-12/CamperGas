package com.example.campergas.ui.screens.inclination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.usecase.GetInclinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class InclinationViewModel @Inject constructor(
    private val getInclinationUseCase: GetInclinationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InclinationUiState())
    val uiState: StateFlow<InclinationUiState> = _uiState.asStateFlow()

    init {
        // Obtener datos de inclinación en tiempo real
        viewModelScope.launch {
            getInclinationUseCase().collectLatest { inclination ->
                _uiState.value = if (inclination != null) {
                    _uiState.value.copy(
                        inclinationPitch = inclination.pitch,
                        inclinationRoll = inclination.roll,
                        isLevel = inclination.isLevel,
                        isLoading = false,
                        error = null,
                        timestamp = inclination.timestamp
                    )
                } else {
                    _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    /**
     * Determina si el vehículo está nivelado basándose en los valores de inclinación pitch y roll
     */
    private fun isVehicleLevel(pitch: Float, roll: Float): Boolean {
        val levelThreshold = 2.0f // 2 grados de tolerancia
        return abs(pitch) <= levelThreshold && abs(roll) <= levelThreshold
    }
}

data class InclinationUiState(
    val inclinationPitch: Float = 0f, // Cabeceo (adelante/atrás)
    val inclinationRoll: Float = 0f,  // Alabeo (lado a lado)
    val isLevel: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val timestamp: Long = 0L
)
