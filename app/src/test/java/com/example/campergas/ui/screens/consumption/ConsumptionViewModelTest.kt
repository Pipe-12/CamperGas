package com.example.campergas.ui.screens.consumption

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.domain.model.Consumption
import com.example.campergas.domain.usecase.GetConsumptionHistoryUseCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ConsumptionViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ConsumptionViewModel
    private val getConsumptionHistoryUseCase: GetConsumptionHistoryUseCase = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val consumptionsFlow = MutableStateFlow<List<Consumption>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // Por defecto, cuando se llama al use case sin fechas, devuelve todas las consumptions
        every { getConsumptionHistoryUseCase(null, null) } returns consumptionsFlow

        // Mock the new summary methods
        every { getConsumptionHistoryUseCase.getLastDayConsumption() } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase.getLastWeekConsumption() } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase.getLastMonthConsumption() } returns flowOf(emptyList())
        every { getConsumptionHistoryUseCase.calculateTotalConsumption(any()) } returns 0f
        every { getConsumptionHistoryUseCase.prepareChartData(any()) } returns emptyList()

        // Inicializar el ViewModel, que llamará a loadConsumptionHistory en init
        viewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
        clearAllMocks()
    }

    @Test
    fun `initial state has default values`() = runTest {
        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.consumptions.isEmpty())
        assertFalse(state.isLoading) // El estado inicial es false hasta que se ejecuta loadConsumptionHistory
        assertNull(state.error)
        assertNull(state.startDate)
        assertNull(state.endDate)
        assertEquals(0f, state.lastDayConsumption, 0.01f)
        assertEquals(0f, state.lastWeekConsumption, 0.01f)
        assertEquals(0f, state.lastMonthConsumption, 0.01f)
        assertEquals(0f, state.customPeriodConsumption, 0.01f)
        assertTrue(state.chartData.isEmpty())

        // Después de avanzar hasta que termine la corrutina inicial
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadConsumptionHistory updates state with consumptions`() = runTest {
        // Arrange
        val testConsumptions = listOf(
            createTestConsumption(1),
            createTestConsumption(2)
        )

        // Act
        consumptionsFlow.value = testConsumptions
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(testConsumptions, state.consumptions)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `setDateRange updates state and reloads data`() = runTest {
        // Arrange
        val startDate = 1000L
        val endDate = 2000L
        val filteredConsumptions = listOf(createTestConsumption(3))

        every { getConsumptionHistoryUseCase(startDate, endDate) } returns flowOf(
            filteredConsumptions
        )

        // Act
        viewModel.setDateRange(startDate, endDate)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(startDate, state.startDate)
        assertEquals(endDate, state.endDate)
        assertEquals(filteredConsumptions, state.consumptions)
        // Verificar que se llama el método al menos una vez (puede ser más debido a updateCustomPeriodSummary)
        verify(atLeast = 1) { getConsumptionHistoryUseCase(startDate, endDate) }
    }

    @Test
    fun `clearDateFilter resets date range and reloads all data`() = runTest {
        // Arrange - Primero establecer un filtro de fecha
        val startDate = 1000L
        val endDate = 2000L
        every { getConsumptionHistoryUseCase(startDate, endDate) } returns flowOf(emptyList())

        viewModel.setDateRange(startDate, endDate)
        advanceUntilIdle()

        // Verificar que el filtro se estableció
        assertEquals(startDate, viewModel.uiState.value.startDate)

        // Act - Ahora limpiar el filtro
        viewModel.clearDateFilter()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertNull(state.startDate)
        assertNull(state.endDate)
        verify(exactly = 2) {
            getConsumptionHistoryUseCase(
                null,
                null
            )
        } // Una vez en init y otra en clearDateFilter
    }

    @Test
    fun `setLastWeekFilter sets correct date range`() = runTest {
        // Mockear la función setDateRangeFromCalendar que es privada
        // mediante la verificación del método público que la llama
        every { getConsumptionHistoryUseCase(any(), any()) } returns flowOf(emptyList())

        // Act
        viewModel.setLastWeekFilter()
        advanceUntilIdle()

        // Assert
        verify { getConsumptionHistoryUseCase(any(), any()) }
        assertNotNull(viewModel.uiState.value.startDate)
        assertNotNull(viewModel.uiState.value.endDate)
        // No podemos verificar los valores exactos porque son calculados internamente con Calendar
    }

    @Test
    fun `setLastMonthFilter sets correct date range`() = runTest {
        // Arrange
        every { getConsumptionHistoryUseCase(any(), any()) } returns flowOf(emptyList())

        // Act
        viewModel.setLastMonthFilter()
        advanceUntilIdle()

        // Assert
        verify { getConsumptionHistoryUseCase(any(), any()) }
        assertNotNull(viewModel.uiState.value.startDate)
        assertNotNull(viewModel.uiState.value.endDate)
        // No podemos verificar los valores exactos porque son calculados internamente con Calendar
    }

    @Test
    fun `setLastDayFilter sets correct date range`() = runTest {
        // Arrange
        every { getConsumptionHistoryUseCase(any(), any()) } returns flowOf(emptyList())

        // Act
        viewModel.setLastDayFilter()
        advanceUntilIdle()

        // Assert
        verify { getConsumptionHistoryUseCase(any(), any()) }
        assertNotNull(viewModel.uiState.value.startDate)
        assertNotNull(viewModel.uiState.value.endDate)
        // No podemos verificar los valores exactos porque son calculados internamente con Calendar
    }

    @Test
    fun `exception during loadConsumptionHistory updates error state`() = runTest {
        // Arrange
        val errorMessage = "Error cargando datos"
        every { getConsumptionHistoryUseCase(null, null) } throws Exception(errorMessage)

        // Act - Recrear el ViewModel para forzar la carga con error
        viewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(errorMessage, state.error)
        assertFalse(state.isLoading)
        assertTrue(state.consumptions.isEmpty())
    }

    private fun createTestConsumption(id: Long = 1) = Consumption(
        id = id,
        cylinderId = id,
        cylinderName = "Test Cylinder $id",
        date = 1000L * id,
        fuelKilograms = 10f,
        fuelPercentage = 75f,
        totalWeight = 15f,
        isCalibrated = true,
        isHistorical = false
    )
}
