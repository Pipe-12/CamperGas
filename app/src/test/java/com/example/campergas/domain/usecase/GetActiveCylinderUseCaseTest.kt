package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class GetActiveCylinderUseCaseTest {

    private lateinit var getActiveCylinderUseCase: GetActiveCylinderUseCase
    private val gasCylinderRepository = mockk<GasCylinderRepository>()

    private val sampleActiveCylinder = GasCylinder(
        id = 1L,
        name = "Cilindro Activo",
        capacity = 11.0f,
        tare = 5.0f,
        isActive = true
    )

    @Before
    fun setup() {
        getActiveCylinderUseCase = GetActiveCylinderUseCase(gasCylinderRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns active cylinder when exists`() = runTest {
        // Arrange
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(sampleActiveCylinder)

        // Act
        val result = getActiveCylinderUseCase()

        // Assert
        result.collect { cylinder ->
            assertNotNull(cylinder)
            assertEquals(sampleActiveCylinder.id, cylinder?.id)
            assertEquals(sampleActiveCylinder.name, cylinder?.name)
            assertTrue(cylinder?.isActive == true)
        }

        verify { gasCylinderRepository.getActiveCylinder() }
    }

    @Test
    fun `invoke returns null when no active cylinder`() = runTest {
        // Arrange
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(null)

        // Act
        val result = getActiveCylinderUseCase()

        // Assert
        result.collect { cylinder ->
            assertNull(cylinder)
        }

        verify { gasCylinderRepository.getActiveCylinder() }
    }

    @Test
    fun `getActiveCylinderSync returns active cylinder synchronously`() = runTest {
        // Arrange
        coEvery { gasCylinderRepository.getActiveCylinderSync() } returns sampleActiveCylinder

        // Act
        val result = getActiveCylinderUseCase.getActiveCylinderSync()

        // Assert
        assertNotNull(result)
        assertEquals(sampleActiveCylinder.id, result?.id)
        assertEquals(sampleActiveCylinder.name, result?.name)
        assertTrue(result?.isActive == true)

        coVerify { gasCylinderRepository.getActiveCylinderSync() }
    }

    @Test
    fun `getActiveCylinderSync returns null when no active cylinder`() = runTest {
        // Arrange
        coEvery { gasCylinderRepository.getActiveCylinderSync() } returns null

        // Act
        val result = getActiveCylinderUseCase.getActiveCylinderSync()

        // Assert
        assertNull(result)

        coVerify { gasCylinderRepository.getActiveCylinderSync() }
    }

    @Test
    fun `getActiveCylinderSync handles repository exception`() = runTest {
        // Arrange
        val errorMessage = "Database error"
        coEvery { gasCylinderRepository.getActiveCylinderSync() } throws Exception(errorMessage)

        // Act & Assert
        try {
            getActiveCylinderUseCase.getActiveCylinderSync()
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }

        coVerify { gasCylinderRepository.getActiveCylinderSync() }
    }

    @Test
    fun `multiple calls to invoke return same flow`() = runTest {
        // Arrange
        every { gasCylinderRepository.getActiveCylinder() } returns flowOf(sampleActiveCylinder)

        // Act
        val result1 = getActiveCylinderUseCase()
        val result2 = getActiveCylinderUseCase()

        // Assert
        assertNotNull(result1)
        assertNotNull(result2)

        // Verify repository is called for each invocation
        verify(exactly = 2) { gasCylinderRepository.getActiveCylinder() }
    }
}
