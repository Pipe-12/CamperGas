package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AddGasCylinderUseCaseTest {

    private lateinit var addGasCylinderUseCase: AddGasCylinderUseCase
    private val gasCylinderRepository = mockk<GasCylinderRepository>()

    @Before
    fun setup() {
        addGasCylinderUseCase = AddGasCylinderUseCase(gasCylinderRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke creates cylinder successfully with valid data`() = runTest {
        // Arrange
        val name = "Cilindro Test"
        val capacity = 11.0f
        val tare = 5.0f
        val expectedId = 123L

        coEvery { gasCylinderRepository.insertCylinder(any()) } returns expectedId

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrThrow())

        // Verify correct data passed to repository
        coVerify { 
            gasCylinderRepository.insertCylinder(
                match { cylinder ->
                    cylinder.name == name &&
                    cylinder.capacity == capacity &&
                    cylinder.tare == tare &&
                    !cylinder.isActive // Should be inactive by default
                }
            )
        }
    }

    @Test
    fun `invoke fails with empty name`() = runTest {
        // Arrange
        val name = ""
        val capacity = 11.0f
        val tare = 5.0f

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("El nombre no puede estar vacío") == true)

        // Verify no interaction with repository
        coVerify(exactly = 0) { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke fails with blank name`() = runTest {
        // Arrange
        val name = "   "
        val capacity = 11.0f
        val tare = 5.0f

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("El nombre no puede estar vacío") == true)

        coVerify(exactly = 0) { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke fails with negative capacity`() = runTest {
        // Arrange
        val name = "Cilindro Test"
        val capacity = -1.0f
        val tare = 5.0f

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("La capacidad debe ser mayor que 0") == true)

        coVerify(exactly = 0) { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke fails with zero capacity`() = runTest {
        // Arrange
        val name = "Cilindro Test"
        val capacity = 0.0f
        val tare = 5.0f

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("La capacidad debe ser mayor que 0") == true)

        coVerify(exactly = 0) { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke fails with negative tare`() = runTest {
        // Arrange
        val name = "Cilindro Test"
        val capacity = 11.0f
        val tare = -1.0f

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("La tara debe ser mayor o igual que 0") == true)

        coVerify(exactly = 0) { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke accepts zero tare`() = runTest {
        // Arrange
        val name = "Cilindro Test"
        val capacity = 11.0f
        val tare = 0.0f
        val expectedId = 123L

        coEvery { gasCylinderRepository.insertCylinder(any()) } returns expectedId

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrThrow())

        coVerify { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke fails when tare is greater than capacity`() = runTest {
        // Arrange
        val name = "Cilindro Test"
        val capacity = 5.0f
        val tare = 6.0f

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("La tara no puede ser mayor que la capacidad") == true)

        coVerify(exactly = 0) { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke accepts tare equal to capacity`() = runTest {
        // Arrange
        val name = "Cilindro Test"
        val capacity = 5.0f
        val tare = 5.0f
        val expectedId = 123L

        coEvery { gasCylinderRepository.insertCylinder(any()) } returns expectedId

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrThrow())

        coVerify { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke handles repository exception`() = runTest {
        // Arrange
        val name = "Cilindro Test"
        val capacity = 11.0f
        val tare = 5.0f
        val errorMessage = "Database error"

        coEvery { gasCylinderRepository.insertCylinder(any()) } throws Exception(errorMessage)

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)

        coVerify { gasCylinderRepository.insertCylinder(any()) }
    }

    @Test
    fun `invoke trims whitespace from name`() = runTest {
        // Arrange
        val name = "  Cilindro Test  "
        val expectedTrimmedName = "Cilindro Test"
        val capacity = 11.0f
        val tare = 5.0f
        val expectedId = 123L

        coEvery { gasCylinderRepository.insertCylinder(any()) } returns expectedId

        // Act
        val result = addGasCylinderUseCase(
            name = name,
            capacity = capacity,
            tare = tare
        )

        // Assert
        assertTrue(result.isSuccess)

        // Verify trimmed name is used
        coVerify { 
            gasCylinderRepository.insertCylinder(
                match { cylinder -> cylinder.name == expectedTrimmedName }
            )
        }
    }
}
