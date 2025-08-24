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
        loadConsumptionSummaries()
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
        loadConsumptionSummaries()
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

    fun setLastDayFilter() {
        setDateRangeFromCalendar(Calendar.DAY_OF_YEAR, -1)
    }

    private fun loadConsumptionSummaries() {
        viewModelScope.launch {
            try {
                // Load last day consumption
                val lastDayConsumption = calculateConsumptionForPeriod(Calendar.DAY_OF_YEAR, -1)
                
                // Load last week consumption
                val lastWeekConsumption = calculateConsumptionForPeriod(Calendar.DAY_OF_YEAR, -7)
                
                // Load last month consumption
                val lastMonthConsumption = calculateConsumptionForPeriod(Calendar.MONTH, -1)
                
                // Calculate custom period consumption if dates are set
                val customPeriodConsumption = if (_uiState.value.startDate != null && _uiState.value.endDate != null) {
                    calculateConsumptionForDateRange(_uiState.value.startDate!!, _uiState.value.endDate!!)
                } else 0f
                
                _uiState.value = _uiState.value.copy(
                    lastDayConsumption = lastDayConsumption,
                    lastWeekConsumption = lastWeekConsumption,
                    lastMonthConsumption = lastMonthConsumption,
                    customPeriodConsumption = customPeriodConsumption,
                    chartData = generateChartData()
                )
            } catch (e: Exception) {
                // Handle errors silently for summaries to not interfere with main data loading
            }
        }
    }

    private suspend fun calculateConsumptionForPeriod(calendarField: Int, amount: Int): Float {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        calendar.add(calendarField, amount)
        val startDate = calendar.timeInMillis
        
        return calculateConsumptionForDateRange(startDate, endDate)
    }

    private suspend fun calculateConsumptionForDateRange(startDate: Long, endDate: Long): Float {
        return try {
            var totalConsumption = 0f
            getConsumptionHistoryUseCase(startDate, endDate).collect { consumptions ->
                // Calculate consumption by finding differences between consecutive measurements
                val sortedConsumptions = consumptions.sortedBy { it.date }
                
                if (sortedConsumptions.isNotEmpty()) {
                    // Group by cylinder to calculate consumption per cylinder
                    val groupedByCylinder = sortedConsumptions.groupBy { it.cylinderId }
                    
                    var calculatedConsumption = 0f
                    groupedByCylinder.forEach { (_, cylinderConsumptions) ->
                        val sortedCylinderConsumptions = cylinderConsumptions.sortedBy { it.date }
                        
                        // Calculate consumption as difference between first and last measurement
                        if (sortedCylinderConsumptions.size > 1) {
                            val firstMeasurement = sortedCylinderConsumptions.first()
                            val lastMeasurement = sortedCylinderConsumptions.last()
                            val consumption = firstMeasurement.fuelKilograms - lastMeasurement.fuelKilograms
                            if (consumption > 0) {
                                calculatedConsumption += consumption
                            }
                        }
                    }
                    totalConsumption = calculatedConsumption
                }
            }
            totalConsumption
        } catch (e: Exception) {
            0f
        }
    }

    private suspend fun generateChartData(): List<ChartDataPoint> {
        return try {
            val currentState = _uiState.value
            val startDate = currentState.startDate
            val endDate = currentState.endDate
            
            if (startDate != null && endDate != null) {
                val chartData = mutableListOf<ChartDataPoint>()
                
                getConsumptionHistoryUseCase(startDate, endDate).collect { consumptions ->
                    if (consumptions.isNotEmpty()) {
                        // Group by day and calculate daily consumption
                        val dailyConsumption = mutableMapOf<Long, Float>()
                        
                        // Sort consumptions by date
                        val sortedConsumptions = consumptions.sortedBy { it.date }
                        
                        // Group by day (ignoring time)
                        val groupedByDay = sortedConsumptions.groupBy { consumption ->
                            // Round down to start of day
                            val dayInMillis = 24 * 60 * 60 * 1000L
                            (consumption.date / dayInMillis) * dayInMillis
                        }
                        
                        // Calculate consumption for each day
                        groupedByDay.forEach { (dayStart, dayConsumptions) ->
                            val groupedByCylinder = dayConsumptions.groupBy { it.cylinderId }
                            var dayTotal = 0f
                            
                            groupedByCylinder.forEach { (_, cylinderConsumptions) ->
                                val sortedCylinderConsumptions = cylinderConsumptions.sortedBy { it.date }
                                if (sortedCylinderConsumptions.size > 1) {
                                    val firstMeasurement = sortedCylinderConsumptions.first()
                                    val lastMeasurement = sortedCylinderConsumptions.last()
                                    val consumption = firstMeasurement.fuelKilograms - lastMeasurement.fuelKilograms
                                    if (consumption > 0) {
                                        dayTotal += consumption
                                    }
                                }
                            }
                            
                            if (dayTotal > 0) {
                                dailyConsumption[dayStart] = dayTotal
                            }
                        }
                        
                        // Convert to chart data points
                        chartData.addAll(
                            dailyConsumption.map { (date, consumption) ->
                                ChartDataPoint(date, consumption)
                            }.sortedBy { it.date }
                        )
                    }
                }
                
                chartData
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
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
    val endDate: Long? = null,
    val lastDayConsumption: Float = 0f,
    val lastWeekConsumption: Float = 0f,
    val lastMonthConsumption: Float = 0f,
    val customPeriodConsumption: Float = 0f,
    val chartData: List<ChartDataPoint> = emptyList()
)
