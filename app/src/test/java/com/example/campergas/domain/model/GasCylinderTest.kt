package com.example.campergas.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GasCylinderTest {

    @Test
    fun `calculateGasContent with normal weight returns correct gas amount`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 12.0f // 5kg tare + 7kg gas

        // Act
        val gasContent = cylinder.calculateGasContent(totalWeight)

        // Assert
        assertEquals(7.0f, gasContent, 0.01f)
    }

    @Test
    fun `calculateGasContent with weight less than tare returns zero`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 3.0f // Less than tare

        // Act
        val gasContent = cylinder.calculateGasContent(totalWeight)

        // Assert
        assertEquals(0.0f, gasContent, 0.01f)
    }

    @Test
    fun `calculateGasPercentage with normal values returns correct percentage`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 10.0f // 5kg tare + 5kg gas (50% of 10kg capacity)

        // Act
        val percentage = cylinder.calculateGasPercentage(totalWeight)

        // Assert
        assertEquals(50.0f, percentage, 0.01f)
    }

    @Test
    fun `calculateGasPercentage with full tank returns 100 percent`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 15.0f // 5kg tare + 10kg gas (100% of 10kg capacity)

        // Act
        val percentage = cylinder.calculateGasPercentage(totalWeight)

        // Assert
        assertEquals(100.0f, percentage, 0.01f)
    }

    @Test
    fun `calculateGasPercentage with overfilled tank is capped at 100 percent`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 20.0f // 5kg tare + 15kg gas (150% of 10kg capacity)

        // Act
        val percentage = cylinder.calculateGasPercentage(totalWeight)

        // Assert
        assertEquals(100.0f, percentage, 0.01f)
    }

    @Test
    fun `calculateGasPercentage with zero capacity returns zero percent`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 0.0f,
            isActive = true
        )
        val totalWeight = 10.0f

        // Act
        val percentage = cylinder.calculateGasPercentage(totalWeight)

        // Assert
        assertEquals(0.0f, percentage, 0.01f)
    }

    @Test
    fun `isEmpty returns true when gas percentage is less than 5 percent`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 5.3f // 5kg tare + 0.3kg gas (3% of 10kg capacity)

        // Act
        val isEmpty = cylinder.isEmpty(totalWeight)

        // Assert
        assertTrue(isEmpty)
    }

    @Test
    fun `isEmpty returns false when gas percentage is 5 percent or more`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 5.6f // 5kg tare + 0.6kg gas (6% of 10kg capacity)

        // Act
        val isEmpty = cylinder.isEmpty(totalWeight)

        // Assert
        assertFalse(isEmpty)
    }

    @Test
    fun `isLowGas returns true when gas percentage is less than 20 percent`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 6.5f // 5kg tare + 1.5kg gas (15% of 10kg capacity)

        // Act
        val isLowGas = cylinder.isLowGas(totalWeight)

        // Assert
        assertTrue(isLowGas)
    }

    @Test
    fun `isLowGas returns false when gas percentage is 20 percent or more`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            tare = 5.0f,
            capacity = 10.0f,
            isActive = true
        )
        val totalWeight = 8.0f // 5kg tare + 3kg gas (30% of 10kg capacity)

        // Act
        val isLowGas = cylinder.isLowGas(totalWeight)

        // Assert
        assertFalse(isLowGas)
    }

    @Test
    fun `getDisplayName returns formatted name with tare and capacity`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Main Tank",
            tare = 5.5f,
            capacity = 12.5f,
            isActive = true
        )

        // Act
        val displayName = cylinder.getDisplayName()

        // Assert
        assertEquals("Main Tank (5.5kg + 12.5kg)", displayName)
    }
}