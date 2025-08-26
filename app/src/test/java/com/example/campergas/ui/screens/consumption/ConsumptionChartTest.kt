package com.example.campergas.ui.screens.consumption

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.domain.model.Consumption
import com.example.campergas.domain.usecase.ChartDataPoint
import com.example.campergas.domain.usecase.GetConsumptionHistoryUseCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ConsumptionChartDataTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ConsumptionViewModel
    private val getConsumptionHistoryUseCase: GetConsumptionHistoryUseCase = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // Mock summary methods
        every { getConsumptionHistoryUseCase.getLastDayConsumption() } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase.getLastWeekConsumption() } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase.getLastMonthConsumption() } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase.calculateTotalConsumption(any()) } returns 0f
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
        clearAllMocks()
    }

    @Test
    fun `chart data is always present in UI state even when empty`() = runTest {
        // Arrange
        every { getConsumptionHistoryUseCase(null, null) } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase.prepareChartData(emptyList()) } returns emptyList()

        // Act
        viewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("Chart data should always be present in UI state", state.chartData.isEmpty())
    }

    @Test
    fun `chart data is updated when date range is set to empty period`() = runTest {
        // Arrange - Setup empty responses for all date range queries
        every { getConsumptionHistoryUseCase(null, null) } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase(any(), any()) } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase.prepareChartData(emptyList()) } returns emptyList()

        viewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
        advanceUntilIdle()

        // Act - Set a date range that has no data
        val startDate = 1000L
        val endDate = 2000L
        viewModel.setDateRange(startDate, endDate)
        advanceUntilIdle()

        // Assert - Chart data should be empty but still present in state
        val state = viewModel.uiState.value
        assertTrue("Chart data should be empty for period with no data", state.chartData.isEmpty())
        assertTrue("Date range should be set", state.startDate == startDate && state.endDate == endDate)
    }

    @Test
    fun `chart data is updated when sufficient data is available`() = runTest {
        // Arrange
        val testConsumptions = listOf(
            createTestConsumption(1, 1000L),
            createTestConsumption(2, 2000L)
        )
        val testChartData = listOf(
            ChartDataPoint(1000L, 5.0f),
            ChartDataPoint(2000L, 3.0f)
        )

        every { getConsumptionHistoryUseCase(null, null) } returns flowOf(testConsumptions)
        every { getConsumptionHistoryUseCase.prepareChartData(testConsumptions) } returns testChartData

        // Act
        viewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("Chart data should contain test data", state.chartData == testChartData)
        assertTrue("Chart data should have 2 points", state.chartData.size == 2)
    }

    private fun createTestConsumption(id: Long, date: Long) = Consumption(
        id = id,
        cylinderId = id,
        cylinderName = "Test Cylinder $id",
        date = date,
        fuelKilograms = 10f,
        fuelPercentage = 75f,
        totalWeight = 15f,
        isCalibrated = true,
        isHistorical = false
    )
}