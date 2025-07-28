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

class GetActiveCylinderUseCaseTestFixed {

    private lateinit var getActiveCylinderUseCase: GetActiveCylinderUseCase
    private val repository: GasCylinderRepository = mockk()

    @Before
    fun setUp() {
        getActiveCylinderUseCase = GetActiveCylinderUseCase(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `invoke returns flow of active cylinder`() = runTest {
        // Arrange
        val activeCylinder = GasCylinder(
            id = 1L,
            name = "Active Cylinder",
            tare = 10.0f,
            capacity = 15.0f,
            isActive = true
        )
        every { repository.getActiveCylinder() } returns flowOf(activeCylinder)

        // Act
        val flow = getActiveCylinderUseCase()

        // Assert
        flow.collect { cylinder ->
            assertNotNull(cylinder)
            assertEquals(activeCylinder.id, cylinder?.id)
            assertEquals(activeCylinder.name, cylinder?.name)
            assertTrue(cylinder?.isActive == true)
        }

        verify { repository.getActiveCylinder() }
    }

    @Test
    fun `invoke returns flow of null when no active cylinder`() = runTest {
        // Arrange
        every { repository.getActiveCylinder() } returns flowOf(null)

        // Act
        val flow = getActiveCylinderUseCase()

        // Assert
        flow.collect { cylinder ->
            assertNull(cylinder)
        }

        verify { repository.getActiveCylinder() }
    }

    @Test
    fun `getActiveCylinderSync returns active cylinder`() = runTest {
        // Arrange
        val activeCylinder = GasCylinder(
            id = 1L,
            name = "Active Cylinder",
            tare = 10.0f,
            capacity = 15.0f,
            isActive = true
        )
        coEvery { repository.getActiveCylinderSync() } returns activeCylinder

        // Act
        val result = getActiveCylinderUseCase.getActiveCylinderSync()

        // Assert
        assertNotNull(result)
        assertEquals(activeCylinder.id, result?.id)
        assertEquals(activeCylinder.name, result?.name)
        assertTrue(result?.isActive == true)

        coVerify { repository.getActiveCylinderSync() }
    }

    @Test
    fun `getActiveCylinderSync returns null when no active cylinder`() = runTest {
        // Arrange
        coEvery { repository.getActiveCylinderSync() } returns null

        // Act
        val result = getActiveCylinderUseCase.getActiveCylinderSync()

        // Assert
        assertNull(result)

        coVerify { repository.getActiveCylinderSync() }
    }
}
