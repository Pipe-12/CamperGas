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
    private val repository: GasCylinderRepository = mockk()

    @Before
    fun setUp() {
        addGasCylinderUseCase = AddGasCylinderUseCase(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun invokeCreatesCylinderSuccessfully() = runTest {
        // Arrange
        val name = "Test Cylinder"
        val tare = 10.0f
        val capacity = 15.0f
        val expectedId = 123L

        coEvery { repository.insertCylinder(any()) } returns expectedId

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())

        coVerify {
            repository.insertCylinder(
                withArg { cylinder ->
                    assertEquals(name, cylinder.name)
                    assertEquals(tare, cylinder.tare, 0.01f)
                    assertEquals(capacity, cylinder.capacity, 0.01f)
                    assertFalse(cylinder.isActive)
                }
            )
        }
    }

    @Test
    fun invokeCreatesAndActivatesCylinderWhenSetAsActiveIsTrue() = runTest {
        // Arrange
        val name = "Test Cylinder"
        val tare = 10.0f
        val capacity = 15.0f
        val expectedId = 123L

        coEvery { repository.insertCylinder(any()) } returns expectedId
        coEvery { repository.setActiveCylinder(expectedId) } returns Unit

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, true)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())

        coVerify {
            repository.insertCylinder(
                withArg { cylinder ->
                    assertEquals(name, cylinder.name)
                    assertTrue(cylinder.isActive)
                }
            )
            repository.setActiveCylinder(expectedId)
        }
    }

    @Test
    fun invokeTrimsWhitespaceFromName() = runTest {
        // Arrange
        val name = "  Test Cylinder  "
        val tare = 10.0f
        val capacity = 15.0f
        val expectedId = 123L

        coEvery { repository.insertCylinder(any()) } returns expectedId

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isSuccess)

        coVerify {
            repository.insertCylinder(
                withArg { cylinder ->
                    assertEquals("Test Cylinder", cylinder.name) // Trimmed
                }
            )
        }
    }

    @Test
    fun invokeFailsWithBlankName() = runTest {
        // Arrange
        val name = "   "
        val tare = 10.0f
        val capacity = 15.0f

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("El nombre no puede estar vacío", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { repository.insertCylinder(any()) }
    }

    @Test
    fun invokeFailsWithEmptyName() = runTest {
        // Arrange
        val name = ""
        val tare = 10.0f
        val capacity = 15.0f

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("El nombre no puede estar vacío", result.exceptionOrNull()?.message)
    }

    @Test
    fun invokeFailsWithNegativeTare() = runTest {
        // Arrange
        val name = "Test Cylinder"
        val tare = -5.0f
        val capacity = 15.0f

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("La tara no puede ser negativa", result.exceptionOrNull()?.message)
    }

    @Test
    fun invokeAllowsZeroTare() = runTest {
        // Arrange
        val name = "Test Cylinder"
        val tare = 0.0f
        val capacity = 15.0f
        val expectedId = 123L

        coEvery { repository.insertCylinder(any()) } returns expectedId

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
    }

    @Test
    fun invokeFailsWithZeroCapacity() = runTest {
        // Arrange
        val name = "Test Cylinder"
        val tare = 10.0f
        val capacity = 0.0f

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("La capacidad debe ser mayor que cero", result.exceptionOrNull()?.message)
    }

    @Test
    fun invokeFailsWithNegativeCapacity() = runTest {
        // Arrange
        val name = "Test Cylinder"
        val tare = 10.0f
        val capacity = -5.0f

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("La capacidad debe ser mayor que cero", result.exceptionOrNull()?.message)
    }

    @Test
    fun invokeHandlesRepositoryException() = runTest {
        // Arrange
        val name = "Test Cylinder"
        val tare = 10.0f
        val capacity = 15.0f
        val exception = RuntimeException("Database error")

        coEvery { repository.insertCylinder(any()) } throws exception

        // Act
        val result = addGasCylinderUseCase(name, tare, capacity, false)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
