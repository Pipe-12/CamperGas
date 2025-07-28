package com.example.campergas.domain.model

import org.junit.Test
import org.junit.Assert.*

class GasCylinderTest {

    @Test
    fun `calculateFuelKilograms returns correct amount`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val totalWeight = 10.5f

        // Act
        val fuelKg = cylinder.calculateFuelKilograms(totalWeight)

        // Assert
        assertEquals(5.5f, fuelKg, 0.01f)
    }

    @Test
    fun `calculateFuelKilograms returns zero when weight less than tare`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val totalWeight = 4.0f

        // Act
        val fuelKg = cylinder.calculateFuelKilograms(totalWeight)

        // Assert
        assertEquals(0.0f, fuelKg, 0.01f)
    }

    @Test
    fun `calculateFuelPercentage returns correct percentage`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val fuelKg = 5.5f

        // Act
        val percentage = cylinder.calculateFuelPercentage(fuelKg)

        // Assert
        assertEquals(50.0f, percentage, 0.01f)
    }

    @Test
    fun `calculateFuelPercentage returns zero when capacity is zero`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 0.0f,
            tare = 5.0f,
            isActive = true
        )
        val fuelKg = 5.5f

        // Act
        val percentage = cylinder.calculateFuelPercentage(fuelKg)

        // Assert
        assertEquals(0.0f, percentage, 0.01f)
    }

    @Test
    fun `calculateFuelPercentage clamps to 100 percent`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 10.0f,
            tare = 5.0f,
            isActive = true
        )
        val fuelKg = 15.0f // More than capacity

        // Act
        val percentage = cylinder.calculateFuelPercentage(fuelKg)

        // Assert
        assertEquals(100.0f, percentage, 0.01f)
    }

    @Test
    fun `calculateFuelPercentage clamps to 0 percent`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 10.0f,
            tare = 5.0f,
            isActive = true
        )
        val fuelKg = -1.0f // Negative fuel

        // Act
        val percentage = cylinder.calculateFuelPercentage(fuelKg)

        // Assert
        assertEquals(0.0f, percentage, 0.01f)
    }

    @Test
    fun `isEmpty returns true when fuel is zero`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val totalWeight = 5.0f // Equal to tare

        // Act
        val isEmpty = cylinder.isEmpty(totalWeight)

        // Assert
        assertTrue(isEmpty)
    }

    @Test
    fun `isEmpty returns false when fuel is present`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val totalWeight = 8.0f // Greater than tare

        // Act
        val isEmpty = cylinder.isEmpty(totalWeight)

        // Assert
        assertFalse(isEmpty)
    }

    @Test
    fun `isFull returns true when fuel equals capacity`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val totalWeight = 16.0f // Tare + capacity

        // Act
        val isFull = cylinder.isFull(totalWeight)

        // Assert
        assertTrue(isFull)
    }

    @Test
    fun `isFull returns false when fuel is less than capacity`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val totalWeight = 10.0f // Less than tare + capacity

        // Act
        val isFull = cylinder.isFull(totalWeight)

        // Assert
        assertFalse(isFull)
    }

    @Test
    fun `data class equality works correctly`() {
        // Arrange
        val cylinder1 = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val cylinder2 = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )
        val cylinder3 = GasCylinder(
            id = 2L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )

        // Assert
        assertEquals(cylinder1, cylinder2)
        assertNotEquals(cylinder1, cylinder3)
        assertEquals(cylinder1.hashCode(), cylinder2.hashCode())
    }

    @Test
    fun `toString contains relevant information`() {
        // Arrange
        val cylinder = GasCylinder(
            id = 1L,
            name = "Test Cylinder",
            capacity = 11.0f,
            tare = 5.0f,
            isActive = true
        )

        // Act
        val toString = cylinder.toString()

        // Assert
        assertTrue(toString.contains("Test Cylinder"))
        assertTrue(toString.contains("11.0"))
        assertTrue(toString.contains("5.0"))
    }
}
