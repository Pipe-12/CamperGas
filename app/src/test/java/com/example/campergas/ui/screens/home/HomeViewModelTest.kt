package com.example.campergas.ui.screens.home

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.usecase.ConnectBleDeviceUseCase
import com.example.campergas.domain.usecase.GetFuelDataUseCase
import com.example.campergas.domain.usecase.ReadSensorDataUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private val getFuelDataUseCase: GetFuelDataUseCase = mockk()
    private val connectBleDeviceUseCase: ConnectBleDeviceUseCase = mockk()
    private val readSensorDataUseCase: ReadSensorDataUseCase = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()
    
    // Flujos para los datos simulados
    private val fuelDataFlow = MutableStateFlow<FuelMeasurement?>(null)
    private val connectionStateFlow = MutableStateFlow(false)
    private val lastConnectedDeviceFlow = MutableStateFlow("")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any()) } returns 0

        // Setup mock responses
        every { getFuelDataUseCase() } returns fuelDataFlow
        every { readSensorDataUseCase.getConnectionState() } returns connectionStateFlow
        every { connectBleDeviceUseCase.getLastConnectedDevice() } returns lastConnectedDeviceFlow
        coEvery { connectBleDeviceUseCase.invoke(any()) } returns Unit
        coEvery { connectBleDeviceUseCase.disconnect() } returns Unit
        coEvery { readSensorDataUseCase.readAllSensorData() } returns Unit

        viewModel = HomeViewModel(
            getFuelDataUseCase,
            connectBleDeviceUseCase,
            readSensorDataUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
        clearAllMocks()
    }

    @Test
    fun `initial state is null for fuel data and false for connection`() = runTest {
        // Assert
        assertNull(viewModel.fuelData.value)
        assertFalse(viewModel.connectionState.value)
    }

    @Test
    fun `updates fuel data when data is received`() = runTest {
        // Arrange
        val testFuel = FuelMeasurement(
            cylinderId = 1L,
            cylinderName = "Test Cylinder",
            timestamp = 12345L,
            fuelKilograms = 5.0f,
            fuelPercentage = 50.0f,
            totalWeight = 10.0f,
            isCalibrated = true
        )

        // Act
        fuelDataFlow.value = testFuel
        advanceUntilIdle()

        // Assert
        assertEquals(testFuel, viewModel.fuelData.value)
    }

    @Test
    fun `updates connection state when changes`() = runTest {
        // Act
        connectionStateFlow.value = true
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.connectionState.value)
    }

    @Test
    fun `init block attempts to connect to last device if available`() = runTest {
        // Arrange - Create a new ViewModel to test init behavior
        lastConnectedDeviceFlow.value = "AA:BB:CC:DD:EE:FF"
        connectionStateFlow.value = false

        // Act - Create new viewModel which will trigger init
        val newViewModel = HomeViewModel(
            getFuelDataUseCase,
            connectBleDeviceUseCase,
            readSensorDataUseCase
        )
        advanceUntilIdle()

        // Assert
        coVerify { connectBleDeviceUseCase.invoke("AA:BB:CC:DD:EE:FF") }
    }

    @Test
    fun `init block doesn't connect if already connected`() = runTest {
        // Arrange
        lastConnectedDeviceFlow.value = "AA:BB:CC:DD:EE:FF"
        connectionStateFlow.value = true // Ya conectado

        // Act - Create new viewModel which will trigger init
        val newViewModel = HomeViewModel(
            getFuelDataUseCase,
            connectBleDeviceUseCase,
            readSensorDataUseCase
        )
        advanceUntilIdle()

        // Assert - No debería intentar conectar
        coVerify(exactly = 0) { connectBleDeviceUseCase.invoke(any()) }
    }

    @Test
    fun `init block doesn't connect if no last device`() = runTest {
        // Arrange
        lastConnectedDeviceFlow.value = "" // No hay dispositivo anterior
        connectionStateFlow.value = false

        // Act - Create new viewModel which will trigger init
        val newViewModel = HomeViewModel(
            getFuelDataUseCase,
            connectBleDeviceUseCase,
            readSensorDataUseCase
        )
        advanceUntilIdle()

        // Assert - No debería intentar conectar
        coVerify(exactly = 0) { connectBleDeviceUseCase.invoke(any()) }
    }

    @Test
    fun `disconnectDevice calls disconnect use case`() = runTest {
        // Act
        viewModel.disconnectDevice()
        advanceUntilIdle()

        // Assert
        coVerify { connectBleDeviceUseCase.disconnect() }
        assertFalse(viewModel.connectionState.value)
    }

    @Test
    fun `requestSensorDataOnScreenOpen calls readAllSensorData if connected`() = runTest {
        // Arrange
        connectionStateFlow.value = true

        // Act
        viewModel.requestSensorDataOnScreenOpen()
        advanceTimeBy(600) // Más que el delay de 500ms
        advanceUntilIdle()

        // Assert
        coVerify { readSensorDataUseCase.readAllSensorData() }
    }

    @Test
    fun `requestSensorDataOnScreenOpen doesn't call readAllSensorData if not connected`() = runTest {
        // Arrange
        connectionStateFlow.value = false

        // Act
        viewModel.requestSensorDataOnScreenOpen()
        advanceTimeBy(600) // Más que el delay de 500ms
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { readSensorDataUseCase.readAllSensorData() }
    }

    // Nota: No podemos probar directamente onCleared porque es protected
    // pero indirectamente ya verificamos que disconnectDevice funciona correctamente
}
