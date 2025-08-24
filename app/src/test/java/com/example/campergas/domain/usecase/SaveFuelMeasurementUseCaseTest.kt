package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.model.GasCylinder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveFuelMeasurementUseCaseTest {

    private lateinit var useCase: SaveFuelMeasurementUseCase
    private val fuelMeasurementRepository: FuelMeasurementRepository = mockk()
    private val gasCylinderRepository: GasCylinderRepository = mockk()

    private val testCylinder = GasCylinder(
        id = 1L,
        name = "Test Cylinder",
        tare = 5.0f,
        capacity = 10.0f,
        isActive = true
    )

    @Before
    fun setUp() {
        useCase = SaveFuelMeasurementUseCase(fuelMeasurementRepository, gasCylinderRepository)
    }

    @Test
    fun `saveRealTimeMeasurement with valid data saves measurement`() = runTest {
        // Arrange
        val totalWeight = 12.0f // 5kg tare + 7kg gas
        val timestamp = System.currentTimeMillis()
        val expectedId = 123L

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(testCylinder)
        coEvery { fuelMeasurementRepository.insertMeasurement(any()) } returns expectedId

        // Act
        val result = useCase.saveRealTimeMeasurement(totalWeight, timestamp)

        // Assert
        assertTrue(result.isSuccess)
        val saveResult = result.getOrNull()!!
        assertEquals(expectedId, saveResult.measurementId)
        assertTrue(saveResult.processed)
        assertEquals("Medición guardada correctamente", saveResult.reason)

        coVerify {
            fuelMeasurementRepository.insertMeasurement(
                match { measurement ->
                    measurement.cylinderId == testCylinder.id &&
                    measurement.cylinderName == testCylinder.name &&
                    measurement.timestamp == timestamp &&
                    measurement.fuelKilograms == 7.0f &&
                    measurement.fuelPercentage == 70.0f &&
                    measurement.totalWeight == totalWeight &&
                    measurement.isCalibrated &&
                    !measurement.isHistorical
                }
            )
        }
    }

    @Test
    fun `saveRealTimeMeasurement with no active cylinder returns failure`() = runTest {
        // Arrange
        val totalWeight = 12.0f
        val timestamp = System.currentTimeMillis()

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(null)

        // Act
        val result = useCase.saveRealTimeMeasurement(totalWeight, timestamp)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("No hay bombona activa configurada", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { fuelMeasurementRepository.insertMeasurement(any()) }
    }

    @Test
    fun `saveRealTimeMeasurement with weight less than tare calculates zero fuel`() = runTest {
        // Arrange
        val totalWeight = 3.0f // Less than 5kg tare
        val timestamp = System.currentTimeMillis()
        val expectedId = 123L

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(testCylinder)
        coEvery { fuelMeasurementRepository.insertMeasurement(any()) } returns expectedId

        // Act
        val result = useCase.saveRealTimeMeasurement(totalWeight, timestamp)

        // Assert
        assertTrue(result.isSuccess)
        val saveResult = result.getOrNull()!!
        assertTrue(saveResult.processed)

        coVerify {
            fuelMeasurementRepository.insertMeasurement(
                match { measurement ->
                    measurement.fuelKilograms == 0.0f &&
                    measurement.fuelPercentage == 0.0f
                }
            )
        }
    }

    @Test
    fun `saveRealTimeMeasurement handles repository exception`() = runTest {
        // Arrange
        val totalWeight = 12.0f
        val timestamp = System.currentTimeMillis()
        val exception = RuntimeException("Database error")

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(testCylinder)
        coEvery { fuelMeasurementRepository.insertMeasurement(any()) } throws exception

        // Act
        val result = useCase.saveRealTimeMeasurement(totalWeight, timestamp)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `saveHistoricalMeasurements with valid data saves all measurements`() = runTest {
        // Arrange
        val cylinderId = 1L
        val weightMeasurements = listOf(
            Pair(12.0f, 1000L), // 7kg gas, 70%
            Pair(10.0f, 2000L), // 5kg gas, 50%
            Pair(8.0f, 3000L)   // 3kg gas, 30%
        )

        coEvery { gasCylinderRepository.getCylinderById(cylinderId) } returns testCylinder
        coEvery { fuelMeasurementRepository.insertMeasurements(any()) } returns Unit

        // Act
        val result = useCase.saveHistoricalMeasurements(cylinderId, weightMeasurements)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()) // 3 measurements saved

        coVerify {
            fuelMeasurementRepository.insertMeasurements(
                match { measurements ->
                    measurements.size == 3 &&
                    measurements.all { it.isHistorical } &&
                    measurements.all { it.isCalibrated } &&
                    measurements.all { it.cylinderId == cylinderId }
                }
            )
        }
    }

    @Test
    fun `saveHistoricalMeasurements with cylinder not found returns failure`() = runTest {
        // Arrange
        val cylinderId = 1L
        val weightMeasurements = listOf(Pair(12.0f, 1000L))

        coEvery { gasCylinderRepository.getCylinderById(cylinderId) } returns null

        // Act
        val result = useCase.saveHistoricalMeasurements(cylinderId, weightMeasurements)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Bombona no encontrada", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { fuelMeasurementRepository.insertMeasurements(any()) }
    }

    @Test
    fun `saveHistoricalMeasurements filters invalid measurements`() = runTest {
        // Arrange
        val cylinderId = 1L
        val weightMeasurements = listOf(
            Pair(12.0f, 1000L), // Valid
            Pair(Float.NaN, 2000L), // Invalid - will be filtered out
            Pair(10.0f, 3000L)  // Valid
        )

        coEvery { gasCylinderRepository.getCylinderById(cylinderId) } returns testCylinder
        coEvery { fuelMeasurementRepository.insertMeasurements(any()) } returns Unit

        // Act
        val result = useCase.saveHistoricalMeasurements(cylinderId, weightMeasurements)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()) // Only 2 valid measurements saved

        coVerify {
            fuelMeasurementRepository.insertMeasurements(
                match { measurements ->
                    measurements.size == 2 &&
                    measurements.all { it.isValid() }
                }
            )
        }
    }

    @Test
    fun `saveHistoricalMeasurements handles repository exception`() = runTest {
        // Arrange
        val cylinderId = 1L
        val weightMeasurements = listOf(Pair(12.0f, 1000L))
        val exception = RuntimeException("Database error")

        coEvery { gasCylinderRepository.getCylinderById(cylinderId) } returns testCylinder
        coEvery { fuelMeasurementRepository.insertMeasurements(any()) } throws exception

        // Act
        val result = useCase.saveHistoricalMeasurements(cylinderId, weightMeasurements)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `saveRealTimeMeasurement calculates correct percentage for cylinder with zero capacity`() = runTest {
        // Arrange
        val zeroCylinder = testCylinder.copy(capacity = 0.0f)
        val totalWeight = 12.0f
        val timestamp = System.currentTimeMillis()
        val expectedId = 123L

        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(zeroCylinder)
        coEvery { fuelMeasurementRepository.insertMeasurement(any()) } returns expectedId

        // Act
        val result = useCase.saveRealTimeMeasurement(totalWeight, timestamp)

        // Assert
        assertTrue(result.isSuccess)

        coVerify {
            fuelMeasurementRepository.insertMeasurement(
                match { measurement ->
                    measurement.fuelKilograms == 7.0f &&
                    measurement.fuelPercentage == 0.0f // Zero capacity = 0%
                }
            )
        }
    }
}