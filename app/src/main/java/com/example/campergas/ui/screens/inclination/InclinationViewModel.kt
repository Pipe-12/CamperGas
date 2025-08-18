package com.example.campergas.ui.screens.inclination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.VehicleType
import com.example.campergas.domain.usecase.CheckBleConnectionUseCase
import com.example.campergas.domain.usecase.GetInclinationUseCase
import com.example.campergas.domain.usecase.GetVehicleConfigUseCase
import com.example.campergas.domain.usecase.RequestInclinationDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.tan

@HiltViewModel
class InclinationViewModel @Inject constructor(
    private val getInclinationUseCase: GetInclinationUseCase,
    private val requestInclinationDataUseCase: RequestInclinationDataUseCase,
    private val checkBleConnectionUseCase: CheckBleConnectionUseCase,
    private val getVehicleConfigUseCase: GetVehicleConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InclinationUiState())
    val uiState: StateFlow<InclinationUiState> = _uiState.asStateFlow()

    // Control de peticiones para evitar spam
    private var lastRequestTime = 0L
    private val requestCooldownMs = 2000L // 2 segundos entre peticiones

    private val _isRequestingData = MutableStateFlow(false)
    val isRequestingData: StateFlow<Boolean> = _isRequestingData

    init {
        // Cargar configuración del vehículo
        loadVehicleConfig()

        // Obtener datos de inclinación en tiempo real
        viewModelScope.launch {
            getInclinationUseCase().collectLatest { inclination ->
                _uiState.value = if (inclination != null) {
                    val newState = _uiState.value.copy(
                        inclinationPitch = inclination.pitch,
                        inclinationRoll = inclination.roll,
                        isLevel = inclination.isLevel,
                        isLoading = false,
                        error = null,
                        timestamp = inclination.timestamp
                    )
                    // Calcular elevaciones de ruedas
                    newState.copy(wheelElevations = calculateWheelElevations(newState))
                } else {
                    _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    private fun loadVehicleConfig() {
        viewModelScope.launch {
            getVehicleConfigUseCase().collectLatest { config ->
                if (config != null) {
                    _uiState.value = _uiState.value.copy(
                        vehicleType = config.type,
                        distanceBetweenRearWheels = config.distanceBetweenRearWheels,
                        distanceToFrontSupport = config.distanceToFrontSupport,
                        distanceBetweenFrontWheels = config.distanceBetweenFrontWheels ?: 0f
                    ).let { newState ->
                        // Recalcular elevaciones con la nueva configuración
                        newState.copy(wheelElevations = calculateWheelElevations(newState))
                    }
                }
            }
        }
    }

    /**
     * Calcula la elevación necesaria para cada rueda basándose en la inclinación
     */
    private fun calculateWheelElevations(state: InclinationUiState): WheelElevations {
        if (state.distanceBetweenRearWheels == 0f || state.distanceToFrontSupport == 0f) {
            return WheelElevations()
        }

        // Convertir grados a radianes
        val pitchRad = Math.toRadians(state.inclinationPitch.toDouble())
        val rollRad = Math.toRadians(state.inclinationRoll.toDouble())

        // Calcular elevaciones basándose en las distancias configuradas
        val halfRearWheelDistance = state.distanceBetweenRearWheels / 2

        // Para el roll (alabeo lateral)
        val rearLeftElevationRoll = halfRearWheelDistance * tan(rollRad)
        val rearRightElevationRoll = -halfRearWheelDistance * tan(rollRad)

        // Para el pitch (cabeceo frontal/trasero)
        val frontElevationPitch = state.distanceToFrontSupport * tan(pitchRad)

        return when (state.vehicleType) {
            VehicleType.CARAVAN -> {
                // Caravana: ruedas traseras + ruedín delantero
                WheelElevations(
                    rearLeft = rearLeftElevationRoll.toFloat(),
                    rearRight = rearRightElevationRoll.toFloat(),
                    frontSupport = frontElevationPitch.toFloat()
                )
            }

            VehicleType.AUTOCARAVANA -> {
                // Autocaravana: 4 ruedas
                val halfFrontWheelDistance = state.distanceBetweenFrontWheels / 2
                val frontLeftElevationRoll = halfFrontWheelDistance * tan(rollRad)
                val frontRightElevationRoll = -halfFrontWheelDistance * tan(rollRad)

                WheelElevations(
                    rearLeft = rearLeftElevationRoll.toFloat(),
                    rearRight = rearRightElevationRoll.toFloat(),
                    frontLeft = (frontElevationPitch + frontLeftElevationRoll).toFloat(),
                    frontRight = (frontElevationPitch + frontRightElevationRoll).toFloat()
                )
            }
        }
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
    val timestamp: Long = 0L,
    val vehicleType: VehicleType = VehicleType.CARAVAN,
    val distanceBetweenRearWheels: Float = 0f,
    val distanceToFrontSupport: Float = 0f,
    val distanceBetweenFrontWheels: Float = 0f,
    val wheelElevations: WheelElevations = WheelElevations()
)

data class WheelElevations(
    val rearLeft: Float = 0f,
    val rearRight: Float = 0f,
    val frontLeft: Float = 0f,
    val frontRight: Float = 0f,
    val frontSupport: Float = 0f // Para el ruedín delantero de la caravana
)
