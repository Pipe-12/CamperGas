package com.example.campergas.ui.screens.inclination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.usecase.CheckBleConnectionUseCase
import com.example.campergas.domain.usecase.GetInclinationUseCase
import com.example.campergas.domain.usecase.RequestInclinationDataUseCase
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
    private val getInclinationUseCase: GetInclinationUseCase,
    private val requestInclinationDataUseCase: RequestInclinationDataUseCase,
    private val checkBleConnectionUseCase: CheckBleConnectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InclinationUiState())
    val uiState: StateFlow<InclinationUiState> = _uiState.asStateFlow()

    // Control de peticiones para evitar spam
    private var lastRequestTime = 0L
    private val requestCooldownMs = 2000L // 2 segundos entre peticiones

    private val _isRequestingData = MutableStateFlow(false)
    val isRequestingData: StateFlow<Boolean> = _isRequestingData

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

    /**
     * Solicita una lectura manual de datos de inclinación del sensor BLE
     * Incluye protección contra múltiples peticiones seguidas
     */
    fun requestInclinationDataManually() {
        val currentTime = System.currentTimeMillis()

        // Verificar si ha pasado suficiente tiempo desde la última petición
        if (currentTime - lastRequestTime < requestCooldownMs) {
            android.util.Log.d("InclinationViewModel", "⏱️ Petición bloqueada - cooldown activo")
            return
        }

        // Verificar si ya hay una petición en curso
        if (_isRequestingData.value) {
            android.util.Log.d(
                "InclinationViewModel",
                "⏱️ Petición bloqueada - ya hay una en curso"
            )
            return
        }

        android.util.Log.d("InclinationViewModel", "📊 Solicitando datos de inclinación manualmente")
        _isRequestingData.value = true
        lastRequestTime = currentTime

        requestInclinationDataUseCase()

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

data class InclinationUiState(
    val inclinationPitch: Float = 0f, // Cabeceo (adelante/atrás)
    val inclinationRoll: Float = 0f,  // Alabeo (lado a lado)
    val isLevel: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val timestamp: Long = 0L
)
