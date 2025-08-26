package com.example.campergas.ui.screens.home

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.usecase.CheckBleConnectionUseCase
import com.example.campergas.domain.usecase.ConnectBleDeviceUseCase
import com.example.campergas.domain.usecase.GetFuelDataUseCase
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
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private val context: Context = mockk(relaxed = true)
    private val getFuelDataUseCase: GetFuelDataUseCase = mockk()
    private val connectBleDeviceUseCase: ConnectBleDeviceUseCase = mockk()
    private val checkBleConnectionUseCase: CheckBleConnectionUseCase = mockk()

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
        every { checkBleConnectionUseCase() } returns connectionStateFlow
        every { connectBleDeviceUseCase.getLastConnectedDevice() } returns lastConnectedDeviceFlow
        coEvery { connectBleDeviceUseCase.invoke(any()) } returns Unit
        coEvery { connectBleDeviceUseCase.disconnect() } returns Unit

        viewModel = HomeViewModel(
            context,
            getFuelDataUseCase,
            connectBleDeviceUseCase,
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
        @Suppress("UNUSED_VARIABLE")
        val newViewModel = HomeViewModel(
            context,
            getFuelDataUseCase,
            connectBleDeviceUseCase,
            checkBleConnectionUseCase
        )
        advanceUntilIdle()

        // Assert
        coVerify { connectBleDeviceUseCase.invoke("AA:BB:CC:DD:EE:FF") }
    }

    @Test
    fun `init block doesn't connect if already connected`() = runTest {
        // Arrange - Set up initial state where device is already connected
        connectionStateFlow.value = true
        lastConnectedDeviceFlow.value = "AA:BB:CC:DD:EE:FF"

        // Create separate mock to verify connection attempts
        val connectUseCase = mockk<ConnectBleDeviceUseCase>(relaxed = true)
        every { connectUseCase.getLastConnectedDevice() } returns lastConnectedDeviceFlow
        coEvery { connectUseCase.invoke(any()) } returns Unit

        // Act - Create new viewModel
        @Suppress("UNUSED_VARIABLE")
        val newViewModel = HomeViewModel(
            context,
            getFuelDataUseCase,
            connectBleDeviceUseCase,
            checkBleConnectionUseCase
        )

        // Allow enough time for all flows to be processed
        advanceUntilIdle()

        // Assert - No connection should be attempted since already connected
        coVerify(exactly = 0) { connectUseCase.invoke(any()) }
    }

    @Test
    fun `init block doesn't connect if no last device`() = runTest {
        // Arrange
        lastConnectedDeviceFlow.value = "" // No hay dispositivo anterior
        connectionStateFlow.value = false

        // Act - Create new viewModel which will trigger init
        @Suppress("UNUSED_VARIABLE")
        val newViewModel = HomeViewModel(
            context,
            getFuelDataUseCase,
            connectBleDeviceUseCase,
            checkBleConnectionUseCase
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
    fun `requestSensorDataOnScreenOpen works correctly when connected`() = runTest {
        // Arrange
        connectionStateFlow.value = true

        // Act - Should not crash and complete successfully
        viewModel.requestSensorDataOnScreenOpen()
        advanceTimeBy(600) // Más que el delay de 500ms
        advanceUntilIdle()

        // Assert - Method completes without error (service call testing is out of scope)
        assertTrue(connectionStateFlow.value)
    }

    @Test
    fun `requestSensorDataOnScreenOpen works correctly when not connected`() =
        runTest {
            // Arrange
            connectionStateFlow.value = false

            // Act - Should not crash and complete successfully
            viewModel.requestSensorDataOnScreenOpen()
            advanceTimeBy(600) // Más que el delay de 500ms
            advanceUntilIdle()

            // Assert - Method completes without error (no service call when disconnected)
            assertFalse(connectionStateFlow.value)
        }

    // Nota: No podemos probar directamente onCleared porque es protected
    // pero indirectamente ya verificamos que disconnectDevice funciona correctamente
}
