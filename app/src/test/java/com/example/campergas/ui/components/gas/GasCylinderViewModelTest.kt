package com.example.campergas.ui.components.gas

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.usecase.AddGasCylinderUseCase
import com.example.campergas.domain.usecase.GetActiveCylinderUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GasCylinderViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: GasCylinderViewModel
    private val addGasCylinderUseCase: AddGasCylinderUseCase = mockk(relaxed = true)
    private val getActiveCylinderUseCase: GetActiveCylinderUseCase = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    // Flujo para simular la bombona activa
    private val activeCylinderFlow = MutableStateFlow<GasCylinder?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // Setup mock responses
        every { getActiveCylinderUseCase() } returns activeCylinderFlow

        viewModel = GasCylinderViewModel(
            addGasCylinderUseCase,
            getActiveCylinderUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
        clearAllMocks()
    }

    @Test
    fun `initial state has default values and no active cylinder`() = runTest {
        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Sin bombona activa - Las mediciones no se guardarán", state.errorMessage)
        assertNull(state.successMessage)
        assertNull(viewModel.activeCylinder.value)
    }

    @Test
    fun `updates active cylinder when data is received`() = runTest {
        // Arrange
        val testCylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )

        // Act
        activeCylinderFlow.value = testCylinder
        advanceUntilIdle()

        // Assert
        assertEquals(testCylinder, viewModel.activeCylinder.value)
        assertNull(viewModel.uiState.value.errorMessage) // El mensaje de error debería desaparecer
    }

    @Test
    fun `error message is set when active cylinder is null`() = runTest {
        // Arrange - First set an active cylinder
        val testCylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        activeCylinderFlow.value = testCylinder
        advanceUntilIdle()

        // Act - Then set it back to null
        activeCylinderFlow.value = null
        advanceUntilIdle()

        // Assert
        assertNull(viewModel.activeCylinder.value)
        assertEquals(
            "Sin bombona activa - Las mediciones no se guardarán",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun `addCylinder success updates uiState with success message`() = runTest {
        // Arrange
        coEvery { addGasCylinderUseCase(any(), any(), any(), any()) } returns Result.success(1L)

        // Act
        viewModel.addCylinder("Test Cylinder", 5.0f, 10.0f, true)
        
        // Let the coroutine complete before asserting the final state
        advanceUntilIdle()

        // Assert - Check immediate state after success
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Bombona añadida correctamente", state.successMessage)
        assertNull(state.errorMessage)

        // Verify the use case was called with correct parameters
        coVerify { addGasCylinderUseCase("Test Cylinder", 5.0f, 10.0f, true) }

        // Success message should clear after delay
        advanceTimeBy(3100) // Más que el delay de 3s
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `addCylinder failure updates uiState with error message`() = runTest {
        // Arrange
        val errorMessage = "Error al añadir bombona: nombre duplicado"
        coEvery {
            addGasCylinderUseCase(any(), any(), any(), any())
        } returns Result.failure(Exception(errorMessage))

        // Act
        viewModel.addCylinder("Test Cylinder", 5.0f, 10.0f, true)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.errorMessage)
        assertNull(state.successMessage)

        // Verify the use case was called
        coVerify { addGasCylinderUseCase("Test Cylinder", 5.0f, 10.0f, true) }
    }

    @Test
    fun `addCylinder sets loading state during operation`() = runTest {
        // Arrange - Use a controlled manual dispatcher to check intermediate state
        val manualDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(manualDispatcher)

        coEvery { addGasCylinderUseCase(any(), any(), any(), any()) } returns Result.success(1L)

        // Recreate viewModel with new dispatcher
        val localViewModel = GasCylinderViewModel(
            addGasCylinderUseCase,
            getActiveCylinderUseCase
        )

        // Act - Start operation but don't advance time yet
        localViewModel.addCylinder("Test Cylinder", 5.0f, 10.0f, true)

        // Assert - Should be in loading state before completion
        assertTrue(localViewModel.uiState.value.isLoading)

        // Now complete the operation
        manualDispatcher.scheduler.advanceUntilIdle()

        // Should no longer be loading
        assertFalse(localViewModel.uiState.value.isLoading)
        assertEquals("Bombona añadida correctamente", localViewModel.uiState.value.successMessage)
    }

    @Test
    fun `addCylinder exception handling works correctly`() = runTest {
        // Arrange
        coEvery {
            addGasCylinderUseCase(any(), any(), any(), any())
        } throws Exception("Error inesperado")

        // Act
        viewModel.addCylinder("Test Cylinder", 5.0f, 10.0f, true)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error: Error inesperado", state.errorMessage)
        assertNull(state.successMessage)
    }
}
