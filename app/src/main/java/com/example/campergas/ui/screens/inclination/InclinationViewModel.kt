package com.example.campergas.ui.screens.inclination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.usecase.GetInclinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InclinationViewModel @Inject constructor(
    private val getInclinationUseCase: GetInclinationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InclinationUiState())
    val uiState: StateFlow<InclinationUiState> = _uiState.asStateFlow()

    init {
        // TODO: Implementar lógica de inclinación
    }

    // TODO: Implementar métodos para gestionar inclinación
}

data class InclinationUiState(
    val inclinationX: Float = 0f,
    val inclinationY: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)
