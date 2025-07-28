package com.example.campergas.ui.screens.bleconnect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.data.ble.BleDeviceScanner
import com.example.campergas.data.ble.CamperGasBleService
import com.example.campergas.domain.model.BleDevice
import com.example.campergas.domain.usecase.ConnectBleDeviceUseCase
import com.example.campergas.domain.usecase.ScanBleDevicesUseCase
import com.example.campergas.utils.BluetoothPermissionManager
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
class BleConnectViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: BleConnectViewModel
    private val scanBleDevicesUseCase = mockk<ScanBleDevicesUseCase>()
    private val connectBleDeviceUseCase = mockk<ConnectBleDeviceUseCase>()
    private val bluetoothPermissionManager = mockk<BluetoothPermissionManager>()
    private val camperGasBleService = mockk<CamperGasBleService>()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleDevice = BleDevice(
        name = "CamperGas Sensor",
        address = "00:11:22:33:44:55",
        rssi = -50
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Default mocks
        every { camperGasBleService.connectionState } returns flowOf(false)
        every { bluetoothPermissionManager.hasBluetoothPermissions() } returns true
        every { bluetoothPermissionManager.isBluetoothEnabled() } returns true
        coEvery { scanBleDevicesUseCase() } returns flowOf(emptyList())
        coEvery { connectBleDeviceUseCase(any()) } returns Result.success(Unit)

        viewModel = BleConnectViewModel(
            scanBleDevicesUseCase = scanBleDevicesUseCase,
            connectBleDeviceUseCase = connectBleDeviceUseCase,
            bluetoothPermissionManager = bluetoothPermissionManager,
            camperGasBleService = camperGasBleService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state is correct`() {
        // Assert
        assertFalse(viewModel.uiState.value.isScanning)
        assertFalse(viewModel.uiState.value.isConnected)
        assertTrue(viewModel.uiState.value.availableDevices.isEmpty())
        assertNull(viewModel.uiState.value.connectionError)
    }

    @Test
    fun `connectionState updates from service`() = runTest {
        // Arrange
        every { camperGasBleService.connectionState } returns flowOf(true)

        // Create new viewModel to trigger collection
        val newViewModel = BleConnectViewModel(
            scanBleDevicesUseCase = scanBleDevicesUseCase,
            connectBleDeviceUseCase = connectBleDeviceUseCase,
            bluetoothPermissionManager = bluetoothPermissionManager,
            camperGasBleService = camperGasBleService
        )

        // Assert
        assertTrue(newViewModel.uiState.value.isConnected)
    }

    @Test
    fun `startScanning succeeds when permissions granted`() = runTest {
        // Arrange
        val devicesList = listOf(sampleDevice)
        coEvery { scanBleDevicesUseCase() } returns flowOf(devicesList)

        // Act
        viewModel.startScanning()
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isScanning)
        assertEquals(devicesList, state.availableDevices)
        assertNull(state.connectionError)

        coVerify { scanBleDevicesUseCase() }
    }

    @Test
    fun `startScanning fails when no bluetooth permissions`() = runTest {
        // Arrange
        every { bluetoothPermissionManager.hasBluetoothPermissions() } returns false

        // Act
        viewModel.startScanning()
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isScanning)
        assertTrue(state.availableDevices.isEmpty())
        assertTrue(state.connectionError?.contains("permisos de Bluetooth") == true)

        coVerify(exactly = 0) { scanBleDevicesUseCase() }
    }

    @Test
    fun `startScanning fails when bluetooth disabled`() = runTest {
        // Arrange
        every { bluetoothPermissionManager.isBluetoothEnabled() } returns false

        // Act
        viewModel.startScanning()
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isScanning)
        assertTrue(state.availableDevices.isEmpty())
        assertTrue(state.connectionError?.contains("Bluetooth no está habilitado") == true)

        coVerify(exactly = 0) { scanBleDevicesUseCase() }
    }

    @Test
    fun `startScanning handles scan exception`() = runTest {
        // Arrange
        val errorMessage = "Scan failed"
        coEvery { scanBleDevicesUseCase() } throws Exception(errorMessage)

        // Act
        viewModel.startScanning()
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isScanning)
        assertTrue(state.availableDevices.isEmpty())
        assertTrue(state.connectionError?.contains(errorMessage) == true)
    }

    @Test
    fun `stopScanning stops scanning state`() = runTest {
        // Arrange - start scanning first
        coEvery { scanBleDevicesUseCase() } returns flowOf(listOf(sampleDevice))
        viewModel.startScanning()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isScanning)

        // Act
        viewModel.stopScanning()
        testScheduler.advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isScanning)
    }

    @Test
    fun `connectToDevice succeeds with valid device`() = runTest {
        // Arrange
        coEvery { connectBleDeviceUseCase(sampleDevice.address) } returns Result.success(Unit)

        // Act
        viewModel.connectToDevice(sampleDevice)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertNull(state.connectionError)

        coVerify { connectBleDeviceUseCase(sampleDevice.address) }
    }

    @Test
    fun `connectToDevice handles connection failure`() = runTest {
        // Arrange
        val errorMessage = "Connection failed"
        coEvery { connectBleDeviceUseCase(sampleDevice.address) } returns Result.failure(Exception(errorMessage))

        // Act
        viewModel.connectToDevice(sampleDevice)
        testScheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.connectionError?.contains(errorMessage) == true)

        coVerify { connectBleDeviceUseCase(sampleDevice.address) }
    }

    @Test
    fun `disconnect calls service disconnect`() = runTest {
        // Arrange
        every { camperGasBleService.disconnect() } just Runs

        // Act
        viewModel.disconnect()

        // Assert
        verify { camperGasBleService.disconnect() }
    }

    @Test
    fun `clearError clears connection error`() = runTest {
        // Arrange - set an error first
        coEvery { connectBleDeviceUseCase(any()) } returns Result.failure(Exception("Test error"))
        viewModel.connectToDevice(sampleDevice)
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.connectionError)

        // Act
        viewModel.clearError()

        // Assert
        assertNull(viewModel.uiState.value.connectionError)
    }

    @Test
    fun `multiple scans update device list`() = runTest {
        // Arrange
        val firstScan = listOf(sampleDevice)
        val secondScan = listOf(
            sampleDevice,
            BleDevice("Device 2", "00:11:22:33:44:66", -60)
        )

        // First scan
        coEvery { scanBleDevicesUseCase() } returns flowOf(firstScan)
        viewModel.startScanning()
        testScheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.availableDevices.size)

        // Second scan
        coEvery { scanBleDevicesUseCase() } returns flowOf(secondScan)
        viewModel.startScanning()
        testScheduler.advanceUntilIdle()

        // Assert
        assertEquals(2, viewModel.uiState.value.availableDevices.size)
    }

    @Test
    fun `scanning stops automatically after timeout`() = runTest {
        // Arrange
        coEvery { scanBleDevicesUseCase() } returns flowOf(listOf(sampleDevice))

        // Act
        viewModel.startScanning()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isScanning)

        // Simulate timeout - advance time significantly
        testScheduler.advanceTimeBy(15000) // 15 seconds
        testScheduler.advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isScanning)
    }
}
