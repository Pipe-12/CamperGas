package com.example.campergas.ui.screens.settings

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.ThemeMode
import com.example.campergas.domain.usecase.ConfigureReadingIntervalsUseCase
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
class SettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SettingsViewModel
    private val preferencesDataStore: PreferencesDataStore = mockk(relaxed = true)
    private val configureReadingIntervalsUseCase: ConfigureReadingIntervalsUseCase =
        mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    // Flujos para simular preferencias
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val notificationsEnabledFlow = MutableStateFlow(true)
    private val weightIntervalFlow = MutableStateFlow(60) // 60 segundos = 1 minuto
    private val inclinationIntervalFlow = MutableStateFlow(15) // 15 segundos

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // Setup mock responses
        every { preferencesDataStore.themeMode } returns themeModeFlow
        every { preferencesDataStore.areNotificationsEnabled } returns notificationsEnabledFlow

        coEvery { preferencesDataStore.setThemeMode(any()) } coAnswers {
            themeModeFlow.value = firstArg()
        }

        coEvery { preferencesDataStore.setNotificationsEnabled(any()) } coAnswers {
            notificationsEnabledFlow.value = firstArg()
        }

        every { configureReadingIntervalsUseCase.getWeightReadIntervalSeconds() } returns weightIntervalFlow
        every { configureReadingIntervalsUseCase.getInclinationReadIntervalSeconds() } returns inclinationIntervalFlow
        coEvery { configureReadingIntervalsUseCase.setWeightReadInterval(any()) } returns Unit
        coEvery { configureReadingIntervalsUseCase.setInclinationReadInterval(any()) } returns Unit

        viewModel = SettingsViewModel(
            preferencesDataStore,
            configureReadingIntervalsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
        clearAllMocks()
    }

    @Test
    fun `initial state has default values from preferences`() = runTest {
        // Assert
        val state = viewModel.uiState.value
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertTrue(state.notificationsEnabled)
        assertFalse(state.isLoading)
        assertNull(state.error)

        // Verify weight and inclination intervals
        assertEquals(1, viewModel.weightInterval.value) // 60s convertido a 1 min
        assertEquals(15, viewModel.inclinationInterval.value)
    }

    @Test
    fun `setThemeMode updates preferences`() = runTest {
        // Act
        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        // Assert
        coVerify { preferencesDataStore.setThemeMode(ThemeMode.DARK) }
        assertEquals(ThemeMode.DARK, themeModeFlow.value)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `toggleNotifications inverts notification state`() = runTest {
        // Act - Toggle from true to false
        viewModel.toggleNotifications()
        advanceUntilIdle()

        // Assert
        coVerify { preferencesDataStore.setNotificationsEnabled(false) }
        assertFalse(viewModel.uiState.value.notificationsEnabled)

        // Act - Toggle back to true
        viewModel.toggleNotifications()
        advanceUntilIdle()

        // Assert
        coVerify { preferencesDataStore.setNotificationsEnabled(true) }
        assertTrue(viewModel.uiState.value.notificationsEnabled)
    }

    @Test
    fun `setWeightInterval converts minutes to seconds and updates use case`() = runTest {
        // Act
        viewModel.setWeightInterval(5) // 5 minutos

        // Assert - Check the status message is set before delay
        assertEquals("Intervalo de peso configurado: 5 min", viewModel.operationStatus.value)
        coVerify { configureReadingIntervalsUseCase.setWeightReadInterval(300) } // 5*60 = 300s

        // Verify message is cleared after delay
        advanceTimeBy(2100)
        assertNull(viewModel.operationStatus.value)
    }

    @Test
    fun `setInclinationInterval updates use case directly with seconds`() = runTest {
        // Act
        viewModel.setInclinationInterval(30) // 30 segundos

        // Assert - Check the status message is set before delay
        assertEquals("Intervalo de inclinación configurado: 30s", viewModel.operationStatus.value)
        coVerify { configureReadingIntervalsUseCase.setInclinationReadInterval(30) }

        // Verify message is cleared after delay
        advanceTimeBy(2100)
        assertNull(viewModel.operationStatus.value)
    }

    @Test
    fun `setWeightInterval handles exceptions`() = runTest {
        // Arrange
        coEvery { configureReadingIntervalsUseCase.setWeightReadInterval(any()) } throws
                Exception("Error de conexión")

        // Act
        viewModel.setWeightInterval(5)

        // Assert - Check the error message is set before delay
        assertEquals(
            "Error al configurar intervalo de peso: Error de conexión",
            viewModel.operationStatus.value
        )

        // Verify message is cleared after delay
        advanceTimeBy(2100)
        assertNull(viewModel.operationStatus.value)
    }

    @Test
    fun `setInclinationInterval handles exceptions`() = runTest {
        // Arrange
        coEvery { configureReadingIntervalsUseCase.setInclinationReadInterval(any()) } throws
                Exception("Error de conexión")

        // Act
        viewModel.setInclinationInterval(10)

        // Assert - Check the error message is set before delay
        assertEquals(
            "Error al configurar intervalo de inclinación: Error de conexión",
            viewModel.operationStatus.value
        )

        // Verify message is cleared after delay
        advanceTimeBy(2100)
        assertNull(viewModel.operationStatus.value)
    }
}
