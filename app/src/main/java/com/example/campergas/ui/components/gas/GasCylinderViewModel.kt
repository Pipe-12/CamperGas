package com.example.campergas.ui.components.gas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.usecase.AddGasCylinderUseCase
import com.example.campergas.domain.usecase.GetActiveCylinderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GasCylinderUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class GasCylinderViewModel @Inject constructor(
    private val addGasCylinderUseCase: AddGasCylinderUseCase,
    private val getActiveCylinderUseCase: GetActiveCylinderUseCase
) : ViewModel() {

    private val _activeCylinder = MutableStateFlow<GasCylinder?>(null)
    val activeCylinder: StateFlow<GasCylinder?> = _activeCylinder

    private val _uiState = MutableStateFlow(GasCylinderUiState())
    val uiState: StateFlow<GasCylinderUiState> = _uiState

    init {
        // Observar la bombona activa
        viewModelScope.launch {
            getActiveCylinderUseCase().collectLatest { cylinder ->
                _activeCylinder.value = cylinder
                // Actualizar mensaje si no hay bombona activa
                if (cylinder == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Sin bombona activa - Las mediciones no se guardarán"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                }
            }
        }
    }

    fun addCylinder(name: String, tare: Float, capacity: Float, setAsActive: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = addGasCylinderUseCase(name, tare, capacity, setAsActive)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Bombona añadida correctamente"
                    )
                    // Limpiar mensaje después de unos segundos
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(successMessage = null)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Error al añadir bombona"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

}
