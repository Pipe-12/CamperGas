package com.example.campergas.ui.screens.consumption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.model.Consumption
import com.example.campergas.domain.usecase.GetConsumptionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ConsumptionViewModel @Inject constructor(
    private val getConsumptionHistoryUseCase: GetConsumptionHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsumptionUiState())
    val uiState: StateFlow<ConsumptionUiState> = _uiState.asStateFlow()

    // Job para cancelar la corrutina de carga cuando sea necesario
    private var loadingJob: Job? = null

    init {
        loadConsumptionHistory()
    }

    private fun loadConsumptionHistory() {
        // Cancelar job anterior si existe
        loadingJob?.cancel()

        loadingJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentState = _uiState.value
                val startDate = currentState.startDate
                val endDate = currentState.endDate

                getConsumptionHistoryUseCase(startDate, endDate).collect { consumptions ->
                    _uiState.value = _uiState.value.copy(
                        consumptions = consumptions,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setDateRange(startDate: Long?, endDate: Long?) {
        _uiState.value = _uiState.value.copy(
            startDate = startDate,
            endDate = endDate
        )
        loadConsumptionHistory()
    }

    fun clearDateFilter() {
        _uiState.value = _uiState.value.copy(
            startDate = null,
            endDate = null
        )
        loadConsumptionHistory()
    }

    fun setLastWeekFilter() {
        setDateRangeFromCalendar(Calendar.DAY_OF_YEAR, -7)
    }

    fun setLastMonthFilter() {
        setDateRangeFromCalendar(Calendar.MONTH, -1)
    }

    /**
     * Método helper para reducir duplicación de código en filtros de fecha
     */
    private fun setDateRangeFromCalendar(calendarField: Int, amount: Int) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        calendar.add(calendarField, amount)
        val startDate = calendar.timeInMillis

        setDateRange(startDate, endDate)
    }

    override fun onCleared() {
        super.onCleared()
        loadingJob?.cancel()
    }
}

data class ConsumptionUiState(
    val consumptions: List<Consumption> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)
