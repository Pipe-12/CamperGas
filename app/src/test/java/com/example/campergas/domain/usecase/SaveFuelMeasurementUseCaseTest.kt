package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SaveFuelMeasurementUseCaseTest {

    private lateinit var saveFuelMeasurementUseCase: SaveFuelMeasurementUseCase
    private val fuelMeasurementRepository: FuelMeasurementRepository = mockk()
    private val gasCylinderRepository: GasCylinderRepository = mockk()

    @Before
    fun setUp() {
        saveFuelMeasurementUseCase = SaveFuelMeasurementUseCase(
            fuelMeasurementRepository,
            gasCylinderRepository
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `saveRealTimeMeasurement succeeds with valid data and active cylinder`() = runTest {
        // Arrange
        val totalWeight = 15.0f
        val activeCylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 10.0f,
            capacity = 5.0f,
            isActive = true
        )
        val savedMeasurementId = 123L

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(activeCylinder)
        coEvery { fuelMeasurementRepository.insertMeasurement(any()) } returns savedMeasurementId

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(totalWeight)

        // Assert
        assertTrue(result.isSuccess)
        val saveResult = result.getOrNull()!!
        assertEquals(savedMeasurementId, saveResult.measurementId)
        assertTrue(saveResult.processed)
        assertEquals("Medición guardada correctamente", saveResult.reason)

        coVerify {
            fuelMeasurementRepository.insertMeasurement(
                withArg { measurement ->
                    assertEquals(1L, measurement.cylinderId)
                    assertEquals("Test Cylinder", measurement.cylinderName)
                    assertEquals(5.0f, measurement.fuelKilograms, 0.01f)
                    assertEquals(100.0f, measurement.fuelPercentage, 0.01f)
                    assertEquals(15.0f, measurement.totalWeight, 0.01f)
                    assertTrue(measurement.isCalibrated)
                    assertFalse(measurement.isHistorical)
                }
            )
        }
    }

    @Test
    fun `saveRealTimeMeasurement fails when no active cylinder`() = runTest {
        // Arrange
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(null)

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(15.0f)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("No hay bombona activa configurada", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveRealTimeMeasurement skips saving when called too frequently`() = runTest {
        // Arrange
        val activeCylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 10.0f,
            capacity = 5.0f,
            isActive = true
        )

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(activeCylinder)
        coEvery { fuelMeasurementRepository.insertMeasurement(any()) } returns 1L

        // Act - First call should succeed
        val firstResult = saveFuelMeasurementUseCase.saveRealTimeMeasurement(15.0f)
        assertTrue(firstResult.isSuccess)

        // Act - Second call immediately should be skipped
        val secondResult = saveFuelMeasurementUseCase.saveRealTimeMeasurement(15.0f)

        // Assert
        assertTrue(secondResult.isSuccess)
        val saveResult = secondResult.getOrNull()!!
        assertEquals(-1L, saveResult.measurementId)
        assertFalse(saveResult.processed)
        assertTrue(saveResult.reason.contains("Medición omitida"))
    }

    @Test
    fun `saveRealTimeMeasurement calculates fuel correctly`() = runTest {
        // Arrange
        val totalWeight = 12.5f
        val activeCylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 10.0f,
            capacity = 5.0f,
            isActive = true
        )

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(activeCylinder)
        coEvery { fuelMeasurementRepository.insertMeasurement(any()) } returns 1L

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(totalWeight)

        // Assert
        assertTrue(result.isSuccess)
        coVerify {
            fuelMeasurementRepository.insertMeasurement(
                withArg { measurement ->
                    assertEquals(2.5f, measurement.fuelKilograms, 0.01f) // 12.5 - 10.0
                    assertEquals(50.0f, measurement.fuelPercentage, 0.01f) // 2.5 / 5.0 * 100
                }
            )
        }
    }

    @Test
    fun `saveRealTimeMeasurement handles negative fuel weight`() = runTest {
        // Arrange
        val totalWeight = 8.0f // Less than tare
        val activeCylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 10.0f,
            capacity = 5.0f,
            isActive = true
        )

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(activeCylinder)
        coEvery { fuelMeasurementRepository.insertMeasurement(any()) } returns 1L

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(totalWeight)

        // Assert
        assertTrue(result.isSuccess)
        coVerify {
            fuelMeasurementRepository.insertMeasurement(
                withArg { measurement ->
                    assertEquals(0f, measurement.fuelKilograms, 0.01f) // Negative clamped to 0
                    assertEquals(0f, measurement.fuelPercentage, 0.01f)
                }
            )
        }
    }

    @Test
    fun `saveHistoricalMeasurements succeeds with valid data`() = runTest {
        // Arrange
        val cylinderId = 1L
        val cylinder = GasCylinder(
            id = cylinderId,
            name = "Test Cylinder",
            tare = 10.0f,
            capacity = 5.0f,
            isActive = true
        )
        val weightMeasurements = listOf(
            Pair(15.0f, 1640995200000L),
            Pair(14.0f, 1640995260000L)
        )

        coEvery { gasCylinderRepository.getCylinderById(cylinderId) } returns cylinder
        coEvery { fuelMeasurementRepository.insertMeasurements(any()) } returns Unit

        // Act
        val result = saveFuelMeasurementUseCase.saveHistoricalMeasurements(
            cylinderId,
            weightMeasurements
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()) // Should return count of saved measurements

        coVerify {
            fuelMeasurementRepository.insertMeasurements(
                withArg { measurements ->
                    assertEquals(2, measurements.size)

                    val first = measurements[0]
                    assertEquals(cylinderId, first.cylinderId)
                    assertEquals("Test Cylinder", first.cylinderName)
                    assertEquals(5.0f, first.fuelKilograms, 0.01f)
                    assertEquals(100.0f, first.fuelPercentage, 0.01f)
                    assertEquals(15.0f, first.totalWeight, 0.01f)
                    assertTrue(first.isCalibrated)
                    assertTrue(first.isHistorical)

                    val second = measurements[1]
                    assertEquals(4.0f, second.fuelKilograms, 0.01f)
                    assertEquals(80.0f, second.fuelPercentage, 0.01f)
                    assertEquals(14.0f, second.totalWeight, 0.01f)
                }
            )
        }
    }

    @Test
    fun `saveHistoricalMeasurements fails when cylinder not found`() = runTest {
        // Arrange
        val cylinderId = 999L
        val weightMeasurements = listOf(Pair(15.0f, 1640995200000L))

        coEvery { gasCylinderRepository.getCylinderById(cylinderId) } returns null

        // Act
        val result = saveFuelMeasurementUseCase.saveHistoricalMeasurements(
            cylinderId,
            weightMeasurements
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Bombona no encontrada", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveHistoricalMeasurements filters invalid measurements`() = runTest {
        // Arrange
        val cylinderId = 1L
        val cylinder = GasCylinder(
            id = cylinderId,
            name = "Test Cylinder",
            tare = 10.0f,
            capacity = 5.0f,
            isActive = true
        )
        // Include some invalid data (NaN, Infinity)
        val weightMeasurements = listOf(
            Pair(15.0f, 1640995200000L), // Valid
            Pair(Float.NaN, 1640995260000L), // Invalid
            Pair(14.0f, 1640995320000L) // Valid
        )

        coEvery { gasCylinderRepository.getCylinderById(cylinderId) } returns cylinder
        coEvery { fuelMeasurementRepository.insertMeasurements(any()) } returns Unit

        // Act
        val result = saveFuelMeasurementUseCase.saveHistoricalMeasurements(
            cylinderId,
            weightMeasurements
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()) // Should only save 2 valid measurements

        coVerify {
            fuelMeasurementRepository.insertMeasurements(
                withArg { measurements ->
                    assertEquals(2, measurements.size) // Invalid measurement filtered out
                }
            )
        }
    }
}
