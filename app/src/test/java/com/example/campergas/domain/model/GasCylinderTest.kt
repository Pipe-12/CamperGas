package com.example.campergas.domain.model

import org.junit.Test
import org.junit.Assert.*

class GasCylinderTest {

    @Test
    fun `data class properties are set correctly`() {
        // Arrange
        val id = 123L
        val name = "Test Cylinder"
        val tare = 10.0f
        val capacity = 15.0f
        val isActive = true
        val createdAt = 1640995200000L

        // Act
        val cylinder = GasCylinder(
            id = id,
            name = name,
            tare = tare,
            capacity = capacity,
            isActive = isActive,
            createdAt = createdAt
        )

        // Assert
        assertEquals(id, cylinder.id)
        assertEquals(name, cylinder.name)
        assertEquals(tare, cylinder.tare, 0.01f)
        assertEquals(capacity, cylinder.capacity, 0.01f)
        assertEquals(isActive, cylinder.isActive)
        assertEquals(createdAt, cylinder.createdAt)
    }

    @Test
    fun `calculateGasContent returns correct value`() {
        val cylinder = createTestCylinder(tare = 10.0f)
        val totalWeight = 15.0f
        val expected = 5.0f // 15 - 10
        assertEquals(expected, cylinder.calculateGasContent(totalWeight), 0.01f)
    }

    @Test
    fun `calculateGasContent returns zero for negative values`() {
        val cylinder = createTestCylinder(tare = 10.0f)
        val totalWeight = 8.0f // Less than tare
        assertEquals(0f, cylinder.calculateGasContent(totalWeight), 0.01f)
    }

    @Test
    fun `calculateGasPercentage returns correct percentage`() {
        val cylinder = createTestCylinder(tare = 10.0f, capacity = 5.0f)
        val totalWeight = 12.5f // 2.5kg gas / 5kg capacity = 50%
        assertEquals(50.0f, cylinder.calculateGasPercentage(totalWeight), 0.01f)
    }

    @Test
    fun `calculateGasPercentage returns zero for zero capacity`() {
        val cylinder = createTestCylinder(capacity = 0f)
        assertEquals(0f, cylinder.calculateGasPercentage(15.0f), 0.01f)
    }

    @Test
    fun `calculateGasPercentage caps at 100 percent`() {
        val cylinder = createTestCylinder(tare = 10.0f, capacity = 5.0f)
        val totalWeight = 20.0f // More than full capacity
        assertEquals(100.0f, cylinder.calculateGasPercentage(totalWeight), 0.01f)
    }

    @Test
    fun `isEmpty returns true when percentage below 5`() {
        val cylinder = createTestCylinder(tare = 10.0f, capacity = 10.0f)
        val totalWeight = 10.2f // 0.2kg gas / 10kg capacity = 2%
        assertTrue(cylinder.isEmpty(totalWeight))
    }

    @Test
    fun `isEmpty returns false when percentage above 5`() {
        val cylinder = createTestCylinder(tare = 10.0f, capacity = 10.0f)
        val totalWeight = 11.0f // 1kg gas / 10kg capacity = 10%
        assertFalse(cylinder.isEmpty(totalWeight))
    }

    @Test
    fun `isLowGas returns true when percentage below 20`() {
        val cylinder = createTestCylinder(tare = 10.0f, capacity = 10.0f)
        val totalWeight = 11.5f // 1.5kg gas / 10kg capacity = 15%
        assertTrue(cylinder.isLowGas(totalWeight))
    }

    @Test
    fun `isLowGas returns false when percentage above 20`() {
        val cylinder = createTestCylinder(tare = 10.0f, capacity = 10.0f)
        val totalWeight = 13.0f // 3kg gas / 10kg capacity = 30%
        assertFalse(cylinder.isLowGas(totalWeight))
    }

    @Test
    fun `getDisplayName returns formatted string`() {
        val cylinder = createTestCylinder(
            name = "Test Cylinder",
            tare = 10.0f,
            capacity = 15.0f
        )
        assertEquals("Test Cylinder (10.0kg + 15.0kg)", cylinder.getDisplayName())
    }

    @Test
    fun `default values are set correctly`() {
        val cylinder = GasCylinder(
            name = "Test",
            tare = 10.0f,
            capacity = 5.0f
        )
        
        assertEquals(0L, cylinder.id) // Default id
        assertFalse(cylinder.isActive) // Default isActive
        // createdAt should be around current time (within reasonable range)
        val now = System.currentTimeMillis()
        assertTrue("createdAt should be recent", 
            Math.abs(now - cylinder.createdAt) < 1000) // Within 1 second
    }

    private fun createTestCylinder(
        id: Long = 1L,
        name: String = "Test Cylinder",
        tare: Float = 10.0f,
        capacity: Float = 15.0f,
        isActive: Boolean = false,
        createdAt: Long = System.currentTimeMillis()
    ) = GasCylinder(
        id = id,
        name = name,
        tare = tare,
        capacity = capacity,
        isActive = isActive,
        createdAt = createdAt
    )
}
