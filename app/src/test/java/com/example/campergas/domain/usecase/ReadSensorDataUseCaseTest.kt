package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ReadSensorDataUseCaseTest {

    private lateinit var readSensorDataUseCase: ReadSensorDataUseCase
    private val bleRepository: BleRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        readSensorDataUseCase = ReadSensorDataUseCase(bleRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `readAllSensorData calls both weight and inclination methods`() = runTest {
        // Act
        readSensorDataUseCase.readAllSensorData()

        // Assert - verify both methods were called
        verify { bleRepository.readWeightDataOnDemand() }
        verify { bleRepository.readInclinationDataOnDemand() }
    }

    @Test
    fun `readWeightData calls repository readWeightDataOnDemand`() = runTest {
        // Act
        readSensorDataUseCase.readWeightData()

        // Assert
        verify { bleRepository.readWeightDataOnDemand() }
    }

    @Test
    fun `readInclinationData calls repository readInclinationDataOnDemand`() = runTest {
        // Act
        readSensorDataUseCase.readInclinationData()

        // Assert
        verify { bleRepository.readInclinationDataOnDemand() }
    }
}