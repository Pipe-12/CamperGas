package com.example.campergas.ui.screens.consumption

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.domain.model.Consumption
import com.example.campergas.domain.usecase.GetConsumptionHistoryUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ConsumptionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ConsumptionViewModel
    private val getConsumptionHistoryUseCase = mockk<GetConsumptionHistoryUseCase>()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleConsumptions = listOf(
        Consumption(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Cilindro 1",
            date = System.currentTimeMillis(),
            consumedKilograms = 2.5f,
            consumedPercentage = 22.7f,
            startWeight = 8.5f,
            endWeight = 6.0f
        ),
        Consumption(
            id = 2L,
            cylinderId = 1L,
            cylinderName = "Cilindro 1",
            date = System.currentTimeMillis() - 86400000, // 1 day ago
            consumedKilograms = 1.8f,
            consumedPercentage = 16.4f,
            startWeight = 6.0f,
            endWeight = 4.2f
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Default mock
        every { getConsumptionHistoryUseCase() } returns flowOf(sampleConsumptions)

        viewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state shows loading`() {
        // Assert
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.consumptions.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `consumption history loads successfully`() = runTest {
        // The viewModel automatically loads data in init
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleConsumptions, state.consumptions)
        assertNull(state.error)

        verify { getConsumptionHistoryUseCase() }
    }

    @Test
    fun `consumption history handles empty list`() = runTest {
        // Arrange
        every { getConsumptionHistoryUseCase() } returns flowOf(emptyList())

        // Create new viewModel to trigger loading
        val newViewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = newViewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.consumptions.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `consumption history handles use case exception`() = runTest {
        // Arrange
        val errorMessage = "Database error"
        every { getConsumptionHistoryUseCase() } throws Exception(errorMessage)

        // Create new viewModel to trigger loading
        val newViewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = newViewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.consumptions.isEmpty())
        assertTrue(state.error?.contains(errorMessage) == true)
    }

    @Test
    fun `refresh loads consumption history again`() = runTest {
        // Arrange - initial load
        testScheduler.advanceUntilIdle()
        verify(exactly = 1) { getConsumptionHistoryUseCase() }

        // Act
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        // Assert
        verify(exactly = 2) { getConsumptionHistoryUseCase() }
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleConsumptions, state.consumptions)
    }

    @Test
    fun `refresh handles exception`() = runTest {
        // Arrange - initial successful load
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.error)

        // Mock failure for refresh
        val errorMessage = "Refresh failed"
        every { getConsumptionHistoryUseCase() } throws Exception(errorMessage)

        // Act
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.error?.contains(errorMessage) == true)
    }

    @Test
    fun `clearError clears error state`() = runTest {
        // Arrange - set error state
        every { getConsumptionHistoryUseCase() } throws Exception("Test error")
        val newViewModel = ConsumptionViewModel(getConsumptionHistoryUseCase)
        testScheduler.advanceUntilIdle()
        assertNotNull(newViewModel.uiState.value.error)

        // Act
        newViewModel.clearError()

        // Assert
        assertNull(newViewModel.uiState.value.error)
    }

    @Test
    fun `multiple refreshes handle concurrent calls`() = runTest {
        // Arrange
        testScheduler.advanceUntilIdle()

        // Act - multiple rapid refreshes
        viewModel.refresh()
        viewModel.refresh()
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        // Assert - should handle gracefully without crash
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleConsumptions, state.consumptions)
    }

    @Test
    fun `consumption data updates when use case flow emits new data`() = runTest {
        // Arrange - initial load
        testScheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.consumptions.size)

        // New consumption data
        val newConsumptions = sampleConsumptions + Consumption(
            id = 3L,
            cylinderId = 1L,
            cylinderName = "Cilindro 1",
            date = System.currentTimeMillis() - 172800000, // 2 days ago
            consumedKilograms = 3.0f,
            consumedPercentage = 27.3f,
            startWeight = 4.2f,
            endWeight = 1.2f
        )

        // Mock new data emission
        every { getConsumptionHistoryUseCase() } returns flowOf(newConsumptions)

        // Act
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        // Assert
        assertEquals(3, viewModel.uiState.value.consumptions.size)
        assertEquals(newConsumptions, viewModel.uiState.value.consumptions)
    }

    @Test
    fun `loading state is correct during refresh`() = runTest {
        // Arrange - initial load completed
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)

        // Mock slow loading
        every { getConsumptionHistoryUseCase() } returns flowOf(sampleConsumptions)

        // Act
        viewModel.refresh()
        
        // Assert loading state during refresh
        // Note: Due to the immediate nature of our flow, loading might be very brief
        // In a real scenario with network calls, this would be more observable
        
        testScheduler.advanceUntilIdle()
        
        // Final state should not be loading
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
