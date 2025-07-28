package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.FuelMeasurementRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class SaveFuelMeasurementUseCaseTest {

    private lateinit var saveFuelMeasurementUseCase: SaveFuelMeasurementUseCase
    private val fuelMeasurementRepository = mockk<FuelMeasurementRepository>()
    private val gasCylinderRepository = mockk<GasCylinderRepository>()

    private val sampleCylinder = GasCylinder(
        id = 1L,
        name = "Cilindro Test",
        capacity = 11.0f,
        tare = 5.0f,
        isActive = true
    )

    @Before
    fun setup() {
        saveFuelMeasurementUseCase = SaveFuelMeasurementUseCase(
            fuelMeasurementRepository = fuelMeasurementRepository,
            gasCylinderRepository = gasCylinderRepository
        )

        // Mock común: siempre hay un cilindro activo
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(sampleCylinder)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveRealTimeMeasurement saves measurement successfully when cylinder is active`() = runTest {
        // Arrange
        val totalWeight = 10.5f
        val timestamp = System.currentTimeMillis()
        val expectedMeasurementId = 123L

        coEvery { 
            fuelMeasurementRepository.insertRealTimeMeasurement(any()) 
        } returns expectedMeasurementId

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(
            totalWeight = totalWeight,
            timestamp = timestamp
        )

        // Assert
        assertTrue(result.isSuccess)
        val saveResult = result.getOrThrow()
        assertTrue(saveResult.processed)
        assertEquals(expectedMeasurementId, saveResult.measurementId)
        assertTrue(saveResult.reason.contains("guardada correctamente"))

        // Verify interaction
        coVerify { fuelMeasurementRepository.insertRealTimeMeasurement(any()) }
    }

    @Test
    fun `saveRealTimeMeasurement fails when no active cylinder`() = runTest {
        // Arrange
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(null)

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(
            totalWeight = 10.5f,
            timestamp = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("No hay bombona activa") == true)

        // Verify no interaction with repository
        coVerify(exactly = 0) { fuelMeasurementRepository.insertRealTimeMeasurement(any()) }
    }

    @Test
    fun `saveRealTimeMeasurement handles repository exception`() = runTest {
        // Arrange
        val errorMessage = "Database error"
        coEvery { 
            fuelMeasurementRepository.insertRealTimeMeasurement(any()) 
        } throws Exception(errorMessage)

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(
            totalWeight = 10.5f,
            timestamp = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveHistoricalMeasurements saves multiple measurements correctly`() = runTest {
        // Arrange
        val cylinderId = 1L
        val weightMeasurements = listOf(
            Pair(10.5f, System.currentTimeMillis()),
            Pair(10.3f, System.currentTimeMillis() - 60000),
            Pair(10.1f, System.currentTimeMillis() - 120000)
        )
        val expectedSavedCount = 3

        coEvery { 
            fuelMeasurementRepository.insertHistoricalMeasurements(any()) 
        } returns expectedSavedCount

        // Act
        val result = saveFuelMeasurementUseCase.saveHistoricalMeasurements(
            cylinderId = cylinderId,
            weightMeasurements = weightMeasurements
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedSavedCount, result.getOrThrow())

        // Verify correct number of measurements passed
        coVerify { 
            fuelMeasurementRepository.insertHistoricalMeasurements(
                match { measurements -> measurements.size == 3 }
            ) 
        }
    }

    @Test
    fun `saveHistoricalMeasurements handles empty list`() = runTest {
        // Arrange
        val cylinderId = 1L
        val weightMeasurements = emptyList<Pair<Float, Long>>()

        coEvery { 
            fuelMeasurementRepository.insertHistoricalMeasurements(any()) 
        } returns 0

        // Act
        val result = saveFuelMeasurementUseCase.saveHistoricalMeasurements(
            cylinderId = cylinderId,
            weightMeasurements = weightMeasurements
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow())
    }

    @Test
    fun `fuel calculation is correct with valid cylinder`() = runTest {
        // Arrange
        val totalWeight = 10.5f // 10.5kg total
        val cylinderTare = 5.0f  // 5kg tara
        val cylinderCapacity = 11.0f // 11kg capacidad
        val expectedFuelKg = 5.5f // 10.5 - 5.0 = 5.5kg combustible
        val expectedFuelPercentage = 50.0f // 5.5/11 * 100 = 50%

        val cylinder = sampleCylinder.copy(
            tare = cylinderTare,
            capacity = cylinderCapacity
        )
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(cylinder)

        coEvery { 
            fuelMeasurementRepository.insertRealTimeMeasurement(any()) 
        } returns 1L

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(
            totalWeight = totalWeight,
            timestamp = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isSuccess)
        
        // Verify the measurement entity has correct calculated values
        coVerify { 
            fuelMeasurementRepository.insertRealTimeMeasurement(
                match { entity ->
                    entity.fuelKilograms == expectedFuelKg &&
                    entity.fuelPercentage == expectedFuelPercentage &&
                    entity.totalWeight == totalWeight
                }
            ) 
        }
    }

    @Test
    fun `fuel calculation handles negative fuel weight`() = runTest {
        // Arrange - total weight less than tare
        val totalWeight = 4.0f // 4kg total
        val cylinderTare = 5.0f  // 5kg tara
        val expectedFuelKg = 0.0f // Should be clamped to 0

        val cylinder = sampleCylinder.copy(tare = cylinderTare)
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(cylinder)

        coEvery { 
            fuelMeasurementRepository.insertRealTimeMeasurement(any()) 
        } returns 1L

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(
            totalWeight = totalWeight,
            timestamp = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isSuccess)
        
        // Verify fuel weight is clamped to 0
        coVerify { 
            fuelMeasurementRepository.insertRealTimeMeasurement(
                match { entity -> entity.fuelKilograms == expectedFuelKg }
            ) 
        }
    }

    @Test
    fun `fuel percentage calculation handles zero capacity`() = runTest {
        // Arrange
        val totalWeight = 10.5f
        val cylinderCapacity = 0.0f // Zero capacity
        val expectedFuelPercentage = 0.0f

        val cylinder = sampleCylinder.copy(capacity = cylinderCapacity)
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(cylinder)

        coEvery { 
            fuelMeasurementRepository.insertRealTimeMeasurement(any()) 
        } returns 1L

        // Act
        val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(
            totalWeight = totalWeight,
            timestamp = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isSuccess)
        
        // Verify percentage is 0 when capacity is 0
        coVerify { 
            fuelMeasurementRepository.insertRealTimeMeasurement(
                match { entity -> entity.fuelPercentage == expectedFuelPercentage }
            ) 
        }
    }
}
