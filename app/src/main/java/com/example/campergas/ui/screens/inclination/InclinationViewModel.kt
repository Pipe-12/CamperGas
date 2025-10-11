package com.example.campergas.ui.screens.inclination

import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.VehicleType
import com.example.campergas.domain.usecase.CheckBleConnectionUseCase
import com.example.campergas.domain.usecase.GetInclinationUseCase
import com.example.campergas.domain.usecase.GetVehicleConfigUseCase
import com.example.campergas.domain.usecase.RequestInclinationDataUseCase
import com.example.campergas.ui.base.BaseRequestViewModel
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
    checkBleConnectionUseCase: CheckBleConnectionUseCase,
    private val getVehicleConfigUseCase: GetVehicleConfigUseCase
) : BaseRequestViewModel(checkBleConnectionUseCase) {

    private val _uiState = MutableStateFlow(InclinationUiState())
    val uiState: StateFlow<InclinationUiState> = _uiState.asStateFlow()

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
        val rearWheelDistance = state.distanceBetweenRearWheels 

        // Para el roll (alabeo lateral)
        val rearLeftElevationRoll = rearWheelDistance * tan(rollRad)
        val rearRightElevationRoll = -rearWheelDistance * tan(rollRad)

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
                val frontWheelDistance = state.distanceBetweenFrontWheels
                val frontLeftElevationRoll = frontWheelDistance * tan(rollRad)
                val frontRightElevationRoll = -frontWheelDistance * tan(rollRad)

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
        executeManualRequest(
            requestAction = { requestInclinationDataUseCase() },
            logTag = "InclinationViewModel",
            dataTypeDescription = "inclinación"
        )
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
    val distanceBetweenRearWheels: Float = 250f, // 2.5 metros en cm
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
