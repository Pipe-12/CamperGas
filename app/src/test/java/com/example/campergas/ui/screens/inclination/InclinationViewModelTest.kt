package com.example.campergas.ui.screens.inclination

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.domain.model.Inclination
import com.example.campergas.domain.usecase.GetInclinationUseCase
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
class InclinationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: InclinationViewModel
    private val getInclinationUseCase = mockk<GetInclinationUseCase>()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleInclination = Inclination(
        pitch = 15.5f,
        roll = -3.2f,
        timestamp = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Default mock
        every { getInclinationUseCase() } returns flowOf(sampleInclination)

        viewModel = InclinationViewModel(getInclinationUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state is correct`() {
        // Assert
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.inclination)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `inclination data loads successfully`() = runTest {
        // The viewModel automatically collects data in init
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleInclination, state.inclination)
        assertNull(state.error)

        verify { getInclinationUseCase() }
    }

    @Test
    fun `inclination data handles null values`() = runTest {
        // Arrange
        every { getInclinationUseCase() } returns flowOf(null)

        // Create new viewModel to trigger collection
        val newViewModel = InclinationViewModel(getInclinationUseCase)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = newViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.inclination)
        assertNull(state.error)
    }

    @Test
    fun `inclination data handles use case exception`() = runTest {
        // Arrange
        val errorMessage = "Sensor error"
        every { getInclinationUseCase() } throws Exception(errorMessage)

        // Create new viewModel to trigger collection
        val newViewModel = InclinationViewModel(getInclinationUseCase)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = newViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.inclination)
        assertTrue(state.error?.contains(errorMessage) == true)
    }

    @Test
    fun `refresh updates inclination data`() = runTest {
        // Arrange - initial load
        testScheduler.advanceUntilIdle()
        verify(exactly = 1) { getInclinationUseCase() }

        // New inclination data
        val newInclination = Inclination(
            pitch = 10.0f,
            roll = 5.0f,
            timestamp = System.currentTimeMillis()
        )
        every { getInclinationUseCase() } returns flowOf(newInclination)

        // Act
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        // Assert
        verify(exactly = 2) { getInclinationUseCase() }
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(newInclination, state.inclination)
        assertNull(state.error)
    }

    @Test
    fun `refresh handles exception`() = runTest {
        // Arrange - initial successful load
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.error)

        // Mock failure for refresh
        val errorMessage = "Refresh failed"
        every { getInclinationUseCase() } throws Exception(errorMessage)

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
        every { getInclinationUseCase() } throws Exception("Test error")
        val newViewModel = InclinationViewModel(getInclinationUseCase)
        testScheduler.advanceUntilIdle()
        assertNotNull(newViewModel.uiState.value.error)

        // Act
        newViewModel.clearError()

        // Assert
        assertNull(newViewModel.uiState.value.error)
    }

    @Test
    fun `multiple inclination updates are handled correctly`() = runTest {
        // Arrange
        val inclinations = listOf(
            Inclination(10.0f, 5.0f, System.currentTimeMillis()),
            Inclination(15.0f, -2.0f, System.currentTimeMillis()),
            Inclination(8.0f, 3.0f, System.currentTimeMillis())
        )

        // Act & Assert for each inclination
        inclinations.forEach { inclination ->
            every { getInclinationUseCase() } returns flowOf(inclination)
            viewModel.refresh()
            testScheduler.advanceUntilIdle()
            
            assertEquals(inclination, viewModel.uiState.value.inclination)
        }
    }

    @Test
    fun `loading state is managed correctly during refresh`() = runTest {
        // Arrange - initial load completed
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)

        // Note: Due to immediate flow emission, loading state is very brief
        // In real scenarios with network/sensor delays, this would be more observable
        
        // Act
        viewModel.refresh()
        testScheduler.advanceUntilIdle()
        
        // Assert final state
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `inclination data with extreme values is handled`() = runTest {
        // Arrange - extreme inclination values
        val extremeInclination = Inclination(
            pitch = 89.9f,  // Near vertical
            roll = -89.9f,  // Near vertical opposite
            timestamp = System.currentTimeMillis()
        )
        every { getInclinationUseCase() } returns flowOf(extremeInclination)

        // Create new viewModel
        val newViewModel = InclinationViewModel(getInclinationUseCase)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = newViewModel.uiState.value
        assertEquals(extremeInclination, state.inclination)
        assertNull(state.error)
    }

    @Test
    fun `inclination data with zero values is handled`() = runTest {
        // Arrange - zero inclination (level)
        val levelInclination = Inclination(
            pitch = 0.0f,
            roll = 0.0f,
            timestamp = System.currentTimeMillis()
        )
        every { getInclinationUseCase() } returns flowOf(levelInclination)

        // Create new viewModel
        val newViewModel = InclinationViewModel(getInclinationUseCase)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = newViewModel.uiState.value
        assertEquals(levelInclination, state.inclination)
        assertNull(state.error)
    }

    @Test
    fun `concurrent refreshes are handled gracefully`() = runTest {
        // Arrange
        testScheduler.advanceUntilIdle()

        // Act - multiple rapid refreshes
        repeat(5) {
            viewModel.refresh()
        }
        testScheduler.advanceUntilIdle()

        // Assert - should handle without crashes
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleInclination, state.inclination)
        assertNull(state.error)
    }
}
