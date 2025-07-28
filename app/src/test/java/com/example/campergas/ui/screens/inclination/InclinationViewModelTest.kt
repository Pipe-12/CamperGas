package com.example.campergas.ui.screens.inclination

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.domain.model.Inclination
import com.example.campergas.domain.usecase.CheckBleConnectionUseCase
import com.example.campergas.domain.usecase.GetInclinationUseCase
import com.example.campergas.domain.usecase.RequestInclinationDataUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class InclinationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: InclinationViewModel
    private val getInclinationUseCase: GetInclinationUseCase = mockk()
    private val requestInclinationDataUseCase: RequestInclinationDataUseCase = mockk()
    private val checkBleConnectionUseCase: CheckBleConnectionUseCase = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val inclinationFlow = MutableStateFlow<Inclination?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0

        // Setup mock responses
        every { getInclinationUseCase() } returns inclinationFlow
        every { requestInclinationDataUseCase() } returns Unit
        every { checkBleConnectionUseCase.isConnected() } returns true

        viewModel = InclinationViewModel(
            getInclinationUseCase, 
            requestInclinationDataUseCase,
            checkBleConnectionUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
        clearAllMocks()
    }

    @Test
    fun `initial ui state has default values`() = runTest {
        // Assert
        val state = viewModel.uiState.value
        assertEquals(0f, state.inclinationPitch)
        assertEquals(0f, state.inclinationRoll)
        assertFalse(state.isLevel)
        assertTrue(state.isLoading)
        assertNull(state.error)
        assertEquals(0L, state.timestamp)
    }

    @Test
    fun `ui state updates when inclination data is received`() = runTest {
        // Arrange
        val testInclination = Inclination(pitch = 1.5f, roll = 0.5f, timestamp = 12345L)
        
        // Act
        inclinationFlow.value = testInclination
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(1.5f, state.inclinationPitch)
        assertEquals(0.5f, state.inclinationRoll)
        assertTrue(state.isLevel)  // Both pitch and roll are within ±2 degrees
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(12345L, state.timestamp)
    }

    @Test
    fun `ui state updates when vehicle is not level`() = runTest {
        // Arrange - Vehicle not level (roll > 2 degrees)
        val testInclination = Inclination(pitch = 1.5f, roll = 3.5f, timestamp = 12345L)
        
        // Act
        inclinationFlow.value = testInclination
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(1.5f, state.inclinationPitch)
        assertEquals(3.5f, state.inclinationRoll)
        assertFalse(state.isLevel)  // Roll is > ±2 degrees
        assertFalse(state.isLoading)
    }

    @Test
    fun `requestInclinationDataManually calls use case`() = runTest {
        // Act
        viewModel.requestInclinationDataManually()
        
        // Assert
        verify { requestInclinationDataUseCase() }
        assertTrue(viewModel.isRequestingData.value)
        
        // Should reset after delay
        advanceTimeBy(1600)  // 1.6 seconds (more than the 1.5s delay)
        assertFalse(viewModel.isRequestingData.value)
    }

    @Test
    fun `requestInclinationDataManually blocks rapid repeated calls`() = runTest {
        // Act - First call
        viewModel.requestInclinationDataManually()
        
        // Request again immediately
        viewModel.requestInclinationDataManually()
        
        // Assert - Should only call once
        verify(exactly = 1) { requestInclinationDataUseCase() }
    }

    @Test
    fun `isConnected delegates to CheckBleConnectionUseCase`() {
        // Arrange
        every { checkBleConnectionUseCase.isConnected() } returns true
        
        // Act
        val result = viewModel.isConnected()
        
        // Assert
        assertTrue(result)
        verify { checkBleConnectionUseCase.isConnected() }
    }

    @Test
    fun `canMakeRequest returns true when not in cooldown and not requesting`() = runTest {
        // Act - First call sets cooldown and requesting flag
        viewModel.requestInclinationDataManually()
        
        // Assert - Should be false immediately after
        assertFalse(viewModel.canMakeRequest())
        
        // Act - Wait for cooldown (2s) and reset requesting flag (1.5s)
        advanceTimeBy(2100)  // > 2 seconds cooldown time
        
        // Assert - Should now be true
        assertTrue(viewModel.canMakeRequest())
    }
}